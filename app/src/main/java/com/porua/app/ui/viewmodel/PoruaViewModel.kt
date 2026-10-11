package com.porua.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.porua.app.data.local.entity.ChapterEntity
import com.porua.app.data.local.entity.QuestionEntity
import com.porua.app.data.local.entity.StudyTaskEntity
import com.porua.app.data.local.entity.SubjectEntity
import com.porua.app.data.local.entity.UserProfileEntity
import com.porua.app.data.repository.PoruaRepository
import com.porua.app.domain.sm2.Sm2Algorithm
import com.porua.app.domain.sm2.Sm2Rating
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PoruaUiState(
    val userProfile: UserProfileEntity = UserProfileEntity(),
    val subjects: List<SubjectEntity> = emptyList(),
    val currentSubjectChapters: List<ChapterEntity> = emptyList(),
    val selectedSubjectId: String = "chem",
    val studyTasks: List<StudyTaskEntity> = emptyList(),

    // Quiz Session State
    val currentQuestions: List<QuestionEntity> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedOption: String? = null,
    val isAnswerChecked: Boolean = false,
    val isAnswerCorrect: Boolean = false,
    val quizSecondsElapsed: Int = 42,
    val sm2RatingRecorded: Sm2Rating? = null,

    // AI Chat State
    val chatMessages: List<ChatMessage> = listOf(
        ChatMessage(
            id = 1,
            sender = ChatSender.USER,
            text = "How do I find the emf of a Daniell cell? I keep getting the sign wrong."
        ),
        ChatMessage(
            id = 2,
            sender = ChatSender.PORUA_AI,
            text = "Step 1 — Anode (oxidation): Zn -> Zn²⁺ + 2e⁻, E° = -0.76 V\nStep 2 — Cathode (reduction): Cu²⁺ + 2e⁻ -> Cu, E° = +0.34 V\nStep 3 — E°cell = E°cathode - E°anode = 0.34 - (-0.76) = 1.10 V",
            answerSummary = "1.10 V",
            syllabusNote = "NCTB HSC Chemistry 2nd paper, Ch 4"
        )
    ),
    val isAiTyping: Boolean = false,
    val showCloudConsent: Boolean = false,

    // Snapped OCR State
    val snappedQuestionText: String = "Calculate the standard emf of the cell Zn | Zn²⁺ (1 M) || Cu²⁺ (1 M) | Cu, given E°(Zn²⁺/Zn) = -0.76 V and E°(Cu²⁺/Cu) = +0.34 V. (a) 0.42 V (b) 1.10 V (c) -1.10 V (d) 0.76 V",
    val similarOfflineQuestions: List<QuestionEntity> = emptyList()
)

data class ChatMessage(
    val id: Long,
    val sender: ChatSender,
    val text: String,
    val answerSummary: String = "",
    val syllabusNote: String = ""
)

enum class ChatSender { USER, PORUA_AI }

