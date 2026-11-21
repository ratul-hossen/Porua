package com.porua.app.data.content

import android.content.Context

/**
 * Single source-of-truth for all content (JSON-based).
 * Currently wires only the Science group.
 * Arts and Commerce groups will be added later.
 */
object ContentRepository {

    // Science subjects with their asset paths
    private val scienceSubjects = listOf(
        ContentSubject("physics",   "Physics",             "Science", 21, "atom"),
        ContentSubject("chemistry", "Chemistry",           "Science", 10, "flask"),
        ContentSubject("biology",   "Biology",             "Science", 24, "leaf"),
        ContentSubject("higher_mathematics", "Higher Math","Science", 20, "sigma")
    )

    private val subjectAssetPaths = mapOf(
        "physics"            to "science/physics.json",
        "chemistry"          to "science/chemistry.json",
        "biology"            to "science/biology.json",
        "higher_mathematics" to "science/higher_mathematics.json"
    )

    // ── Arts group subjects (to be filled later) ────────────────────────────
    private val artsSubjects: List<ContentSubject> = emptyList()

    // ── Commerce group subjects (to be filled later) ─────────────────────────
    private val commerceSubjects: List<ContentSubject> = emptyList()

    // ── Public API ───────────────────────────────────────────────────────────

    fun getSubjectsForGroup(group: String): List<ContentSubject> = when (group) {
        "Science"  -> scienceSubjects
        "Arts"     -> artsSubjects
        "Commerce" -> commerceSubjects
        else       -> emptyList()
    }

    fun getChaptersForSubject(context: Context, subjectId: String): List<ContentChapter> {
        val path = subjectAssetPaths[subjectId] ?: return emptyList()
        val subjectData = ContentParser.loadSubjectData(context, path) ?: return emptyList()
        return subjectData.chapters.map { ContentParser.toContentChapter(subjectId, it) }
    }

    fun getAllMcqsForSubject(context: Context, subjectId: String): List<PooledMcqItem> {
        val path = subjectAssetPaths[subjectId] ?: return emptyList()
        val subjectData = ContentParser.loadSubjectData(context, path) ?: return emptyList()
        val subjectName = scienceSubjects.find { it.id == subjectId }?.name ?: subjectData.subject
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
        GroupInfo("Science",  "Physics, Chemistry, Biology, Math", scienceSubjects.size, true),
        GroupInfo("Arts",     "Bangla, English, History, Civics",  0,                   false),
        GroupInfo("Commerce", "Accounting, Business, Economics",   0,                   false)
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
