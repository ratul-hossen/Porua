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

    // Commerce subjects
    private val commerceSubjects = listOf(
        ContentSubject("accounting",            "Accounting",         "Commerce", 3, "calculator"),
        ContentSubject("business_organization", "Business Org & Mgt", "Commerce", 2, "briefcase"),
        ContentSubject("finance_banking",       "Finance & Banking",  "Commerce", 2, "bank")
    )

    // Arts / Humanities subjects
    private val artsSubjects = listOf(
        ContentSubject("economics", "Economics",             "Arts", 2, "chart"),
        ContentSubject("civics",    "Civics & Governance",   "Arts", 2, "scale"),
        ContentSubject("history",   "History of Bangladesh", "Arts", 2, "scroll")
    )

    private val subjectAssetPaths = mapOf(
        "ict"                   to "HSC/Science/Subjects/ICT/ict.json",
        "english"               to "HSC/Science/Subjects/English/english.json",
        "physics"               to "HSC/Science/Subjects/Physics/physics.json",
        "chemistry"             to "HSC/Science/Subjects/Chemistry/chemistry.json",
        "biology"               to "HSC/Science/Subjects/Biology/biology.json",
        "higher_mathematics"    to "HSC/Science/Subjects/Higher_Mathematics/higher_mathematics.json",
        "accounting"            to "HSC/Commerce/Subjects/Accounting/accounting.json",
        "business_organization" to "HSC/Commerce/Subjects/Business_Organization/business_organization.json",
        "finance_banking"       to "HSC/Commerce/Subjects/Finance_Banking/finance_banking.json",
        "economics"             to "HSC/Arts/Subjects/Economics/economics.json",
        "civics"                to "HSC/Arts/Subjects/Civics/civics.json",
        "history"               to "HSC/Arts/Subjects/History/history.json"
    )

    // ── Public API ───────────────────────────────────────────────────────────

    fun getSubjectsForGroup(group: String): List<ContentSubject> {
        val stream = when (group.lowercase().trim()) {
            "commerce", "commers", "business studies" -> commerceSubjects
            "arts", "humanities"                      -> artsSubjects
            else                                      -> scienceSubjects
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
        GroupInfo("Science",  "ICT, English, Physics, Chemistry, Biology, Math", commonSubjects.size + scienceSubjects.size, true),
        GroupInfo("Arts",     "ICT, English, Economics, Civics, History",        commonSubjects.size + artsSubjects.size,     true),
        GroupInfo("Commerce", "ICT, English, Accounting, Business, Finance",     commonSubjects.size + commerceSubjects.size, true)
    )

    fun getTotalQuestionsForSubject(subjectId: String): Int {
        return when (subjectId.lowercase().trim()) {
            "ict" -> 300
            "english" -> 500
            "physics" -> 1050
            "chemistry" -> 500
            "biology" -> 1200
            "higher_mathematics", "math" -> 1000
            "accounting" -> 15
            "business_organization" -> 10
            "finance_banking" -> 10
            "economics" -> 10
            "civics" -> 10
            "history" -> 10
            else -> 50
        }
    }

    fun getTotalQuestionsForGroup(group: String): Int {
        val subjects = getSubjectsForGroup(group)
        return subjects.sumOf { getTotalQuestionsForSubject(it.id) }.coerceAtLeast(1)
    }
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