class PoruaViewModel(private val repository: PoruaRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PoruaUiState())
    val uiState: StateFlow<PoruaUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<String>()
    val eventFlow: SharedFlow<String> = _eventFlow.asSharedFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            repository.getUserProfile().collect { profile ->
                if (profile != null) {
                    _uiState.update { it.copy(userProfile = profile) }
                }
            }
        }

        viewModelScope.launch {
            repository.getAllSubjects().collect { subjects ->
                _uiState.update { it.copy(subjects = subjects) }
            }
        }

        viewModelScope.launch {
            repository.getChaptersForSubject("chem").collect { chapters ->
                _uiState.update { it.copy(currentSubjectChapters = chapters) }
            }
        }

        viewModelScope.launch {
            repository.getAllTasks().collect { tasks ->
                _uiState.update { it.copy(studyTasks = tasks) }
            }
        }

        viewModelScope.launch {
            repository.getQuestionsBySubject("chem").collect { questions ->
                _uiState.update {
                    it.copy(
                        currentQuestions = questions,
                        similarOfflineQuestions = questions.take(3)
                    )
                }
            }
        }
    }

    // Task CRUD
    fun toggleTask(taskId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleTaskCompleted(taskId, !isCompleted)
            _eventFlow.emit(if (!isCompleted) "Task completed! Great job." else "Task marked pending.")
        }
    }

    fun addNewTask(title: String, subject: String, durationMin: Int, priority: String, category: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val task = StudyTaskEntity(
                title = title.trim(),
                subject = subject,
                durationMinutes = durationMin,
                questionCount = 10,
                isCompleted = false,
                scheduledDate = "Today",
                priority = priority,
                category = category
            )
            repository.addTask(task)
            _eventFlow.emit("New study task created!")
        }
    }

    fun deleteTask(task: StudyTaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            _eventFlow.emit("Task removed.")
        }
    }

    // Quiz / Diagnostic Interaction
    fun selectQuizOption(option: String) {
        if (!_uiState.value.isAnswerChecked) {
            _uiState.update { it.copy(selectedOption = option) }
        }
    }

    fun checkCurrentAnswer() {
        val state = _uiState.value
        val questions = state.currentQuestions
        if (questions.isEmpty()) return
        val currentQ = questions[state.currentQuestionIndex.coerceIn(0, questions.lastIndex)]
        val isCorrect = state.selectedOption == currentQ.correctOption

        _uiState.update {
            it.copy(
                isAnswerChecked = true,
                isAnswerCorrect = isCorrect
            )
        }
    }

    fun applySm2Rating(rating: Sm2Rating) {
        val state = _uiState.value
        val questions = state.currentQuestions
        if (questions.isEmpty()) return
        val currentQ = questions[state.currentQuestionIndex.coerceIn(0, questions.lastIndex)]

        val sm2Result = Sm2Algorithm.calculateNextReview(
            rating = rating,
            currentRepetitions = currentQ.repetitions,
            currentIntervalDays = currentQ.intervalDays,
            currentEaseFactor = currentQ.easeFactor
        )

        viewModelScope.launch {
            repository.updateQuestion(
                currentQ.copy(
                    repetitions = sm2Result.repetitions,
                    intervalDays = sm2Result.intervalDays,
                    easeFactor = sm2Result.easeFactor,
                    lastReviewedTimestamp = System.currentTimeMillis(),
                    nextReviewTimestamp = sm2Result.nextReviewTimestamp
                )
            )
            _uiState.update { it.copy(sm2RatingRecorded = rating) }
            _eventFlow.emit("SM-2 Scheduled: Review in ${rating.intervalDescription}")
        }
    }

    fun nextQuestion() {
        val state = _uiState.value
        val nextIdx = state.currentQuestionIndex + 1
        if (nextIdx < state.currentQuestions.size) {
            _uiState.update {
                it.copy(
                    currentQuestionIndex = nextIdx,
                    selectedOption = null,
                    isAnswerChecked = false,
                    isAnswerCorrect = false,
                    sm2RatingRecorded = null
                )
            }
        }
    }

    // AI Chat Interaction
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(
            id = System.currentTimeMillis(),
            sender = ChatSender.USER,
            text = text
        )

        _uiState.update {
            it.copy(
                chatMessages = it.chatMessages + userMsg,
                isAiTyping = true
            )
        }

        // Simulate intelligent offline / Gemini tutor response
        viewModelScope.launch {
            kotlinx.coroutines.delay(1200)
            val aiMsg = ChatMessage(
                id = System.currentTimeMillis() + 1,
                sender = ChatSender.PORUA_AI,
                text = "Here is a practice question on this topic:\n\nFor the cell Cu | Cu²⁺ (1 M) || Ag⁺ (1 M) | Ag, calculate E°cell given E°(Cu²⁺/Cu) = +0.34 V and E°(Ag⁺/Ag) = +0.80 V.\n\nE°cell = E°cathode - E°anode = 0.80 - 0.34 = +0.46 V.",
                answerSummary = "0.46 V",
                syllabusNote = "NCTB HSC Chemistry 2nd paper, Ch 4"
            )
            _uiState.update {
                it.copy(
                    chatMessages = it.chatMessages + aiMsg,
                    isAiTyping = false
                )
            }
        }
    }

    fun setShowCloudConsent(show: Boolean) {
        _uiState.update { it.copy(showCloudConsent = show) }
    }

    // Settings & Exam updates
    fun updateExamInfo(track: String, group: String, date: String) {
        viewModelScope.launch {
            val updated = _uiState.value.userProfile.copy(
                examTrack = track,
                group = group,
                examDate = date
            )
            repository.updateUserProfile(updated)
            _eventFlow.emit("Exam profile updated successfully.")
        }
    }

    fun toggleOfflineMode() {
        val updated = _uiState.value.userProfile.copy(
            isOfflineMode = !_uiState.value.userProfile.isOfflineMode
        )
        viewModelScope.launch {
            repository.updateUserProfile(updated)
            _eventFlow.emit(if (updated.isOfflineMode) "Offline mode enabled." else "Online sync enabled.")
        }
    }
}

class PoruaViewModelFactory(private val repository: PoruaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PoruaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PoruaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
