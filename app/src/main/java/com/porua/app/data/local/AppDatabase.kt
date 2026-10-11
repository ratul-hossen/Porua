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
import kotlinx.coroutines.launch

@Database(
    entities = [
        SubjectEntity::class,
        ChapterEntity::class,
        QuestionEntity::class,
        StudyTaskEntity::class,
        UserProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun subjectDao(): SubjectDao
    abstract fun chapterDao(): ChapterDao
    abstract fun questionDao(): QuestionDao
    abstract fun studyTaskDao(): StudyTaskDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "porua_offline_db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val userProfileDao = database.userProfileDao()
            val subjectDao = database.subjectDao()
            val chapterDao = database.chapterDao()
            val questionDao = database.questionDao()
            val taskDao = database.studyTaskDao()

            // 1. Initial User Profile
            userProfileDao.insertOrUpdateProfile(
                UserProfileEntity(
                    id = 1,
                    name = "Ratul",
                    examTrack = "HSC",
                    examYear = "2027",
                    group = "Science",
                    examDate = "12 Apr 2027",
                    daysRemaining = 186,
                    examReadyPercentage = 58,
                    streakDays = 12,
                    dailyStudyTime = "1 hour",
                    isOfflineMode = true,
                    cloudAiEnabled = false
                )
            )

            // 2. Subjects from Figma screen 04 & 07
            val initialSubjects = listOf(
                SubjectEntity("chem", "Chemistry", "2nd paper", 12, 312, 62, "flask", true),
                SubjectEntity("phy", "Physics", "1st & 2nd paper", 10, 280, 71, "atom", true),
                SubjectEntity("math", "Higher Math", "1st & 2nd paper", 10, 260, 48, "sigma", true),
                SubjectEntity("eng", "English", "Units 1-8", 8, 190, 80, "text", true),
                SubjectEntity("bio", "Biology", "Botany & Zoology", 12, 240, 52, "leaf", false),
                SubjectEntity("ict", "ICT", "Complete syllabus", 6, 150, 65, "computer", false)
            )
            subjectDao.insertAll(initialSubjects)

            // 3. Chapters for Chemistry (as in screen 08)
            val chemChapters = listOf(
                ChapterEntity(1, "chem", 1, "Environmental Chemistry", 8, 26, 86, 2, false),
                ChapterEntity(2, "chem", 2, "Organic Chemistry", 14, 48, 34, 5, true),
                ChapterEntity(3, "chem", 3, "Quantitative Chemistry", 9, 40, 67, 0, false),
                ChapterEntity(4, "chem", 4, "Electrochemistry", 7, 32, 48, 2, true),
                ChapterEntity(5, "chem", 5, "Economic Chemistry", 6, 20, 0, 10, false)
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
                    title = "Alcohols",
                    subject = "Chemistry",
                    durationMinutes = 15,
                    questionCount = 0,
                    isCompleted = true,
                    scheduledDate = "Today",
                    priority = "High",
                    category = "Revise"
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
                    questionCount = 0,
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
