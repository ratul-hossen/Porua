package com.porua.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.porua.app.data.local.dao.ChapterDao
import com.porua.app.data.local.dao.QuestionDao
import com.porua.app.data.local.dao.StudyTaskDao
import com.porua.app.data.local.dao.SubjectDao
import com.porua.app.data.local.dao.UserProfileDao
import com.porua.app.data.local.entity.ChapterEntity
import com.porua.app.data.local.entity.QuestionEntity
import com.porua.app.data.local.entity.StudyTaskEntity
import com.porua.app.data.local.entity.SubjectEntity
import com.porua.app.data.local.entity.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import com.porua.app.data.local.dao.QuizAttemptDao
import com.porua.app.data.local.entity.QuizAttemptEntity
import com.porua.app.domain.countdown.CountdownResult
import com.porua.app.domain.countdown.ExamDateProvider
import kotlinx.coroutines.launch

@Database(
    entities = [
        SubjectEntity::class,
        ChapterEntity::class,
        QuestionEntity::class,
        StudyTaskEntity::class,
        UserProfileEntity::class,
        ChatHistoryEntity::class,
        QuizAttemptEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun subjectDao(): SubjectDao
    abstract fun chapterDao(): ChapterDao
    abstract fun questionDao(): QuestionDao
    abstract fun studyTaskDao(): StudyTaskDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun chatHistoryDao(): ChatHistoryDao
    abstract fun quizAttemptDao(): QuizAttemptDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS chat_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        prompt TEXT NOT NULL,
                        reply TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Clear any leftover mock/dummy test values from prior runs
                db.execSQL("UPDATE user_profile SET examReadyPercentage = 0, streakDays = 0 WHERE id = 1 AND (streakDays = 12 OR examReadyPercentage = 58)")
                db.execSQL("UPDATE subjects SET mastery = 0")
                db.execSQL("UPDATE chapters SET mastery = 0, lastStudiedDaysAgo = 0")
                db.execSQL("UPDATE study_tasks SET isCompleted = 0")
            }
        }

        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS quiz_attempts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        attemptDate TEXT NOT NULL,
                        subjectId TEXT NOT NULL,
                        subjectName TEXT NOT NULL,
                        totalQuestions INTEGER NOT NULL,
                        score INTEGER NOT NULL,
                        wrongCount INTEGER NOT NULL,
                        skippedCount INTEGER NOT NULL,
                        timeTakenSeconds INTEGER NOT NULL,
                        questionResultsJson TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "porua_offline_db"
                )
                    .addCallback(DatabaseCallback())
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                populateInitialDataDirect(db)
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                populateInitialDataDirect(db)
            }
        }

        private fun populateInitialDataDirect(db: SupportSQLiteDatabase) {
            db.beginTransaction()
            try {
                // 1. Initial User Profile (0% readiness & 0 day streak for fresh install)
                val defaultYear = ExamDateProvider.getAvailableExamYears().first()
                val initialCountdown = ExamDateProvider.calculateCountdown(defaultYear)
                val initialDays = when (initialCountdown) {
                    is CountdownResult.Upcoming -> initialCountdown.daysRemaining.toInt()
                    else -> 0
                }
                val formattedExamDate = ExamDateProvider.formatExamDate(defaultYear)
                db.execSQL(
                    """
                    INSERT OR REPLACE INTO user_profile (id, name, examTrack, examYear, "group", examDate, daysRemaining, examReadyPercentage, streakDays, dailyStudyTime, isOfflineMode, cloudAiEnabled)
                    VALUES (1, '', 'HSC', '$defaultYear', 'Science', '$formattedExamDate', $initialDays, 0, 0, '1 hour', 1, 0)
                    """.trimIndent()
                )

                // 2. Initial Subjects (0% initial mastery until student practices)
                db.execSQL("INSERT OR REPLACE INTO subjects (id, name, paper, chaptersCount, questionsCount, mastery, iconType, isSelected) VALUES ('chem', 'Chemistry', '2nd paper', 12, 312, 0, 'flask', 1)")
                db.execSQL("INSERT OR REPLACE INTO subjects (id, name, paper, chaptersCount, questionsCount, mastery, iconType, isSelected) VALUES ('phy', 'Physics', '1st & 2nd paper', 10, 280, 0, 'atom', 1)")
                db.execSQL("INSERT OR REPLACE INTO subjects (id, name, paper, chaptersCount, questionsCount, mastery, iconType, isSelected) VALUES ('math', 'Higher Math', '1st & 2nd paper', 10, 260, 0, 'sigma', 1)")
                db.execSQL("INSERT OR REPLACE INTO subjects (id, name, paper, chaptersCount, questionsCount, mastery, iconType, isSelected) VALUES ('eng', 'English', 'Units 1-8', 8, 190, 0, 'text', 1)")
                db.execSQL("INSERT OR REPLACE INTO subjects (id, name, paper, chaptersCount, questionsCount, mastery, iconType, isSelected) VALUES ('bio', 'Biology', 'Botany & Zoology', 12, 240, 0, 'leaf', 0)")
                db.execSQL("INSERT OR REPLACE INTO subjects (id, name, paper, chaptersCount, questionsCount, mastery, iconType, isSelected) VALUES ('ict', 'ICT', 'Complete syllabus', 6, 150, 0, 'computer', 0)")

                // 3. Chapters (0% initial mastery)
                db.execSQL("INSERT OR REPLACE INTO chapters (id, subjectId, chapterNumber, name, topicsCount, questionsCount, mastery, lastStudiedDaysAgo, isBookmarked) VALUES (1, 'chem', 1, 'Environmental Chemistry', 8, 26, 0, 0, 0)")
                db.execSQL("INSERT OR REPLACE INTO chapters (id, subjectId, chapterNumber, name, topicsCount, questionsCount, mastery, lastStudiedDaysAgo, isBookmarked) VALUES (2, 'chem', 2, 'Organic Chemistry', 14, 48, 0, 0, 1)")
                db.execSQL("INSERT OR REPLACE INTO chapters (id, subjectId, chapterNumber, name, topicsCount, questionsCount, mastery, lastStudiedDaysAgo, isBookmarked) VALUES (3, 'chem', 3, 'Quantitative Chemistry', 9, 40, 0, 0, 0)")
                db.execSQL("INSERT OR REPLACE INTO chapters (id, subjectId, chapterNumber, name, topicsCount, questionsCount, mastery, lastStudiedDaysAgo, isBookmarked) VALUES (4, 'chem', 4, 'Electrochemistry', 7, 32, 0, 0, 1)")
                db.execSQL("INSERT OR REPLACE INTO chapters (id, subjectId, chapterNumber, name, topicsCount, questionsCount, mastery, lastStudiedDaysAgo, isBookmarked) VALUES (5, 'chem', 5, 'Economic Chemistry', 6, 20, 0, 0, 0)")

                // 4. Questions
                db.execSQL(
                    """
                    INSERT OR REPLACE INTO questions (id, chapterId, subjectId, topicName, difficulty, questionText, optionA, optionB, optionC, optionD, correctOption, explanation, syllabusReference, easeFactor, intervalDays, repetitions, lastReviewedTimestamp, nextReviewTimestamp)
                    VALUES (1, 4, 'chem', 'Electrode potential', 'Medium', 'The standard hydrogen electrode (SHE) potential at 25 °C is...', '1.00 V', '0.00 V', '-0.76 V', '+0.34 V', 'B', 'SHE is the reference electrode. Every other electrode potential is measured against it, so its value is fixed at 0.00 V by definition.', 'NCTB HSC Chemistry 2nd paper, Ch 4', 2.5, 1, 0, 0, 0)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT OR REPLACE INTO questions (id, chapterId, subjectId, topicName, difficulty, questionText, optionA, optionB, optionC, optionD, correctOption, explanation, syllabusReference, easeFactor, intervalDays, repetitions, lastReviewedTimestamp, nextReviewTimestamp)
                    VALUES (2, 4, 'chem', 'Daniell cell emf', 'Medium', 'Calculate the standard EMF of the cell Zn | Zn²⁺ (1 M) || Cu²⁺ (1 M) | Cu, given E°(Zn²⁺/Zn) = -0.76 V and E°(Cu²⁺/Cu) = +0.34 V.', '0.42 V', '1.10 V', '-1.10 V', '0.76 V', 'B', 'E°cell = E°cathode - E°anode = 0.34 - (-0.76) = 1.10 V. Zn acts as anode and Cu acts as cathode.', 'NCTB HSC Chemistry 2nd paper, Ch 4', 2.5, 1, 0, 0, 0)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT OR REPLACE INTO questions (id, chapterId, subjectId, topicName, difficulty, questionText, optionA, optionB, optionC, optionD, correctOption, explanation, syllabusReference, easeFactor, intervalDays, repetitions, lastReviewedTimestamp, nextReviewTimestamp)
                    VALUES (3, 2, 'chem', 'Alcohols', 'Hard', 'Which reagent is used to differentiate between primary, secondary, and tertiary alcohols?', 'Tollens'' reagent', 'Lucas reagent (conc. HCl + anh. ZnCl₂)', 'Fehling''s solution', 'Grignard reagent', 'B', 'Lucas test differentiates alcohols based on their reactivity with concentrated HCl and anhydrous ZnCl₂ (tertiary reacts immediately, secondary in 5 min, primary requires heating).', 'NCTB HSC Chemistry 2nd paper, Ch 2', 2.5, 1, 0, 0, 0)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT OR REPLACE INTO questions (id, chapterId, subjectId, topicName, difficulty, questionText, optionA, optionB, optionC, optionD, correctOption, explanation, syllabusReference, easeFactor, intervalDays, repetitions, lastReviewedTimestamp, nextReviewTimestamp)
                    VALUES (4, 4, 'chem', 'Galvanic cell notation', 'Hard', 'In IUPAC cell notation, which part of the electrochemical cell is written on the extreme left?', 'Cathode metal', 'Anode metal / Oxidation electrode', 'Salt bridge', 'Electrolyte solution of cathode', 'B', 'According to IUPAC convention, the anode (oxidation half-cell) is always written on the left, and the cathode (reduction half-cell) is on the right.', 'NCTB HSC Chemistry 2nd paper, Ch 4', 2.5, 1, 0, 0, 0)
                    """.trimIndent()
                )

                // 5. Initial Study Tasks (0 completed initially)
                db.execSQL("INSERT OR REPLACE INTO study_tasks (id, title, subject, durationMinutes, questionCount, isCompleted, scheduledDate, priority, category) VALUES (1, 'Organic Chemistry basics', 'Chemistry', 15, 5, 0, 'Today', 'High', 'Learn')")
                db.execSQL("INSERT OR REPLACE INTO study_tasks (id, title, subject, durationMinutes, questionCount, isCompleted, scheduledDate, priority, category) VALUES (2, 'Electrode potential', 'Chemistry', 20, 10, 0, 'Today', 'High', 'Practice')")
                db.execSQL("INSERT OR REPLACE INTO study_tasks (id, title, subject, durationMinutes, questionCount, isCompleted, scheduledDate, priority, category) VALUES (3, 'Vectors basics', 'Physics', 10, 5, 0, 'Today', 'Medium', 'Learn')")

                db.setTransactionSuccessful()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                db.endTransaction()
            }
        }

        suspend fun ensureInitialData(database: AppDatabase) = kotlinx.coroutines.withContext(Dispatchers.IO) {
            try {
                if (database.userProfileDao().getProfileCount() == 0) {
                    populateInitialData(database)
                } else {
                    // Automatically sanitize stale mock data on existing installs without requiring manual app data wipe
                    val db = database.openHelper.writableDatabase
                    db.execSQL("UPDATE user_profile SET examReadyPercentage = 0, streakDays = 0 WHERE id = 1 AND (streakDays = 12 OR examReadyPercentage = 58)")
                    db.execSQL("UPDATE subjects SET mastery = 0 WHERE mastery > 0 AND NOT EXISTS (SELECT 1 FROM questions WHERE lastReviewedTimestamp > 0)")
                    db.execSQL("UPDATE chapters SET mastery = 0 WHERE mastery > 0 AND NOT EXISTS (SELECT 1 FROM questions WHERE lastReviewedTimestamp > 0)")
                    db.execSQL("UPDATE study_tasks SET isCompleted = 0 WHERE isCompleted = 1 AND NOT EXISTS (SELECT 1 FROM questions WHERE lastReviewedTimestamp > 0)")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val userProfileDao = database.userProfileDao()
            val subjectDao = database.subjectDao()
            val chapterDao = database.chapterDao()
            val questionDao = database.questionDao()
            val taskDao = database.studyTaskDao()

            // 1. Initial User Profile (0% readiness & 0 day streak for fresh install)
            val defaultYear = ExamDateProvider.getAvailableExamYears().first()
            val initialCountdown = ExamDateProvider.calculateCountdown(defaultYear)
            val initialDays = when (initialCountdown) {
                is CountdownResult.Upcoming -> initialCountdown.daysRemaining.toInt()
                else -> 0
            }
            userProfileDao.insertOrUpdateProfile(
                UserProfileEntity(
                    id = 1,
                    name = "",
                    examTrack = "HSC",
                    examYear = defaultYear.toString(),
                    group = "Science",
                    examDate = ExamDateProvider.formatExamDate(defaultYear),
                    daysRemaining = initialDays,
                    examReadyPercentage = 0,
                    streakDays = 0,
                    dailyStudyTime = "1 hour",
                    isOfflineMode = true,
                    cloudAiEnabled = false
                )
            )

            // 2. Subjects (0% initial mastery)
            val initialSubjects = listOf(
                SubjectEntity("chem", "Chemistry", "2nd paper", 12, 312, 0, "flask", true),
                SubjectEntity("phy", "Physics", "1st & 2nd paper", 10, 280, 0, "atom", true),
                SubjectEntity("math", "Higher Math", "1st & 2nd paper", 10, 260, 0, "sigma", true),
                SubjectEntity("eng", "English", "Units 1-8", 8, 190, 0, "text", true),
                SubjectEntity("bio", "Biology", "Botany & Zoology", 12, 240, 0, "leaf", false),
                SubjectEntity("ict", "ICT", "Complete syllabus", 6, 150, 0, "computer", false)
            )
            subjectDao.insertAll(initialSubjects)

            // 3. Chapters for Chemistry (0% initial mastery)
            val chemChapters = listOf(
                ChapterEntity(1, "chem", 1, "Environmental Chemistry", 8, 26, 0, 0, false),
                ChapterEntity(2, "chem", 2, "Organic Chemistry", 14, 48, 0, 0, true),
                ChapterEntity(3, "chem", 3, "Quantitative Chemistry", 9, 40, 0, 0, false),
                ChapterEntity(4, "chem", 4, "Electrochemistry", 7, 32, 0, 0, true),
                ChapterEntity(5, "chem", 5, "Economic Chemistry", 6, 20, 0, 0, false)
            )
            chapterDao.insertAll(chemChapters)

            // 4. Questions (including SHE potential from screen 09 and Daniell cell from screen 18/20b)
            val sampleQuestions = listOf(
                QuestionEntity(
                    chapterId = 4,
                    subjectId = "chem",
                    topicName = "Electrode potential",
                    difficulty = "Medium",
                    questionText = "The standard hydrogen electrode (SHE) potential at 25 °C is...",
                    optionA = "1.00 V",
                    optionB = "0.00 V",
                    optionC = "-0.76 V",
                    optionD = "+0.34 V",
                    correctOption = "B",
                    explanation = "SHE is the reference electrode. Every other electrode potential is measured against it, so its value is fixed at 0.00 V by definition.",
                    syllabusReference = "NCTB HSC Chemistry 2nd paper, Ch 4"
                ),
                QuestionEntity(
                    chapterId = 4,
                    subjectId = "chem",
                    topicName = "Daniell cell emf",
                    difficulty = "Medium",
                    questionText = "Calculate the standard EMF of the cell Zn | Zn²⁺ (1 M) || Cu²⁺ (1 M) | Cu, given E°(Zn²⁺/Zn) = -0.76 V and E°(Cu²⁺/Cu) = +0.34 V.",
                    optionA = "0.42 V",
                    optionB = "1.10 V",
                    optionC = "-1.10 V",
                    optionD = "0.76 V",
                    correctOption = "B",
                    explanation = "E°cell = E°cathode - E°anode = 0.34 - (-0.76) = 1.10 V. Zn acts as anode and Cu acts as cathode.",
                    syllabusReference = "NCTB HSC Chemistry 2nd paper, Ch 4"
                ),
                QuestionEntity(
                    chapterId = 2,
                    subjectId = "chem",
                    topicName = "Alcohols",
                    difficulty = "Hard",
                    questionText = "Which reagent is used to differentiate between primary, secondary, and tertiary alcohols?",
                    optionA = "Tollens' reagent",
                    optionB = "Lucas reagent (conc. HCl + anh. ZnCl₂)",
                    optionC = "Fehling's solution",
                    optionD = "Grignard reagent",
                    correctOption = "B",
                    explanation = "Lucas test differentiates alcohols based on their reactivity with concentrated HCl and anhydrous ZnCl₂ (tertiary reacts immediately, secondary in 5 min, primary requires heating).",
                    syllabusReference = "NCTB HSC Chemistry 2nd paper, Ch 2"
                ),
                QuestionEntity(
                    chapterId = 4,
                    subjectId = "chem",
                    topicName = "Galvanic cell notation",
                    difficulty = "Hard",
                    questionText = "In IUPAC cell notation, which part of the electrochemical cell is written on the extreme left?",
                    optionA = "Cathode metal",
                    optionB = "Anode metal / Oxidation electrode",
                    optionC = "Salt bridge",
                    optionD = "Electrolyte solution of cathode",
                    correctOption = "B",
                    explanation = "According to IUPAC convention, the anode (oxidation half-cell) is always written on the left, and the cathode (reduction half-cell) is on the right.",
                    syllabusReference = "NCTB HSC Chemistry 2nd paper, Ch 4"
                )
            )
            questionDao.insertAll(sampleQuestions)

            // 5. Initial Study Tasks (from Home screen Today's Plan)
            val initialTasks = listOf(
                StudyTaskEntity(
                    id = 1,
                    title = "Organic Chemistry basics",
                    subject = "Chemistry",
                    durationMinutes = 15,
                    questionCount = 5,
                    isCompleted = false,
                    scheduledDate = "Today",
                    priority = "High",
                    category = "Learn"
                ),
                StudyTaskEntity(
                    id = 2,
                    title = "Electrode potential",
                    subject = "Chemistry",
                    durationMinutes = 20,
                    questionCount = 10,
                    isCompleted = false,
                    scheduledDate = "Today",
                    priority = "High",
                    category = "Practice"
                ),
                StudyTaskEntity(
                    id = 3,
                    title = "Vectors basics",
                    subject = "Physics",
                    durationMinutes = 10,
                    questionCount = 5,
                    isCompleted = false,
                    scheduledDate = "Today",
                    priority = "Medium",
                    category = "Learn"
                )
            )
            initialTasks.forEach { taskDao.insertTask(it) }
        }
    }
}
