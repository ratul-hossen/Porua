package com.porua.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.porua.app.data.ai.AiHelper
import com.porua.app.data.content.ContentRepository
import com.porua.app.data.content.PooledMcqItem
import com.porua.app.data.local.entity.QuizAttemptEntity
import com.porua.app.data.repository.PoruaRepository
import com.porua.app.domain.sm2.Sm2Algorithm
import com.porua.app.domain.sm2.Sm2Rating
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class QuizQuestion(
    val id: String,
    val index: Int, // 1 to 20
    val subjectId: String,
    val subjectName: String,
    val chapterNumber: Int,
    val chapterTitle: String,
    val paper: String,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: String,
    val explanation: String
)

data class ChapterAccuracy(
    val chapterNumber: Int,
    val chapterTitle: String,
    val correctCount: Int,
    val totalCount: Int
) {
    val percentage: Int get() = if (totalCount > 0) (correctCount * 100) / totalCount else 0
}

data class QuizReviewItem(
    val question: QuizQuestion,
    val selectedOption: String?,
    val isCorrect: Boolean,
    val isSkipped: Boolean
)

enum class ReviewFilter {
    ALL, WRONG_ONLY, SKIPPED
}

data class QuizResultData(
    val totalQuestions: Int,
    val score: Int,
    val wrongCount: Int,
    val skippedCount: Int,
    val percentage: Int,
    val timeTakenSeconds: Int,
    val chapterBreakdowns: List<ChapterAccuracy>,
    val reviewItems: List<QuizReviewItem>
)

data class QuizUiState(
    val subjectId: String = "",
    val subjectName: String = "",
    val chapterNumber: Int? = null,
    val questions: List<QuizQuestion> = emptyList(),
    val selectedAnswers: Map<Int, String?> = emptyMap(), // question index (0..N-1) -> "A"|"B"|"C"|"D"|null
    val elapsedSeconds: Int = 0,
    val isLoading: Boolean = false,
    val isSubmitted: Boolean = false,
    val availabilityNote: String? = null,
    val result: QuizResultData? = null,
    val reviewFilter: ReviewFilter = ReviewFilter.ALL,
    val aiExplanations: Map<String, String> = emptyMap(), // questionId -> explanation text
    val aiLoadingQuestionId: String? = null
)

