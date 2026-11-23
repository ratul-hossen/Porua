package com.porua.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object GetStarted : Screen("get_started")
    object SignUp : Screen("sign_up")
    object Login : Screen("login")
    object ExamTrack : Screen("exam_track")
    object SubjectsDate : Screen("subjects_date")
    object DiagnosticIntro : Screen("diagnostic_intro")
    object Home : Screen("home")
    object Practice : Screen("practice")
    object Chapters : Screen("chapters")
    object Question : Screen("question")
    object AskPorua : Screen("ask_porua")
    object AiInsights : Screen("ai_insights")
    object SnapQuestion : Screen("snap_question")
    object Progress : Screen("progress")
    object Plan : Screen("plan")
    object Me : Screen("me")

    // ── Content (JSON-based) screens ─────────────────────────────────────────
    object GroupSelection : Screen("group_selection")
    object ContentSubjects : Screen("content_subjects/{group}") {
        fun createRoute(group: String) = "content_subjects/$group"
    }
    object ContentChapters : Screen("content_chapters/{subjectId}/{subjectName}") {
        fun createRoute(subjectId: String, subjectName: String) =
            "content_chapters/$subjectId/${android.net.Uri.encode(subjectName)}"
    }
    object ContentPractice : Screen("content_practice/{subjectId}/{chapterIndex}/{mode}") {
        fun createRoute(subjectId: String, chapterIndex: Int, mode: String) =
            "content_practice/$subjectId/$chapterIndex/$mode"
    }

    // ── Coursera-Style 20-Question MCQ Quiz ──────────────────────────────────
    object Quiz : Screen("quiz/{subjectId}?chapterNumber={chapterNumber}&chapterIndex={chapterIndex}") {
        fun createRoute(subjectId: String, chapterNumber: Int? = null, chapterIndex: Int? = null): String {
            val params = mutableListOf<String>()
            if (chapterNumber != null) params.add("chapterNumber=$chapterNumber")
            if (chapterIndex != null) params.add("chapterIndex=$chapterIndex")
            return if (params.isNotEmpty()) "quiz/$subjectId?${params.joinToString("&")}" else "quiz/$subjectId"
        }
    }
}
