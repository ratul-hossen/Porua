package com.porua.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val attemptDate: String,
    val subjectId: String,
    val subjectName: String,
    val totalQuestions: Int,
    val score: Int,
    val wrongCount: Int,
    val skippedCount: Int,
    val timeTakenSeconds: Int,
    val questionResultsJson: String
)
