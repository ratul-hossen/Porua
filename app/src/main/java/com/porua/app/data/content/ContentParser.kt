package com.porua.app.data.content

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object ContentParser {

    // ── Load a subject JSON from assets ─────────────────────────────────────
    fun loadSubjectData(context: Context, assetPath: String): SubjectData? {
        return try {
            val json = context.assets.open(assetPath).bufferedReader().readText()
            parseSubjectData(JSONObject(json))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseSubjectData(obj: JSONObject): SubjectData {
        val chaptersArr = obj.optJSONArray("chapters") ?: JSONArray()
        val chapters = (0 until chaptersArr.length()).map { i ->
            parseChapter(chaptersArr.getJSONObject(i))
        }
        return SubjectData(
            subject = obj.optString("subject"),
            stream = obj.optString("stream"),
            curriculum = obj.optString("curriculum"),
            chapters = chapters
        )
    }

    private fun parseChapter(obj: JSONObject): SubjectChapter {
        val contentObj = obj.optJSONObject("content") ?: JSONObject()
        val mcqArr = contentObj.optJSONArray("mcqs") ?: JSONArray()
        val mcqs = (0 until mcqArr.length()).map { i ->
            val m = mcqArr.getJSONObject(i)
            McqRaw(
                number = m.optInt("number", i + 1),
                content = m.optString("content")
            )
        }
        val keyTermsArr = contentObj.optJSONArray("key_terms") ?: JSONArray()
        val studyNotesArr = contentObj.optJSONArray("study_notes") ?: JSONArray()

        return SubjectChapter(
            chapter_number = obj.optInt("chapter_number"),
            title = obj.optString("title"),
            paper = obj.optString("paper"),
            content = ChapterContent(
                overview = contentObj.optString("overview"),
                key_terms = (0 until keyTermsArr.length()).map { keyTermsArr.getString(it) },
                study_notes = (0 until studyNotesArr.length()).map { studyNotesArr.getString(it) },
                short_questions = contentObj.optString("short_questions"),
                long_questions = contentObj.optString("long_questions"),
                mcqs = mcqs,
                answer_tip = contentObj.optString("answer_tip"),
                model_answers = contentObj.optString("model_answers"),
                worked_example = contentObj.optString("worked_example")
            )
        )
    }

    // ── Parse raw MCQ string into structured ParsedMcq ─────────────────────
    // Input example: "Which organelle…? A. Ribosome B. Mitochondrion C. Golgi D. Lysosome Answer: B"
    fun parseMcq(raw: McqRaw): ParsedMcq {
        val text = raw.content.trim()

        // Extract correct answer  (last "Answer: X")
        val answerRegex = Regex("""Answer:\s*([A-D])""", RegexOption.IGNORE_CASE)
        val correctOption = answerRegex.findAll(text).lastOrNull()?.groupValues?.get(1)?.uppercase() ?: "A"

        // Split on option labels  A.  B.  C.  D.  (or A) B) …)
        val optionRegex = Regex("""(?:^|\s)([A-D])[.)]\s+""")
        val splits = optionRegex.findAll(text).toList()

        fun extract(label: String): String {
            val idx = splits.indexOfFirst { it.groupValues[1].uppercase() == label }
            if (idx < 0) return ""
            val start = splits[idx].range.last + 1
            val end = if (idx + 1 < splits.size) splits[idx + 1].range.first else text.length
            return text.substring(start, end)
                .replace(answerRegex, "")
                .trim()
                .trimEnd('.')
        }

        // Question text = everything before the first option label
        val questionText = if (splits.isNotEmpty()) {
            text.substring(0, splits.first().range.first).trim()
        } else {
            text.replace(answerRegex, "").trim()
        }

        return ParsedMcq(
            number = raw.number,
            questionText = questionText.ifBlank { text },
            optionA = extract("A"),
            optionB = extract("B"),
            optionC = extract("C"),
            optionD = extract("D"),
            correctOption = correctOption
        )
    }

    // ── Parse short-question block into list of Q&A pairs ───────────────────
    // Handles both numbered ("1. Q\n\n   Answer: A") and plain ("Q\n\n   Answer: A") formats
    fun parseShortQuestions(raw: String): List<ParsedShortQuestion> {
        if (raw.isBlank()) return emptyList()

        val results = mutableListOf<ParsedShortQuestion>()

        // Try splitting on "Answer:" boundaries
        // Pattern: optional number + question text + "Answer:" + answer text
        val qaRegex = Regex(
            """(?:^\s*\d+\.\s*)?(.+?)\s+Answer:\s*(.+?)(?=\s+(?:\d+\.\s+|\z)|$)""",
            setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE)
        )

        // Simpler line-by-line split: split on "Answer:"
        val blocks = raw.split(Regex("""(?=\d+\.\s+[A-Z])""")).filter { it.isNotBlank() }

        if (blocks.size > 1) {
            blocks.forEachIndexed { i, block ->
                val parts = block.split(Regex("""Answer:""", RegexOption.IGNORE_CASE), limit = 2)
                val q = parts.getOrElse(0) { "" }
                    .replace(Regex("""^\d+\.\s*"""), "").trim()
                val a = parts.getOrElse(1) { "" }.trim()
                if (q.isNotBlank()) {
                    results.add(ParsedShortQuestion(i + 1, q, a))
                }
            }
        } else {
            // Fallback: split on "Answer:" directly
            val parts = raw.split(Regex("""Answer:""", RegexOption.IGNORE_CASE))
            if (parts.size >= 2) {
                val q = parts[0].replace(Regex("""^\d+\.\s*"""), "").trim()
                val a = parts[1].trim()
                results.add(ParsedShortQuestion(1, q, a))
            } else {
                results.add(ParsedShortQuestion(1, raw.trim(), ""))
            }
        }

        return results.filter { it.question.isNotBlank() }
    }

    // ── Build ContentChapter from SubjectChapter ─────────────────────────────
    fun toContentChapter(subjectId: String, ch: SubjectChapter): ContentChapter {
        return ContentChapter(
            subjectId = subjectId,
            chapterNumber = ch.chapter_number,
            title = ch.title,
            paper = ch.paper,
            overview = ch.content.overview,
            studyNotes = ch.content.study_notes,
            shortQuestions = parseShortQuestions(ch.content.short_questions),
            mcqs = ch.content.mcqs.map { parseMcq(it) }
        )
    }
}
