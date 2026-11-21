package com.porua.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.porua.app.data.local.entity.QuizAttemptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizAttemptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: QuizAttemptEntity): Long

    @Query("SELECT * FROM quiz_attempts ORDER BY id DESC")
    fun getAllAttempts(): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE subjectId = :subjectId ORDER BY id DESC")
    fun getAttemptsBySubject(subjectId: String): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts ORDER BY id DESC LIMIT 1")
    suspend fun getLatestAttempt(): QuizAttemptEntity?

    @Query("SELECT * FROM quiz_attempts WHERE subjectId = :subjectId ORDER BY id DESC LIMIT 1")
    suspend fun getLatestAttemptForSubject(subjectId: String): QuizAttemptEntity?
}
