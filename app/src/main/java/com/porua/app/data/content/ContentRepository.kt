package com.porua.app.data.content

import android.content.Context

/**
 * Single source-of-truth for all NCTB content across Common, Science, Commerce, and Arts streams.
 */
object ContentRepository {

    // Common subjects (mandatory for all groups)
    private val commonSubjects = listOf(
        ContentSubject("ict",     "ICT",     "Common", 6, "laptop"),
        ContentSubject("english", "English", "Common", 10, "book")
    )

    // Science subjects
    private val scienceSubjects = listOf(
        ContentSubject("physics",            "Physics",      "Science", 21, "atom"),
        ContentSubject("chemistry",          "Chemistry",    "Science", 10, "flask"),
        ContentSubject("biology",            "Biology",      "Science", 24, "leaf"),
        ContentSubject("higher_mathematics", "Higher Math",  "Science", 20, "sigma")
    )

    // Commerce subjects (reserved for future content)
    private val commerceSubjects = listOf(
        ContentSubject("accounting",            "Accounting",         "Commerce", 0, "calculator"),
        ContentSubject("business_organization", "Business Org & Mgt", "Commerce", 0, "briefcase"),
        ContentSubject("finance_banking",       "Finance & Banking",  "Commerce", 0, "bank")
    )

    // Arts / Humanities subjects (reserved for future content)
    private val artsSubjects = listOf(
        ContentSubject("economics", "Economics",             "Arts", 0, "chart"),
        ContentSubject("civics",    "Civics & Governance",   "Arts", 0, "scale"),
        ContentSubject("history",   "History of Bangladesh", "Arts", 0, "scroll")
    )

    private val subjectAssetPaths = mapOf(
        "ict"                to "HSC/Science/Subjects/ICT/ict.json",
        "english"            to "HSC/Science/Subjects/English/english.json",
        "physics"            to "HSC/Science/Subjects/Physics/physics.json",
        "chemistry"          to "HSC/Science/Subjects/Chemistry/chemistry.json",
        "biology"            to "HSC/Science/Subjects/Biology/biology.json",
        "higher_mathematics" to "HSC/Science/Subjects/Higher_Mathematics/higher_mathematics.json"
    )

    // ── Public API ───────────────────────────────────────────────────────────

    fun getSubjectsForGroup(group: String): List<ContentSubject> {
        val stream = when (group.lowercase().trim()) {
            "commerce", "business studies" -> commerceSubjects
            "arts", "humanities"           -> artsSubjects
            else                           -> scienceSubjects
        }
        return commonSubjects + stream
    }

    fun getSubjectName(subjectId: String): String {
        val all = commonSubjects + scienceSubjects + commerceSubjects + artsSubjects
        return all.find { it.id == subjectId }?.name ?: subjectId.replaceFirstChar { it.uppercase() }
    }

    fun getChaptersForSubject(context: Context, subjectId: String): List<ContentChapter> {
        val path = subjectAssetPaths[subjectId] ?: return emptyList()
        val subjectData = ContentParser.loadSubjectData(context, path) ?: return emptyList()
        return subjectData.chapters.map { ContentParser.toContentChapter(subjectId, it) }
    }

    fun getAllMcqsForSubject(context: Context, subjectId: String): List<PooledMcqItem> {
        val path = subjectAssetPaths[subjectId] ?: return emptyList()
        val subjectData = ContentParser.loadSubjectData(context, path) ?: return emptyList()
        val subjectName = getSubjectName(subjectId)
        val result = mutableListOf<PooledMcqItem>()
        subjectData.chapters.forEachIndexed { chIdx, ch ->
            val contentCh = ContentParser.toContentChapter(subjectId, ch)
            contentCh.mcqs.forEachIndexed { qIdx, mcq ->
                val explanation = ch.content.explanations.getOrNull(qIdx)
                    ?: ch.content.worked_example.ifBlank {
                        ch.content.model_answers.ifBlank { ch.content.answer_tip }
                    }
                result.add(
                    PooledMcqItem(
                        subjectId = subjectId,
                        subjectName = subjectName,
                        chapterIndex = chIdx,
                        chapterNumber = ch.chapter_number,
                        chapterTitle = ch.title,
                        paper = ch.paper,
                        mcq = mcq,
                        explanation = explanation
                    )
                )
            }
        }
        return result
    }

    fun getAllGroups(): List<GroupInfo> = listOf(
        GroupInfo("Science",          "ICT, English, Physics, Chemistry, Biology, Math", commonSubjects.size + scienceSubjects.size, true),
        GroupInfo("Business Studies", "ICT, English, Accounting, Business, Finance",     commonSubjects.size + commerceSubjects.size, false),
        GroupInfo("Humanities",       "ICT, English, Economics, Civics, History",        commonSubjects.size + artsSubjects.size,     false)
    )
}

data class PooledMcqItem(
    val subjectId: String,
    val subjectName: String,
    val chapterIndex: Int = 0,
    val chapterNumber: Int,
    val chapterTitle: String,
    val paper: String,
    val mcq: ParsedMcq,
    val explanation: String = ""
)

data class GroupInfo(
    val name: String,
    val subjectPreview: String,
    val subjectCount: Int,
    val isAvailable: Boolean
)
