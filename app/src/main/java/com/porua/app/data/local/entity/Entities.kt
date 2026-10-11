package com.porua.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val paper: String,
    val chaptersCount: Int,
    val questionsCount: Int,
    val mastery: Int,
    val iconType: String,
    val isSelected: Boolean = true
)

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: String,
    val chapterNumber: Int,
    val name: String,
    val topicsCount: Int,
    val questionsCount: Int,
    val mastery: Int,
    val lastStudiedDaysAgo: Int,
    val isBookmarked: Boolean = false
)

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chapterId: Long,
    val subjectId: String,
    val topicName: String,
    val difficulty: String, // "Easy", "Medium", "Hard"
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: String, // "A", "B", "C", "D"
    val explanation: String,
    val syllabusReference: String,
    // SM-2 Spaced Repetition parameters
    val easeFactor: Float = 2.5f,
    val intervalDays: Int = 1,
    val repetitions: Int = 0,
    val lastReviewedTimestamp: Long = 0L,
    val nextReviewTimestamp: Long = 0L
)

@Entity(tableName = "study_tasks")
data class StudyTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String,
    val durationMinutes: Int,
    val questionCount: Int,
    val isCompleted: Boolean = false,
    val scheduledDate: String = "",
    val priority: String = "Medium", // High, Medium, Low
    val category: String = "Revise" // Revise, Practice, Learn
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Ratul",
    val examTrack: String = "HSC",
    val examYear: String = "2027",
    val group: String = "Science",
    val examDate: String = "12 Apr 2027",
    val daysRemaining: Int = 186,
    val examReadyPercentage: Int = 58,
    val streakDays: Int = 12,
    val dailyStudyTime: String = "1 hour",
    val isOfflineMode: Boolean = true,
    val cloudAiEnabled: Boolean = false
)
