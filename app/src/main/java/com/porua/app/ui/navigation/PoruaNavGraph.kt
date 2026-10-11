package com.porua.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.porua.app.ui.components.BottomNavItem
import com.porua.app.ui.screens.AiInsightsScreen
import com.porua.app.ui.screens.AskPoruaScreen
import com.porua.app.ui.screens.ChaptersScreen
import com.porua.app.ui.screens.DiagnosticIntroScreen
import com.porua.app.ui.screens.ExamTrackScreen
import com.porua.app.ui.screens.HomeScreen
import com.porua.app.ui.screens.MeScreen
import com.porua.app.ui.screens.PlanScreen
import com.porua.app.ui.screens.PracticeHubScreen
import com.porua.app.ui.screens.ProgressScreen
import com.porua.app.ui.screens.QuestionScreen
import com.porua.app.ui.screens.SnapQuestionScreen
import com.porua.app.ui.screens.SplashScreen
import com.porua.app.ui.screens.SubjectsDateScreen
import com.porua.app.ui.screens.WelcomeScreen
import com.porua.app.ui.viewmodel.PoruaViewModel

@Composable
fun PoruaNavGraph(
    navController: NavHostController,
    viewModel: PoruaViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    val navigateBottom: (BottomNavItem) -> Unit = { item ->
        val target = when (item) {
            BottomNavItem.HOME -> Screen.Home.route
            BottomNavItem.PRACTICE -> Screen.Practice.route
            BottomNavItem.PROGRESS -> Screen.Progress.route
            BottomNavItem.PLAN -> Screen.Plan.route
            BottomNavItem.ME -> Screen.Me.route
        }
        navController.navigate(target) {
            popUpTo(Screen.Home.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // 01 · Splash
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // 02 · Welcome
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onGetStarted = { navController.navigate(Screen.ExamTrack.route) },
                onRestoreBackup = { navController.navigate(Screen.Home.route) }
            )
        }

        // 03 · Exam track (Step 1 of 4)
        composable(Screen.ExamTrack.route) {
            ExamTrackScreen(
                onBack = { navController.popBackStack() },
                onContinue = { track, group ->
                    viewModel.updateExamInfo(track, group, "12 Apr 2027")
                    navController.navigate(Screen.SubjectsDate.route)
                }
            )
        }

        // 04 · Subjects & exam date (Step 2 of 4)
        composable(Screen.SubjectsDate.route) {
            SubjectsDateScreen(
                onBack = { navController.popBackStack() },
                onContinue = { subjects, date, time ->
                    navController.navigate(Screen.DiagnosticIntro.route)
                }
            )
        }

        // 05 · Diagnostic intro (Step 4 of 4)
        composable(Screen.DiagnosticIntro.route) {
            DiagnosticIntroScreen(
                onBack = { navController.popBackStack() },
                onStartDiagnostic = { navController.navigate(Screen.Question.route) },
                onSkipForNow = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        // 06 · Home
        composable(Screen.Home.route) {
            HomeScreen(
                uiState = uiState,
                onNavigateBottom = navigateBottom,
                onToggleTask = { id, comp -> viewModel.toggleTask(id, comp) },
                onSeeAllTasks = { navController.navigate(Screen.Plan.route) },
                onSeeAiInsights = { navController.navigate(Screen.AiInsights.route) },
                onAskPorua = { navController.navigate(Screen.AskPorua.route) },
                onPracticeTask = { task -> navController.navigate(Screen.Question.route) }
            )
        }

        // 07 · Practice hub
        composable(Screen.Practice.route) {
            PracticeHubScreen(
                uiState = uiState,
                onNavigateBottom = navigateBottom,
                onSubjectClick = { sub -> navController.navigate(Screen.Chapters.route) },
                onSmartMixClick = { navController.navigate(Screen.Question.route) },
                onReviewDueClick = { navController.navigate(Screen.Question.route) },
                onPracticeWithAiClick = { navController.navigate(Screen.AskPorua.route) },
                onSearchClick = { navController.navigate(Screen.Chapters.route) }
            )
        }

        // 08 · Chapters
        composable(Screen.Chapters.route) {
            ChaptersScreen(
                subjectTitle = "Chemistry • 2nd paper",
                chapters = uiState.currentSubjectChapters,
                onBack = { navController.popBackStack() },
                onChapterClick = { chapter -> navController.navigate(Screen.Question.route) }
            )
        }

        // 09 & 10 · Question & Feedback
        composable(Screen.Question.route) {
            QuestionScreen(
                uiState = uiState,
                onClose = { navController.popBackStack() },
                onSelectOption = { option -> viewModel.selectQuizOption(option) },
                onCheckAnswer = { viewModel.checkCurrentAnswer() },
                onSkip = { viewModel.nextQuestion() },
                onApplySm2Rating = { rating -> viewModel.applySm2Rating(rating) },
                onExplainWithAi = { navController.navigate(Screen.AskPorua.route) },
                onNextQuestion = { viewModel.nextQuestion() }
            )
        }

        // 18 & 18c · Ask Porua AI Chat
        composable(Screen.AskPorua.route) {
            AskPoruaScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
                onSendMessage = { text -> viewModel.sendChatMessage(text) },
                onOpenCameraSnap = { navController.navigate(Screen.SnapQuestion.route) },
                onSaveReviewCard = { viewModel.addNewTask("Electrochemistry review", "Chemistry", 15, "High", "Revise") },
                onToggleConsentDialog = { show -> viewModel.setShowCloudConsent(show) }
            )
        }

        // 19 · AI insights
        composable(Screen.AiInsights.route) {
            AiInsightsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // 20 & 20b · Snap a question & OCR result
        composable(Screen.SnapQuestion.route) {
            SnapQuestionScreen(
                uiState = uiState,
                onClose = { navController.popBackStack() },
                onSolveWithAi = { questionText ->
                    viewModel.sendChatMessage("Solve this question: $questionText")
                    navController.navigate(Screen.AskPorua.route) {
                        popUpTo(Screen.SnapQuestion.route) { inclusive = true }
                    }
                },
                onSelectOfflineMatch = { matchQ ->
                    navController.navigate(Screen.Question.route)
                }
            )
        }

        // Progress / Mastery Map
        composable(Screen.Progress.route) {
            ProgressScreen(
                uiState = uiState,
                onNavigateBottom = navigateBottom,
                onOpenAiInsights = { navController.navigate(Screen.AiInsights.route) }
            )
        }

        // Plan / Revision Planner (CRUD tasks)
        composable(Screen.Plan.route) {
            PlanScreen(
                uiState = uiState,
                eventFlow = viewModel.eventFlow,
                onNavigateBottom = navigateBottom,
                onToggleTask = { id, comp -> viewModel.toggleTask(id, comp) },
                onDeleteTask = { task -> viewModel.deleteTask(task) },
                onAddTask = { title, subject, dur, pri, cat ->
                    viewModel.addNewTask(title, subject, dur, pri, cat)
                }
            )
        }

        // Me / Profile & Settings
        composable(Screen.Me.route) {
            MeScreen(
                uiState = uiState,
                onNavigateBottom = navigateBottom,
                onToggleOfflineMode = { viewModel.toggleOfflineMode() },
                onToggleCloudAi = { }
            )
        }
    }
}
