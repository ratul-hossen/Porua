package com.porua.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
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
}