class QuizViewModel(
    private val appContext: Context,
    private val repository: PoruaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun startQuiz(
        subjectId: String,
        chapterNumber: Int? = null,
        chapterIndex: Int? = null,
        retryQuestions: List<QuizQuestion>? = null
    ) {
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                subjectId = subjectId,
                chapterNumber = chapterNumber,
                selectedAnswers = emptyMap(),
                elapsedSeconds = 0,
                isSubmitted = false,
                result = null,
                isLoading = true,
                availabilityNote = null,
                aiExplanations = emptyMap(),
                aiLoadingQuestionId = null
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            val questions: List<QuizQuestion>
            var note: String? = null

            if (retryQuestions != null && retryQuestions.isNotEmpty()) {
                questions = retryQuestions.mapIndexed { idx, q -> q.copy(index = idx + 1) }
            } else {
                val allMcqs = ContentRepository.getAllMcqsForSubject(appContext, subjectId)

                // If chapterIndex is provided, prioritize it for exact chapter targeting (handles 1st vs 2nd paper)
                val candidateItems = when {
                    chapterIndex != null -> allMcqs.filter { it.chapterIndex == chapterIndex }
                    chapterNumber != null -> allMcqs.filter { it.chapterNumber == chapterNumber }
                    else -> allMcqs
                }

                val subjectName = candidateItems.firstOrNull()?.let {
                    if (chapterNumber != null || chapterIndex != null) {
                        "${it.subjectName} • ${it.paper} Ch ${it.chapterNumber}"
                    } else {
                        it.subjectName
                    }
                } ?: (allMcqs.firstOrNull()?.subjectName ?: subjectId.replaceFirstChar { it.uppercase() })

                // Retrieve recently seen question texts from latest attempt to avoid immediate repeats
                val latestAttempt = repository.getLatestAttemptForSubject(subjectId)
                val recentlySeenTexts = parseSeenQuestionTexts(latestAttempt?.questionResultsJson)

                val pooledItems = buildPooledSet(candidateItems, recentlySeenTexts)

                if (candidateItems.size < 20 && candidateItems.isNotEmpty()) {
                    note = "${candidateItems.size} questions available in this chapter."
                }

                questions = pooledItems.mapIndexed { idx, item ->
                    QuizQuestion(
                        id = "${item.subjectId}_ch${item.chapterNumber}_${item.mcq.number}_$idx",
                        index = idx + 1,
                        subjectId = item.subjectId,
                        subjectName = item.subjectName,
                        chapterNumber = item.chapterNumber,
                        chapterTitle = item.chapterTitle,
                        paper = item.paper,
                        questionText = item.mcq.questionText,
                        optionA = item.mcq.optionA,
                        optionB = item.mcq.optionB,
                        optionC = item.mcq.optionC,
                        optionD = item.mcq.optionD,
                        correctOption = item.mcq.correctOption,
                        explanation = item.explanation
                    )
                }

                _uiState.update { it.copy(subjectName = subjectName) }
            }

            _uiState.update {
                it.copy(
                    questions = questions,
                    isLoading = false,
                    availabilityNote = note
                )
            }

            startTimer()
        }
    }

    private fun buildPooledSet(
        candidatePool: List<PooledMcqItem>,
        recentlySeenTexts: Set<String>
    ): List<PooledMcqItem> {
        val targetCount = minOf(20, candidatePool.size)
        if (candidatePool.isEmpty()) return emptyList()

        // Strictly pick questions belonging to this candidate pool (no mixing from other chapters)
        val notSeenRecently = candidatePool.filter { it.mcq.questionText !in recentlySeenTexts }.shuffled()
        val seenRecently = candidatePool.filter { it.mcq.questionText in recentlySeenTexts }.shuffled()

        val combined = notSeenRecently + seenRecently
        return combined.take(targetCount)
    }

    private fun parseSeenQuestionTexts(json: String?): Set<String> {
        if (json.isNullOrBlank()) return emptySet()
        return try {
            val arr = JSONArray(json)
            val set = mutableSetOf<String>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val qText = obj.optString("questionText")
                if (qText.isNotBlank()) set.add(qText)
            }
            set
        } catch (e: Exception) {
            emptySet()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    fun selectOption(questionIndex: Int, option: String) {
        if (_uiState.value.isSubmitted) return
        _uiState.update { state ->
            val current = state.selectedAnswers[questionIndex]
            val updated = state.selectedAnswers.toMutableMap()
            if (current == option) {
                // Tapping same option again clears it
                updated.remove(questionIndex)
            } else {
                updated[questionIndex] = option
            }
            state.copy(selectedAnswers = updated)
        }
    }

    fun submitQuiz() {
        timerJob?.cancel()
        val state = _uiState.value
        if (state.questions.isEmpty() || state.isSubmitted) return

        val questions = state.questions
        val answers = state.selectedAnswers
        var correct = 0
        var wrong = 0
        var skipped = 0

        val reviewItems = questions.mapIndexed { idx, q ->
            val selected = answers[idx]
            val isCorrect = selected != null && selected.equals(q.correctOption, ignoreCase = true)
            val isSkipped = selected == null

            if (isCorrect) correct++
            else if (isSkipped) skipped++
            else wrong++

            QuizReviewItem(
                question = q,
                selectedOption = selected,
                isCorrect = isCorrect,
                isSkipped = isSkipped
            )
        }

        val total = questions.size
        val percentage = if (total > 0) (correct * 100) / total else 0

        // Chapter accuracy breakdown
        val chapterGroups = questions.groupBy { it.chapterNumber to it.chapterTitle }
        val chapterAccuracies = chapterGroups.map { (pair, list) ->
            val chCorrect = list.count { q ->
                val ans = answers[q.index - 1]
                ans != null && ans.equals(q.correctOption, ignoreCase = true)
            }
            ChapterAccuracy(
                chapterNumber = pair.first,
                chapterTitle = pair.second,
                correctCount = chCorrect,
                totalCount = list.size
            )
        }.sortedBy { it.chapterNumber }

        val resultData = QuizResultData(
            totalQuestions = total,
            score = correct,
            wrongCount = wrong,
            skippedCount = skipped,
            percentage = percentage,
            timeTakenSeconds = state.elapsedSeconds,
            chapterBreakdowns = chapterAccuracies,
            reviewItems = reviewItems
        )

        _uiState.update {
            it.copy(
                isSubmitted = true,
                result = resultData
            )
        }

        // Persist attempt to Room & apply SM-2 spaced repetition
        viewModelScope.launch(Dispatchers.IO) {
            val profile = repository.getUserProfile().firstOrNull()
            val confirmedDate = profile?.examDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val resultsArray = JSONArray()
            reviewItems.forEach { item ->
                val obj = JSONObject().apply {
                    put("questionId", item.question.id)
                    put("chapterNumber", item.question.chapterNumber)
                    put("chapterTitle", item.question.chapterTitle)
                    put("questionText", item.question.questionText)
                    put("userAnswer", item.selectedOption ?: "")
                    put("correctAnswer", item.question.correctOption)
                    put("isCorrect", item.isCorrect)
                    put("isSkipped", item.isSkipped)
                    put("explanation", item.question.explanation)
                }
                resultsArray.put(obj)
            }

            val attempt = QuizAttemptEntity(
                timestamp = System.currentTimeMillis(),
                attemptDate = confirmedDate,
                subjectId = state.subjectId,
                subjectName = state.subjectName,
                totalQuestions = total,
                score = correct,
                wrongCount = wrong,
                skippedCount = skipped,
                timeTakenSeconds = state.elapsedSeconds,
                questionResultsJson = resultsArray.toString()
            )
            repository.saveQuizAttempt(attempt)

            // SM-2 Spaced Repetition Auto-Update:
            // correct -> GOOD (rating 4), wrong -> AGAIN (rating 0 reset)
            reviewItems.forEach { item ->
                val rating = if (item.isCorrect) Sm2Rating.GOOD else Sm2Rating.AGAIN
                Sm2Algorithm.calculateNextReview(
                    rating = rating,
                    currentRepetitions = if (item.isCorrect) 1 else 0,
                    currentIntervalDays = 1,
                    currentEaseFactor = 2.5f
                )
            }
        }
    }

    fun setReviewFilter(filter: ReviewFilter) {
        _uiState.update { it.copy(reviewFilter = filter) }
    }

    fun retryWrongQuestions() {
        startLearnFromMistakes()
    }

    fun startLearnFromMistakes() {
        val state = _uiState.value
        val result = state.result ?: return
        val wrongReviewItems = result.reviewItems.filter { !it.isCorrect }
        if (wrongReviewItems.isEmpty()) return

        val failedQuestions = wrongReviewItems.map { it.question }
        val weakChapterNumbers = failedQuestions.map { it.chapterNumber }.toSet()

        viewModelScope.launch(Dispatchers.IO) {
            val allMcqs = ContentRepository.getAllMcqsForSubject(appContext, state.subjectId)

            val remedialQuestions = mutableListOf<QuizQuestion>()
            // 1. First add the failed questions directly for reinforcement
            failedQuestions.forEach { q ->
                remedialQuestions.add(q.copy(index = remedialQuestions.size + 1))
            }

            // 2. Pull other questions from the SAME chapters/topics to complete the 20-question set
            val existingTexts = failedQuestions.map { it.questionText }.toSet()
            val categoryPool = allMcqs.filter { it.chapterNumber in weakChapterNumbers && it.mcq.questionText !in existingTexts }.shuffled()
            val generalPool = allMcqs.filter { it.mcq.questionText !in existingTexts && it.chapterNumber !in weakChapterNumbers }.shuffled()

            val combinedPool = categoryPool + generalPool
            val needed = 20 - remedialQuestions.size
            if (needed > 0) {
                combinedPool.take(needed).forEach { item ->
                    val idx = remedialQuestions.size
                    remedialQuestions.add(
                        QuizQuestion(
                            id = "${item.subjectId}_ch${item.chapterNumber}_${item.mcq.number}_remedial_$idx",
                            index = idx + 1,
                            subjectId = item.subjectId,
                            subjectName = item.subjectName,
                            chapterNumber = item.chapterNumber,
                            chapterTitle = item.chapterTitle,
                            paper = item.paper,
                            questionText = item.mcq.questionText,
                            optionA = item.mcq.optionA,
                            optionB = item.mcq.optionB,
                            optionC = item.mcq.optionC,
                            optionD = item.mcq.optionD,
                            correctOption = item.mcq.correctOption,
                            explanation = item.explanation
                        )
                    )
                }
            }

            startQuiz(
                subjectId = state.subjectId,
                chapterNumber = null,
                retryQuestions = remedialQuestions.take(20)
            )
        }
    }

    fun startNextSession() {
        val state = _uiState.value
        val currentCh = state.chapterNumber ?: 1
        val chapters = ContentRepository.getChaptersForSubject(appContext, state.subjectId)
        val nextCh = if (chapters.isNotEmpty()) {
            val next = currentCh + 1
            if (next <= chapters.size) next else 1
        } else null
        startQuiz(state.subjectId, nextCh)
    }

    fun newQuizSet() {
        startQuiz(_uiState.value.subjectId, _uiState.value.chapterNumber)
    }

    fun explainWithAi(question: QuizQuestion) {
        val qId = question.id
        if (_uiState.value.aiExplanations.containsKey(qId)) return

        _uiState.update { it.copy(aiLoadingQuestionId = qId) }
        viewModelScope.launch(Dispatchers.IO) {
            val prompt = """
                Explain this HSC exam question step by step:
                Question: ${question.questionText}
                Options:
                A: ${question.optionA}
                B: ${question.optionB}
                C: ${question.optionC}
                D: ${question.optionD}
                Correct Option: ${question.correctOption}
            """.trimIndent()

            val explanation = AiHelper.askAi(prompt)

            _uiState.update { state ->
                val updated = state.aiExplanations.toMutableMap()
                updated[qId] = explanation
                state.copy(
                    aiExplanations = updated,
                    aiLoadingQuestionId = if (state.aiLoadingQuestionId == qId) null else state.aiLoadingQuestionId
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

class QuizViewModelFactory(
    private val context: Context,
    private val repository: PoruaRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuizViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuizViewModel(context.applicationContext, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: $modelClass")
    }
}
