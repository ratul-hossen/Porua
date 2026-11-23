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
    val totalCompletedQuestions: Int = 0,
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
    val isDarkMode: Boolean = true,

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
                    val streamSubjects = com.porua.app.data.content.ContentRepository.getSubjectsForGroup(profile.group).map { cs ->
                        val existingSelection = _uiState.value.subjects.find { it.id == cs.id }?.isSelected ?: true
                        val totalQs = com.porua.app.data.content.ContentRepository.getTotalQuestionsForSubject(cs.id)
                        SubjectEntity(
                            id = cs.id,
                            name = cs.name,
                            paper = if (cs.group == "Common") "Common (Mandatory)" else "${cs.group} Stream",
                            chaptersCount = cs.chapterCount,
                            questionsCount = totalQs,
                            mastery = 0,
                            iconType = cs.iconType,
                            isSelected = existingSelection
                        )
                    }
                    _uiState.update {
                        it.copy(
                            userProfile = profile,
                            countdownResult = countdown,
                            subjects = streamSubjects
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
                if (_uiState.value.subjects.isEmpty()) {
                    _uiState.update { it.copy(subjects = subjects) }
                }
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
                val group = _uiState.value.userProfile.group.ifBlank { "Science" }
                val totalSyllabusQuestions = com.porua.app.data.content.ContentRepository.getTotalQuestionsForGroup(group)

                val completedQuestionKeys = mutableSetOf<String>()
                attempts.forEach { attempt ->
                    try {
                        val arr = org.json.JSONArray(attempt.questionResultsJson)
                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)
                            val qText = obj.optString("questionText", "").trim().lowercase()
                            val chNum = obj.optInt("chapterNumber", 1)
                            val qKey = if (qText.isNotBlank()) {
                                "${attempt.subjectId.lowercase()}_${chNum}_$qText"
                            } else {
                                "${attempt.subjectId.lowercase()}_${chNum}_q$i"
                            }
                            completedQuestionKeys.add(qKey)
                        }
                    } catch (_: Exception) {}
                }

                // Overall Readiness: reaches 100% only when ALL MCQs of the entire syllabus are practiced!
                val totalPracticed = completedQuestionKeys.size
                val readiness = ((totalPracticed * 100) / totalSyllabusQuestions).coerceIn(0, 100)
                val updatedProfile = _uiState.value.userProfile.copy(
                    examReadyPercentage = readiness
                )

                // Subject Mastery: reaches 100% only when ALL MCQs of that subject are practiced!
                val updatedSubjects = _uiState.value.subjects.map { subj ->
                    val totalQsInSubj = com.porua.app.data.content.ContentRepository.getTotalQuestionsForSubject(subj.id)
                    val practicedInSubj = completedQuestionKeys.count { key ->
                        val subPart = key.substringBefore("_")
                        subPart == subj.id.lowercase() ||
                        subPart.contains(subj.id.lowercase()) ||
                        subj.id.lowercase().contains(subPart) ||
                        subj.name.lowercase().startsWith(subPart)
                    }
                    val masteryPct = ((practicedInSubj * 100) / totalQsInSubj.coerceAtLeast(1)).coerceIn(0, 100)
                    subj.copy(
                        questionsCount = totalQsInSubj,
                        mastery = masteryPct
                    )
                }

                _uiState.update { state ->
                    state.copy(
                        quizAttempts = attempts,
                        userProfile = updatedProfile,
                        subjects = if (updatedSubjects.isNotEmpty()) updatedSubjects else state.subjects,
                        totalCompletedQuestions = totalPracticed
                    )
                }
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

        val historyTurns = _uiState.value.chatMessages
            .takeLast(6)
            .filter { it.text.isNotBlank() && !it.text.startsWith("⚠️") }
            .map { Pair(if (it.sender == ChatSender.USER) "user" else "model", it.text) }

        _uiState.update {
            it.copy(
                chatMessages = it.chatMessages + userMsg,
                isAiTyping = true
            )
        }

        viewModelScope.launch {
            try {
                // 1. Check local offline cache only when NO live AI key is configured
                val cached = if (imageBase64.isNullOrBlank() && !AiHelper.hasAnyApiKey()) {
                    repository.getCachedReply(text)
                } else null

                val aiResponse = if (!cached.isNullOrBlank()) {
                    cached
                } else if (useGrok) {
                    AiHelper.askGrok(text.ifBlank { "Please solve or explain this question in detail." }, imageBase64, mimeType, historyTurns)
                } else {
                    AiHelper.askAi(text.ifBlank { "Please solve or explain this question in detail." }, imageBase64, mimeType, historyTurns)
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
            regenerateStudyPlanForGoal(dailyStudyTime, group)
            _eventFlow.emit("Profile and exam settings saved successfully!")
        }
    }

    fun toggleTheme() {
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    fun regenerateStudyPlanForGoal(goal: String, group: String) {
        viewModelScope.launch {
            val totalMinutes = when {
                goal.contains("30") -> 30
                goal.contains("2") || goal.contains("120") -> 120
                goal.contains("3") || goal.contains("180") -> 180
                else -> 60
            }

            val subjects = com.porua.app.data.content.ContentRepository.getSubjectsForGroup(group).ifEmpty {
                com.porua.app.data.content.ContentRepository.getSubjectsForGroup("Science")
            }

            val attempts = _uiState.value.quizAttempts

            fun getNextChapterNumber(subId: String): Int {
                var maxCh = 0
                attempts.forEach { att ->
                    if (att.subjectId.contains(subId, ignoreCase = true) || subId.contains(att.subjectId, ignoreCase = true)) {
                        try {
                            val arr = org.json.JSONArray(att.questionResultsJson)
                            for (i in 0 until arr.length()) {
                                val ch = arr.getJSONObject(i).optInt("chapterNumber", 1)
                                if (ch > maxCh) maxCh = ch
                            }
                        } catch (_: Exception) {}
                    }
                }
                return maxCh + 1
            }

            fun getChapterTitle(subName: String, chNum: Int): String {
                return when {
                    subName.contains("Phys", ignoreCase = true) -> when (chNum) {
                        1 -> "Physical World and Measurement"
                        2 -> "Vector"
                        3 -> "Dynamics"
                        4 -> "Newtonian Mechanics"
                        5 -> "Work, Energy and Power"
                        6 -> "Gravity and Gravitation"
                        7 -> "Structural Properties of Matter"
                        8 -> "Periodic Motion"
                        9 -> "Waves"
                        10 -> "Ideal Gas and Kinetic Theory"
                        else -> "Ch $chNum Advanced Practice"
                    }
                    subName.contains("Chem", ignoreCase = true) -> when (chNum) {
                        1 -> "Environmental Chemistry"
                        2 -> "Organic Chemistry"
                        3 -> "Quantitative Chemistry"
                        4 -> "Chemical Changes"
                        5 -> "Economic Chemistry"
                        else -> "Ch $chNum Advanced Practice"
                    }
                    subName.contains("Bio", ignoreCase = true) -> when (chNum) {
                        1 -> "Cell and its Structure"
                        2 -> "Cell Division"
                        3 -> "Cell Chemistry"
                        4 -> "Microorganisms"
                        5 -> "Algae and Fungi"
                        else -> "Ch $chNum Advanced Practice"
                    }
                    subName.contains("Math", ignoreCase = true) -> when (chNum) {
                        1 -> "Matrices and Determinants"
                        2 -> "Vectors"
                        3 -> "Straight Line"
                        4 -> "Circle"
                        5 -> "Permutation and Combination"
                        else -> "Ch $chNum Advanced Practice"
                    }
                    subName.contains("ICT", ignoreCase = true) -> when (chNum) {
                        1 -> "Information and Communication Technology"
                        2 -> "Communication Systems and Networking"
                        3 -> "Number Systems and Digital Devices"
                        4 -> "Web Design and HTML"
                        5 -> "Programming Language (C)"
                        6 -> "Database Management System"
                        else -> "Ch $chNum Advanced Practice"
                    }
                    subName.contains("Eng", ignoreCase = true) -> when (chNum) {
                        1 -> "People Making History"
                        2 -> "Dreams & Literature"
                        3 -> "Lifestyle & Health"
                        4 -> "Human Relationships"
                        else -> "Ch $chNum Advanced Practice"
                    }
                    subName.contains("Acc", ignoreCase = true) -> when (chNum) {
                        1 -> "Introduction to Accounting"
                        2 -> "Transactions and Accounts"
                        3 -> "Double Entry System"
                        4 -> "Trial Balance"
                        else -> "Ch $chNum Advanced Practice"
                    }
                    else -> "Ch $chNum Foundation"
                }
            }

            val s0 = subjects.getOrNull(0) ?: subjects.first()
            val s1 = subjects.getOrNull(1) ?: subjects.first()
            val s2 = subjects.getOrNull(2) ?: subjects.first()
            val s3 = subjects.getOrNull(3) ?: subjects.first()

            val ch0 = getNextChapterNumber(s0.id)
            val ch1 = getNextChapterNumber(s1.id)
            val ch2 = getNextChapterNumber(s2.id)
            val ch3 = getNextChapterNumber(s3.id)

            val tasks = when (totalMinutes) {
                30 -> listOf(
                    StudyTaskEntity(id = 1, title = "${s0.name}: Ch $ch0 - ${getChapterTitle(s0.name, ch0)}", subject = s0.name, durationMinutes = 15, questionCount = 10, isCompleted = false, scheduledDate = "Today", priority = "High", category = "Practice"),
                    StudyTaskEntity(id = 2, title = "${s1.name}: Ch $ch1 - ${getChapterTitle(s1.name, ch1)}", subject = s1.name, durationMinutes = 15, questionCount = 10, isCompleted = false, scheduledDate = "Today", priority = "Medium", category = "Revise")
                )
                120 -> listOf(
                    StudyTaskEntity(id = 1, title = "${s2.name}: Ch $ch2 - ${getChapterTitle(s2.name, ch2)}", subject = s2.name, durationMinutes = 40, questionCount = 20, isCompleted = false, scheduledDate = "Today", priority = "High", category = "Practice"),
                    StudyTaskEntity(id = 2, title = "${s3.name}: Ch $ch3 - ${getChapterTitle(s3.name, ch3)}", subject = s3.name, durationMinutes = 35, questionCount = 15, isCompleted = false, scheduledDate = "Today", priority = "High", category = "Practice"),
                    StudyTaskEntity(id = 3, title = "${s0.name}: Ch $ch0 - ${getChapterTitle(s0.name, ch0)}", subject = s0.name, durationMinutes = 25, questionCount = 15, isCompleted = false, scheduledDate = "Today", priority = "Medium", category = "Learn"),
                    StudyTaskEntity(id = 4, title = "${s1.name}: Ch $ch1 - ${getChapterTitle(s1.name, ch1)}", subject = s1.name, durationMinutes = 20, questionCount = 10, isCompleted = false, scheduledDate = "Today", priority = "Low", category = "Revise")
                )
                180 -> listOf(
                    StudyTaskEntity(id = 1, title = "${s2.name}: Ch $ch2 - ${getChapterTitle(s2.name, ch2)}", subject = s2.name, durationMinutes = 55, questionCount = 25, isCompleted = false, scheduledDate = "Today", priority = "High", category = "Practice"),
                    StudyTaskEntity(id = 2, title = "${s3.name}: Ch $ch3 - ${getChapterTitle(s3.name, ch3)}", subject = s3.name, durationMinutes = 50, questionCount = 25, isCompleted = false, scheduledDate = "Today", priority = "High", category = "Practice"),
                    StudyTaskEntity(id = 3, title = "${s0.name}: Ch $ch0 - ${getChapterTitle(s0.name, ch0)}", subject = s0.name, durationMinutes = 45, questionCount = 20, isCompleted = false, scheduledDate = "Today", priority = "Medium", category = "Learn"),
                    StudyTaskEntity(id = 4, title = "${s1.name}: Ch $ch1 - ${getChapterTitle(s1.name, ch1)}", subject = s1.name, durationMinutes = 30, questionCount = 15, isCompleted = false, scheduledDate = "Today", priority = "Low", category = "Revise")
                )
                else -> listOf(
                    StudyTaskEntity(id = 1, title = "${s2.name}: Ch $ch2 - ${getChapterTitle(s2.name, ch2)}", subject = s2.name, durationMinutes = 25, questionCount = 15, isCompleted = false, scheduledDate = "Today", priority = "High", category = "Practice"),
                    StudyTaskEntity(id = 2, title = "${s3.name}: Ch $ch3 - ${getChapterTitle(s3.name, ch3)}", subject = s3.name, durationMinutes = 20, questionCount = 10, isCompleted = false, scheduledDate = "Today", priority = "High", category = "Revise"),
                    StudyTaskEntity(id = 3, title = "${s0.name}: Ch $ch0 - ${getChapterTitle(s0.name, ch0)}", subject = s0.name, durationMinutes = 15, questionCount = 10, isCompleted = false, scheduledDate = "Today", priority = "Medium", category = "Learn")
                )
            }

            tasks.forEach { repository.addTask(it) }
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
