package com.porua.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.porua.app.data.ai.AiHelper
import com.porua.app.data.auth.AuthDataStore
import com.porua.app.data.local.entity.ChapterEntity
import com.porua.app.data.local.entity.QuestionEntity
import com.porua.app.data.local.entity.QuizAttemptEntity
import com.porua.app.data.local.entity.StudyTaskEntity
import com.porua.app.data.local.entity.SubjectEntity
import com.porua.app.data.local.entity.UserProfileEntity
import com.porua.app.data.repository.PoruaRepository
import com.porua.app.domain.countdown.CountdownResult
import com.porua.app.domain.countdown.ExamDateProvider
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
    val quizAttempts: List<QuizAttemptEntity> = emptyList(),
    val countdownResult: CountdownResult = CountdownResult.Upcoming(
        daysRemaining = 0,
        examYear = 2027,
        examDate = java.time.LocalDate.of(2027, 4, 1),
        formattedTarget = "HSC 2027 • April"
    ),
    val isExamYearConfigured: Boolean = true,

    // Quiz Session State
    val currentQuestions: List<QuestionEntity> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedOption: String? = null,
    val isAnswerChecked: Boolean = false,
    val isAnswerCorrect: Boolean = false,
    val quizSecondsElapsed: Int = 42,
    val sm2RatingRecorded: Sm2Rating? = null,

    // AI Chat State
    val chatMessages: List<ChatMessage> = emptyList(),
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

