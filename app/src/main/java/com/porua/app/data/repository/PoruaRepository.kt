package com.porua.app.data.repository

import com.porua.app.data.local.AppDatabase
import com.porua.app.data.local.entity.ChapterEntity
import com.porua.app.data.local.entity.QuestionEntity
import com.porua.app.data.local.entity.StudyTaskEntity
import com.porua.app.data.local.entity.SubjectEntity
import com.porua.app.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

class PoruaRepository(private val database: AppDatabase) {

    // User Profile
    fun getUserProfile(): Flow<UserProfileEntity?> = database.userProfileDao().getUserProfile()

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
}
