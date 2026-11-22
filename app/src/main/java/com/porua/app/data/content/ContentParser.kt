package com.porua.app.data.content

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object ContentParser {

    // ── Load a subject JSON from assets ─────────────────────────────────────
    fun loadSubjectData(context: Context, assetPath: String): SubjectData? {
        return try {
            val json = context.assets.open(assetPath).bufferedReader().readText()
            val obj = JSONObject(json)
            val baseDir = assetPath.substringBeforeLast('/', "")

            val subject = obj.optString("subject")
            val stream = obj.optString("stream")
            val curriculum = obj.optString("curriculum")

            val chapters = mutableListOf<SubjectChapter>()

            if (obj.has("papers")) {
                val papersArr = obj.optJSONArray("papers") ?: JSONArray()
                for (i in 0 until papersArr.length()) {
                    val paperObj = papersArr.getJSONObject(i)
                    val paperName = paperObj.optString("paper", paperObj.optString("paper_name", ""))
                    val folder = paperObj.optString("folder", "")
                    val chArr = paperObj.optJSONArray("chapters") ?: JSONArray()
                    for (j in 0 until chArr.length()) {
                        val chObj = chArr.getJSONObject(j)
                        val relFile = chObj.optString("file")
                        val defaultChNum = chObj.optInt("chapter_number", j + 1)
                        val defaultTitle = chObj.optString("chapter_title", chObj.optString("title", ""))

                        val chPath = when {
                            relFile.contains("/") -> if (baseDir.isNotBlank()) "$baseDir/$relFile" else relFile
                            folder.isNotBlank()   -> if (baseDir.isNotBlank()) "$baseDir/$folder/$relFile" else "$folder/$relFile"
                            else                  -> if (baseDir.isNotBlank()) "$baseDir/$relFile" else relFile
                        }

                        val chapter = loadChapterFile(
                            context = context,
                            chPath = chPath,
                            defaultPaper = paperName,
                            defaultNum = defaultChNum,
                            defaultTitle = defaultTitle
                        )
                        if (chapter != null) {
                            chapters.add(chapter)
                        }
                    }
                }
            } else if (obj.has("chapters")) {
                val chArr = obj.optJSONArray("chapters") ?: JSONArray()
                for (j in 0 until chArr.length()) {
                    val chObj = chArr.getJSONObject(j)
                    val relFile = chObj.optString("file")
                    if (relFile.isNotBlank()) {
                        val defaultChNum = chObj.optInt("chapter_number", j + 1)
                        val defaultTitle = chObj.optString("chapter_title", chObj.optString("title", ""))
                        val chPath = if (baseDir.isNotBlank()) "$baseDir/$relFile" else relFile
                        val chapter = loadChapterFile(
                            context = context,
                            chPath = chPath,
                            defaultPaper = "Compulsory",
                            defaultNum = defaultChNum,
                            defaultTitle = defaultTitle
                        )
                        if (chapter != null) {
                            chapters.add(chapter)
                        }
                    } else {
                        chapters.add(parseLegacyChapter(chObj))
                    }
                }
            }

            SubjectData(
                subject = subject,
                stream = stream,
                curriculum = curriculum,
                chapters = chapters
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun loadChapterFile(
        context: Context,
        chPath: String,
        defaultPaper: String,
        defaultNum: Int,
        defaultTitle: String
    ): SubjectChapter? {
        return try {
            val json = context.assets.open(chPath).bufferedReader().readText()
            val chJson = JSONObject(json)

            val rawPaper = chJson.optString("paper", defaultPaper).trim()
            val paper = if (rawPaper.isNotBlank()) rawPaper else defaultPaper
            val chNum = chJson.optInt("chapter_number", defaultNum)
            val title = chJson.optString("chapter_title", chJson.optString("title", defaultTitle))

            val topicsArr = chJson.optJSONArray("topics") ?: JSONArray()
            val topicNames = mutableListOf<String>()
            val parsedMcqs = mutableListOf<ParsedMcq>()
            val explanations = mutableListOf<String>()
            var qCounter = 1

            for (t in 0 until topicsArr.length()) {
                val topObj = topicsArr.getJSONObject(t)
                val topName = topObj.optString("topic_name")
                if (topName.isNotBlank()) {
                    topicNames.add(topName)
                }
                val questionsArr = topObj.optJSONArray("questions")
                    ?: topObj.optJSONArray("mcqs")
                    ?: JSONArray()
                for (q in 0 until questionsArr.length()) {
                    val qObj = questionsArr.getJSONObject(q)
                    val qText = qObj.optString("question")
                    val optionsArr = qObj.optJSONArray("options")
                    var optA = ""
                    var optB = ""
                    var optC = ""
                    var optD = ""
                    if (optionsArr != null) {
                        for (k in 0 until optionsArr.length()) {
                            val opt = optionsArr.getJSONObject(k)
                            val label = opt.optString("label").trim().uppercase()
                            val text = opt.optString("text").trim()
                            when (label) {
                                "A" -> optA = text
                                "B" -> optB = text
                                "C" -> optC = text
                                "D" -> optD = text
                            }
                        }
                    }
                    val correctAns = qObj.optString("correct_answer").trim().uppercase()
                    val explanation = qObj.optString("explanation")

                    parsedMcqs.add(
                        ParsedMcq(
                            number = qCounter++,
                            questionText = qText,
                            optionA = optA,
                            optionB = optB,
                            optionC = optC,
                            optionD = optD,
                            correctOption = correctAns
                        )
                    )
                    explanations.add(explanation)
                }
            }

            val overview = if (topicNames.isNotEmpty()) {
                "Topics covered: " + topicNames.joinToString(", ")
            } else {
                chJson.optString("overview")
            }

            SubjectChapter(
                chapter_number = chNum,
                title = title,
                paper = paper,
                content = ChapterContent(
                    overview = overview,
                    study_notes = topicNames,
                    parsedMcqs = parsedMcqs,
                    explanations = explanations
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseLegacyChapter(obj: JSONObject): SubjectChapter {
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
    fun parseMcq(raw: McqRaw): ParsedMcq {
        val text = raw.content.trim()

        val answerRegex = Regex("""Answer:\s*([A-D])""", RegexOption.IGNORE_CASE)
        val correctOption = answerRegex.findAll(text).lastOrNull()?.groupValues?.get(1)?.uppercase() ?: "A"

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

    fun parseShortQuestions(raw: String): List<ParsedShortQuestion> {
        if (raw.isBlank()) return emptyList()

        val results = mutableListOf<ParsedShortQuestion>()
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
        val mcqs = if (ch.content.parsedMcqs.isNotEmpty()) {
            ch.content.parsedMcqs
        } else {
            ch.content.mcqs.map { parseMcq(it) }
        }
        return ContentChapter(
            subjectId = subjectId,
            chapterNumber = ch.chapter_number,
            title = ch.title,
            paper = ch.paper,
            overview = ch.content.overview,
            studyNotes = ch.content.study_notes,
            shortQuestions = parseShortQuestions(ch.content.short_questions),
            mcqs = mcqs
        )
    }
}