class PoruaViewModel(
    private val repository: PoruaRepository,
    private val authDataStore: AuthDataStore? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(PoruaUiState())
    val uiState: StateFlow<PoruaUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<String>()
    val eventFlow: SharedFlow<String> = _eventFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.ensureInitialData()
        }
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            repository.getUserProfile().collect { profile ->
                if (profile != null) {
                    val yearInt = profile.examYear.toIntOrNull() ?: ExamDateProvider.getAvailableExamYears().first()
                    val countdown = ExamDateProvider.calculateCountdown(yearInt)
                    _uiState.update {
                        it.copy(
                            userProfile = profile,
                            countdownResult = countdown
                        )
                    }
                }
            }
        }

        if (authDataStore != null) {
            viewModelScope.launch {
                authDataStore.examYear.collect { storedYear ->
                    val isConfigured = !storedYear.isNullOrBlank()
                    _uiState.update { it.copy(isExamYearConfigured = isConfigured) }
                    if (!storedYear.isNullOrBlank() && storedYear != _uiState.value.userProfile.examYear) {
                        setExamYear(storedYear)
                    }
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
            repository.getAllQuizAttempts().collect { attempts ->
                _uiState.update { it.copy(quizAttempts = attempts) }
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

        // Load chat history from Room Database
        viewModelScope.launch {
            repository.getAllChats().collect { savedChats ->
                val messages = mutableListOf<ChatMessage>()
                savedChats.forEach { chat ->
                    messages.add(ChatMessage(id = chat.id * 2, sender = ChatSender.USER, text = chat.prompt))
                    messages.add(ChatMessage(id = chat.id * 2 + 1, sender = ChatSender.PORUA_AI, text = chat.reply, answerSummary = ""))
                }
                _uiState.update { it.copy(chatMessages = messages) }
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

    // AI Chat Interaction (Google & Grok API + Room DB Save + Image Support)
    fun sendChatMessage(
        text: String,
        imageBase64: String? = null,
        mimeType: String = "image/jpeg",
        useGrok: Boolean = false
    ) {
        if (text.isBlank() && imageBase64.isNullOrBlank()) return
        val displayText = if (!imageBase64.isNullOrBlank() && text.isBlank()) "[Attached Image]" else text
        val userMsg = ChatMessage(
            id = System.currentTimeMillis(),
            sender = ChatSender.USER,
            text = displayText
        )

        _uiState.update {
            it.copy(
                chatMessages = it.chatMessages + userMsg,
                isAiTyping = true
            )
        }

        viewModelScope.launch {
            try {
                // 1. Check local offline cache first for instant responses
                val cached = if (imageBase64.isNullOrBlank()) repository.getCachedReply(text) else null
                val aiResponse = if (!cached.isNullOrBlank()) {
                    cached
                } else if (useGrok) {
                    AiHelper.askGrok(text.ifBlank { "Please solve or explain this question in detail." }, imageBase64, mimeType)
                } else {
                    AiHelper.askAi(text.ifBlank { "Please solve or explain this question in detail." }, imageBase64, mimeType)
                }

                // Save conversation to Room Database (Flow will automatically emit updated messages to UI)
                repository.saveChat(prompt = displayText, reply = aiResponse)
            } catch (e: Exception) {
                val fallback = "I encountered an issue processing your question: ${e.localizedMessage ?: "Unknown error"}. Please check your connection."
                repository.saveChat(prompt = displayText, reply = fallback)
            } finally {
                _uiState.update { it.copy(isAiTyping = false) }
            }
        }
    }

    fun setShowCloudConsent(show: Boolean) {
        _uiState.update { it.copy(showCloudConsent = show) }
    }

    /**
     * Starts a completely fresh chat session:
     * - Wipes active chat messages from the screen.
     * - Clears saved chat history from local database.
     * - Clears in-memory query cache so no past context is retained.
     */
    fun startNewChat() {
        viewModelScope.launch {
            repository.clearAllChats()
            AiHelper.clearCache()
            _uiState.update {
                it.copy(
                    chatMessages = emptyList(),
                    isAiTyping = false
                )
            }
            _eventFlow.emit("Started fresh chat session")
        }
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

    fun toggleCloudAi() {
        val updated = _uiState.value.userProfile.copy(
            cloudAiEnabled = !_uiState.value.userProfile.cloudAiEnabled
        )
        viewModelScope.launch {
            repository.updateUserProfile(updated)
            _eventFlow.emit(if (updated.cloudAiEnabled) "Cloud AI enabled." else "Cloud AI disabled.")
        }
    }

    fun toggleSubjectSelected(subjectId: String) {
        viewModelScope.launch {
            val subject = _uiState.value.subjects.find { it.id == subjectId } ?: return@launch
            repository.updateSubject(subject.copy(isSelected = !subject.isSelected))
        }
    }

    fun updateSelectedSubjects(subjectIds: List<String>) {
        viewModelScope.launch {
            _uiState.value.subjects.forEach { subject ->
                val shouldBeSelected = subjectIds.contains(subject.id)
                if (subject.isSelected != shouldBeSelected) {
                    repository.updateSubject(subject.copy(isSelected = shouldBeSelected))
                }
            }
        }
    }

    fun refreshCountdown() {
        val currentYear = _uiState.value.userProfile.examYear.toIntOrNull()
            ?: ExamDateProvider.getAvailableExamYears().first()
        val countdown = ExamDateProvider.calculateCountdown(currentYear)
        val updatedDays = when (countdown) {
            is CountdownResult.Upcoming -> countdown.daysRemaining.toInt()
            is CountdownResult.Today -> 0
            is CountdownResult.Passed -> 0
        }
        val updatedDate = ExamDateProvider.formatExamDate(currentYear)
        _uiState.update { state ->
            state.copy(
                countdownResult = countdown,
                userProfile = state.userProfile.copy(
                    daysRemaining = updatedDays,
                    examDate = updatedDate
                )
            )
        }
    }

    fun setExamYear(year: String) {
        val yearInt = year.toIntOrNull() ?: return
        viewModelScope.launch {
            authDataStore?.saveExamYear(year)
            val currentProfile = _uiState.value.userProfile
            val updatedDate = ExamDateProvider.formatExamDate(yearInt)
            val countdown = ExamDateProvider.calculateCountdown(yearInt)
            val updatedDays = when (countdown) {
                is CountdownResult.Upcoming -> countdown.daysRemaining.toInt()
                is CountdownResult.Today -> 0
                is CountdownResult.Passed -> 0
            }
            val updatedProfile = currentProfile.copy(
                examYear = year,
                examDate = updatedDate,
                daysRemaining = updatedDays
            )
            repository.updateUserProfile(updatedProfile)
            _uiState.update { state ->
                state.copy(
                    userProfile = updatedProfile,
                    countdownResult = countdown,
                    isExamYearConfigured = true
                )
            }
            _eventFlow.emit("Target exam year set to $year!")
        }
    }

    fun updateStudentProfile(
        name: String,
        track: String,
        group: String,
        year: String,
        examDate: String,
        dailyStudyTime: String,
        selectedSubjectIds: List<String>? = null
    ) {
        viewModelScope.launch {
            val yearInt = year.toIntOrNull() ?: ExamDateProvider.getAvailableExamYears().first()
            val countdown = ExamDateProvider.calculateCountdown(yearInt)
            val updatedDays = when (countdown) {
                is CountdownResult.Upcoming -> countdown.daysRemaining.toInt()
                is CountdownResult.Today -> 0
                is CountdownResult.Passed -> 0
            }
            val formattedExamDate = ExamDateProvider.formatExamDate(yearInt)
            authDataStore?.saveExamYear(year)
            val current = _uiState.value.userProfile
            val updated = current.copy(
                name = name.trim(),
                examTrack = track.trim(),
                group = group.trim(),
                examYear = year.trim(),
                examDate = formattedExamDate,
                daysRemaining = updatedDays,
                dailyStudyTime = dailyStudyTime.trim()
            )
            repository.updateUserProfile(updated)
            if (selectedSubjectIds != null) {
                _uiState.value.subjects.forEach { subject ->
                    val shouldBeSelected = selectedSubjectIds.contains(subject.id)
                    if (subject.isSelected != shouldBeSelected) {
                        repository.updateSubject(subject.copy(isSelected = shouldBeSelected))
                    }
                }
            }
            _uiState.update {
                it.copy(
                    userProfile = updated,
                    countdownResult = countdown,
                    isExamYearConfigured = true
                )
            }
            _eventFlow.emit("Profile and exam settings saved successfully!")
        }
    }
}

class PoruaViewModelFactory(
    private val repository: PoruaRepository,
    private val authDataStore: AuthDataStore? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PoruaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PoruaViewModel(repository, authDataStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
