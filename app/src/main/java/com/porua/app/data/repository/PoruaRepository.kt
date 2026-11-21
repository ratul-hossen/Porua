package com.porua.app.data.repository

import com.porua.app.data.local.AppDatabase
import com.porua.app.data.local.ChatHistoryEntity
import com.porua.app.data.local.entity.ChapterEntity
import com.porua.app.data.local.entity.QuestionEntity
import com.porua.app.data.local.entity.QuizAttemptEntity
import com.porua.app.data.local.entity.StudyTaskEntity
import com.porua.app.data.local.entity.SubjectEntity
import com.porua.app.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

class PoruaRepository(private val database: AppDatabase) {

    // User Profile
    fun getUserProfile(): Flow<UserProfileEntity?> = database.userProfileDao().getUserProfile()

    suspend fun ensureInitialData() {
        AppDatabase.ensureInitialData(database)
    }

    suspend fun updateUserProfile(profile: UserProfileEntity) {
        database.userProfileDao().insertOrUpdateProfile(profile)
    }

    // Subjects
    fun getAllSubjects(): Flow<List<SubjectEntity>> = database.subjectDao().getAllSubjects()

    suspend fun updateSubject(subject: SubjectEntity) {
        database.subjectDao().updateSubject(subject)
    }

    // Chapters
    fun getChaptersForSubject(subjectId: String): Flow<List<ChapterEntity>> =
        database.chapterDao().getChaptersForSubject(subjectId)

    suspend fun updateChapter(chapter: ChapterEntity) {
        database.chapterDao().updateChapter(chapter)
    }

    // Questions
    fun getQuestionsByChapter(chapterId: Long): Flow<List<QuestionEntity>> =
        database.questionDao().getQuestionsByChapter(chapterId)

    fun getQuestionsBySubject(subjectId: String): Flow<List<QuestionEntity>> =
        database.questionDao().getQuestionsBySubject(subjectId)

    suspend fun searchQuestions(query: String): List<QuestionEntity> =
        database.questionDao().searchQuestions(query)

    suspend fun updateQuestion(question: QuestionEntity) {
        database.questionDao().updateQuestion(question)
    }

    // Study Tasks (CRUD)
    fun getAllTasks(): Flow<List<StudyTaskEntity>> = database.studyTaskDao().getAllTasks()

    fun getPendingTasks(): Flow<List<StudyTaskEntity>> = database.studyTaskDao().getPendingTasks()

    suspend fun addTask(task: StudyTaskEntity): Long = database.studyTaskDao().insertTask(task)

    suspend fun updateTask(task: StudyTaskEntity) {
        database.studyTaskDao().updateTask(task)
    }

    suspend fun deleteTask(task: StudyTaskEntity) {
        database.studyTaskDao().deleteTask(task)
    }

    suspend fun toggleTaskCompleted(taskId: Long, isCompleted: Boolean) {
        database.studyTaskDao().setTaskCompleted(taskId, isCompleted)
    }

    // Chat History (Room Database)
    fun getAllChats(): Flow<List<ChatHistoryEntity>> = database.chatHistoryDao().getAllChats()

    suspend fun getCachedReply(prompt: String): String? =
        database.chatHistoryDao().getCachedReply(prompt)

    suspend fun saveChat(prompt: String, reply: String): Long =
        database.chatHistoryDao().insertChat(ChatHistoryEntity(prompt = prompt, reply = reply))

    suspend fun clearAllChats() = database.chatHistoryDao().clearAllChats()

    // Quiz Attempts
    suspend fun saveQuizAttempt(attempt: QuizAttemptEntity): Long =
        database.quizAttemptDao().insertAttempt(attempt)

    fun getAllQuizAttempts(): Flow<List<QuizAttemptEntity>> =
        database.quizAttemptDao().getAllAttempts()

    fun getQuizAttemptsBySubject(subjectId: String): Flow<List<QuizAttemptEntity>> =
        database.quizAttemptDao().getAttemptsBySubject(subjectId)

    suspend fun getLatestAttemptForSubject(subjectId: String): QuizAttemptEntity? =
        database.quizAttemptDao().getLatestAttemptForSubject(subjectId)
}
