package com.porua.app.data.content

// ─── JSON data models ───────────────────────────────────────────────────────

data class SubjectIndex(
    val subject: String,
    val file: String,
    val chapters: Int
)

data class McqRaw(
    val number: Int,
    val content: String          // raw string: "Question A. opt B. opt … Answer: X"
)

data class ChapterContent(
    val overview: String = "",
    val key_terms: List<String> = emptyList(),
    val study_notes: List<String> = emptyList(),
    val short_questions: String = "",
    val long_questions: String = "",
    val mcqs: List<McqRaw> = emptyList(),
    val answer_tip: String = "",
    val model_answers: String = "",
    val worked_example: String = "",
    val parsedMcqs: List<ParsedMcq> = emptyList(),
    val explanations: List<String> = emptyList()
)

data class SubjectChapter(
    val chapter_number: Int,
    val title: String,
    val paper: String,
    val content: ChapterContent
)

data class SubjectData(
    val subject: String,
    val stream: String = "",
    val curriculum: String = "",
    val chapters: List<SubjectChapter> = emptyList()
)

// ─── App-level parsed models (used in UI) ───────────────────────────────────

data class ContentSubject(
    val id: String,           // e.g. "physics"
    val name: String,         // e.g. "Physics"
    val group: String,        // e.g. "Science"
    val chapterCount: Int,
    val iconType: String      // "atom" | "flask" | "sigma" | "leaf" | "book"
)

data class ContentChapter(
    val subjectId: String,
    val chapterNumber: Int,
    val title: String,
    val paper: String,
    val overview: String,
    val studyNotes: List<String>,
    val shortQuestions: List<ParsedShortQuestion>,
    val mcqs: List<ParsedMcq>
)

data class ParsedMcq(
    val number: Int,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: String   // "A" | "B" | "C" | "D"
)

data class ParsedShortQuestion(
    val number: Int,
    val question: String,
    val answer: String
)
