package com.porua.app.data.content

import android.content.Context

/**
 * Single source-of-truth for all NCTB content across Common, Science, Commerce, and Arts streams.
 */
object ContentRepository {

    // Common subjects (mandatory for all groups)
    private val commonSubjects = listOf(
        ContentSubject("ict",     "ICT",     "Common", 6, "laptop"),
        ContentSubject("english", "English", "Common", 3, "book")
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
        ContentSubject("accounting",            "Accounting",         "Commerce", 2, "calculator"),
        ContentSubject("business_organization", "Business Org & Mgt", "Commerce", 2, "briefcase"),
        ContentSubject("finance_banking",       "Finance & Banking",  "Commerce", 2, "bank")
    )

    // Arts / Humanities subjects
    private val artsSubjects = listOf(
        ContentSubject("economics", "Economics",             "Arts", 1, "chart"),
        ContentSubject("civics",    "Civics & Governance",   "Arts", 2, "scale"),
        ContentSubject("history",   "History of Bangladesh", "Arts", 2, "scroll")
    )

    private val subjectAssetPaths = mapOf(
        "ict"                   to "common/ict.json",
        "english"               to "common/english.json",
        "physics"               to "science/physics.json",
        "chemistry"             to "science/chemistry.json",
        "biology"               to "science/biology.json",
        "higher_mathematics"    to "science/higher_mathematics.json",
        "accounting"            to "commerce/accounting.json",
        "business_organization" to "commerce/business_organization.json",
        "finance_banking"       to "commerce/finance_banking.json",
        "economics"             to "arts/economics.json",
        "civics"                to "arts/civics.json",
        "history"               to "arts/history.json"
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
        subjectData.chapters.forEach { ch ->
            val contentCh = ContentParser.toContentChapter(subjectId, ch)
            val explanation = ch.content.worked_example.ifBlank {
                ch.content.model_answers.ifBlank { ch.content.answer_tip }
            }
            contentCh.mcqs.forEach { mcq ->
                result.add(
                    PooledMcqItem(
                        subjectId = subjectId,
                        subjectName = subjectName,
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
        GroupInfo("Business Studies", "ICT, English, Accounting, Business, Finance",     commonSubjects.size + commerceSubjects.size, true),
        GroupInfo("Humanities",       "ICT, English, Economics, Civics, History",        commonSubjects.size + artsSubjects.size,     true)
    )
}

data class PooledMcqItem(
    val subjectId: String,
    val subjectName: String,
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
