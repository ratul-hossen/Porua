package com.porua.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel as composeViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.porua.app.PoruaApplication
import com.porua.app.ui.components.BottomNavItem
import com.porua.app.ui.screens.AiInsightsScreen
import com.porua.app.ui.screens.AskPoruaScreen
import com.porua.app.ui.screens.ChaptersScreen
import com.porua.app.ui.screens.ContentChaptersScreen
import com.porua.app.ui.screens.ContentPracticeScreen
import com.porua.app.ui.screens.ContentSubjectsScreen
import com.porua.app.ui.screens.DiagnosticIntroScreen
import com.porua.app.ui.screens.ExamTrackScreen
import com.porua.app.ui.screens.GroupSelectionScreen
import com.porua.app.ui.screens.HomeScreen
import com.porua.app.ui.screens.LoginScreen
import com.porua.app.ui.screens.MeScreen
import com.porua.app.ui.screens.PlanScreen
import com.porua.app.ui.screens.PracticeHubScreen
import com.porua.app.ui.screens.ProgressScreen
import com.porua.app.ui.screens.QuestionScreen
import com.porua.app.ui.screens.QuizScreen
import com.porua.app.ui.screens.SignUpScreen
import com.porua.app.ui.screens.SnapQuestionScreen
import com.porua.app.ui.screens.SplashScreen
import com.porua.app.ui.screens.SubjectsDateScreen
import com.porua.app.ui.screens.WelcomeScreen
import com.porua.app.ui.viewmodel.ContentViewModel
import com.porua.app.ui.viewmodel.ContentViewModelFactory
import com.porua.app.ui.viewmodel.PoruaViewModel
import com.porua.app.ui.viewmodel.QuizViewModel
import com.porua.app.ui.viewmodel.QuizViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun PoruaNavGraph(
    navController: NavHostController,
    viewModel: PoruaViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val contentViewModel: ContentViewModel = composeViewModel(
        factory = ContentViewModelFactory(context)
    )
    val contentState by contentViewModel.state.collectAsState()

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
            val app = context.applicationContext as PoruaApplication
            SplashScreen(
                onSplashFinished = {
                    coroutineScope.launch {
                        val isLoggedIn = app.authRepository.isLoggedIn.first()
                        val target = if (isLoggedIn) Screen.Home.route else Screen.Welcome.route
                        navController.navigate(target) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        // 02 · Welcome
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onSignUpClick = { navController.navigate(Screen.SignUp.route) },
                onLoginClick = { navController.navigate(Screen.Login.route) }
            )
        }

        // SignUp
        composable(Screen.SignUp.route) {
            val app = context.applicationContext as PoruaApplication
            SignUpScreen(
                authRepository = app.authRepository,
                onSignUpSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Login
        composable(Screen.Login.route) {
            val app = context.applicationContext as PoruaApplication
            LoginScreen(
                authRepository = app.authRepository,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
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
                    viewModel.updateSelectedSubjects(subjects)
                    viewModel.updateExamInfo(uiState.userProfile.examTrack, uiState.userProfile.group, date)
                    navController.navigate(Screen.DiagnosticIntro.route)
                }
            )
        }

        // 05 · Diagnostic intro (Step 4 of 4)
        composable(Screen.DiagnosticIntro.route) {
            val context = androidx.compose.ui.platform.LocalContext.current
            DiagnosticIntroScreen(
                onBack = { navController.popBackStack() },
                onStartDiagnostic = {
                    val prefs = context.getSharedPreferences("porua_prefs", android.content.Context.MODE_PRIVATE)
                    prefs.edit().putBoolean("is_onboarded", true).apply()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                    navController.navigate(Screen.Question.route)
                },
                onSkipForNow = {
                    val prefs = context.getSharedPreferences("porua_prefs", android.content.Context.MODE_PRIVATE)
                    prefs.edit().putBoolean("is_onboarded", true).apply()
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
                onPracticeTask = { task ->
                    val subId = when {
                        task.subject.contains("chem", ignoreCase = true) -> "chemistry"
                        task.subject.contains("bio", ignoreCase = true) -> "biology"
                        task.subject.contains("math", ignoreCase = true) -> "higher_mathematics"
                        else -> "physics"
                    }
                    navController.navigate(Screen.Quiz.createRoute(subId))
                },
                onSetExamYear = { year -> viewModel.setExamYear(year) }
            )
        }

        // 07 · Practice hub
        composable(Screen.Practice.route) {
            PracticeHubScreen(
                uiState = uiState,
                onNavigateBottom = navigateBottom,
                onSubjectClick = { sub ->
                    val resolvedSubjectId = when (sub.id) {
                        "chem" -> "chemistry"
                        "phy" -> "physics"
                        "math" -> "higher_mathematics"
                        "bio" -> "biology"
                        "eng" -> "english"
                        "ict" -> "ict"
                        "acc" -> "accounting"
                        "econ" -> "economics"
                        "civ" -> "civics"
                        "hist" -> "history"
                        "fin" -> "finance_banking"
                        "bus" -> "business_organization"
                        else -> sub.id
                    }
                    contentViewModel.loadChapters(resolvedSubjectId, sub.name)
                    navController.navigate(
                        Screen.ContentChapters.createRoute(resolvedSubjectId, sub.name)
                    )
                },
                onSmartMixClick = {
                    val defaultSub = uiState.subjects.firstOrNull()?.id ?: "physics"
                    val resolvedId = when (defaultSub) {
                        "chem" -> "chemistry"
                        "phy" -> "physics"
                        "math" -> "higher_mathematics"
                        "bio" -> "biology"
                        "eng" -> "english"
                        "ict" -> "ict"
                        else -> defaultSub
                    }
                    navController.navigate(Screen.Quiz.createRoute(resolvedId))
                },
                onReviewDueClick = { navController.navigate(Screen.Question.route) },
                onPracticeWithAiClick = { navController.navigate(Screen.AskPorua.route) },
                onSearchClick = { navController.navigate(Screen.GroupSelection.route) }
            )
        }

        // 08 · Chapters
        composable(Screen.Chapters.route) {
            val defaultSub = uiState.subjects.firstOrNull()?.id ?: "physics"
            val defaultName = uiState.subjects.firstOrNull()?.name ?: "Physics"
            val resolvedId = when (defaultSub) {
                "chem" -> "chemistry"
                "phy" -> "physics"
                "math" -> "higher_mathematics"
                "bio" -> "biology"
                "eng" -> "english"
                "ict" -> "ict"
                else -> defaultSub
            }
            LaunchedEffect(resolvedId) {
                contentViewModel.loadChapters(resolvedId, defaultName)
            }
            ContentChaptersScreen(
                subjectName = defaultName,
                chapters = contentState.chapters,
                onBack = { navController.popBackStack() },
                onChapterClick = { chapter, mode ->
                    val idx = contentState.chapters.indexOf(chapter)
                    if (mode == "mcq") {
                        navController.navigate(
                            Screen.Quiz.createRoute(resolvedId, chapterNumber = chapter.chapterNumber, chapterIndex = idx)
                        )
                    } else {
                        navController.navigate(
                            Screen.ContentPractice.createRoute(resolvedId, idx, mode)
                        )
                    }
                }
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
                onExplainWithAi = {
                    val currentQ = uiState.currentQuestions.getOrNull(uiState.currentQuestionIndex)
                    if (currentQ != null) {
                        viewModel.sendChatMessage("Please explain this question step-by-step:\n${currentQ.questionText}\nOptions: A) ${currentQ.optionA}, B) ${currentQ.optionB}, C) ${currentQ.optionC}, D) ${currentQ.optionD}")
                    }
                    navController.navigate(Screen.AskPorua.route)
                },
                onNextQuestion = { viewModel.nextQuestion() }
            )
        }

        // 18 & 18c · Ask Porua AI Chat
        composable(Screen.AskPorua.route) {
            AskPoruaScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
                onSendMessage = { text, imageBase64, mimeType -> viewModel.sendChatMessage(text, imageBase64, mimeType) },
                onOpenCameraSnap = { navController.navigate(Screen.SnapQuestion.route) },
                onSaveReviewCard = {
                    val lastPrompt = uiState.chatMessages.lastOrNull { it.sender == com.porua.app.ui.viewmodel.ChatSender.USER }?.text?.take(30)
                        ?: "AI Topic Review"
                    val activeSubject = uiState.subjects.find { it.id == uiState.selectedSubjectId }?.name ?: "General"
                    viewModel.addNewTask(lastPrompt, activeSubject, 15, "High", "Revise")
                },
                onToggleConsentDialog = { show -> viewModel.setShowCloudConsent(show) },
                onNewChat = { viewModel.startNewChat() }
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
                },
                onTypeQuestion = {
                    navController.navigate(Screen.AskPorua.route)
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
            val context = androidx.compose.ui.platform.LocalContext.current
            MeScreen(
                uiState = uiState,
                onNavigateBottom = navigateBottom,
                onToggleOfflineMode = { viewModel.toggleOfflineMode() },
                onToggleCloudAi = { viewModel.toggleCloudAi() },
                onToggleSubject = { subjectId -> viewModel.toggleSubjectSelected(subjectId) },
                onResetOnboarding = {
                    coroutineScope.launch {
                        val app = context.applicationContext as PoruaApplication
                        app.authRepository.logout()
                        val prefs = context.getSharedPreferences("porua_prefs", android.content.Context.MODE_PRIVATE)
                        prefs.edit().putBoolean("is_onboarded", false).apply()
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                },
                onLogout = {
                    coroutineScope.launch {
                        val app = context.applicationContext as PoruaApplication
                        app.authRepository.logout()
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                },
                onUpdateProfile = { name, track, group, year, date, goal, selectedSubjectIds ->
                    viewModel.updateStudentProfile(name, track, group, year, date, goal, selectedSubjectIds)
                }
            )
        }

        // ── Content (JSON) screens ────────────────────────────────────────────

        // Group Selection
        composable(Screen.GroupSelection.route) {
            GroupSelectionScreen(
                groups = contentState.groups,
                onGroupSelected = { group ->
                    contentViewModel.selectGroup(group)
                    navController.navigate(Screen.ContentSubjects.createRoute(group))
                }
            )
        }

        // Content Subjects list (per group)
        composable(
            route = Screen.ContentSubjects.route,
            arguments = listOf(navArgument("group") { type = NavType.StringType })
        ) { backStackEntry ->
            val group = backStackEntry.arguments?.getString("group") ?: ""
            ContentSubjectsScreen(
                group = group,
                subjects = contentState.subjects,
                onBack = { navController.popBackStack() },
                onSubjectClick = { subject ->
                    contentViewModel.loadChapters(subject.id, subject.name)
                    navController.navigate(
                        Screen.ContentChapters.createRoute(subject.id, subject.name)
                    )
                }
            )
        }

        // Content Chapters list (per subject)
        composable(
            route = Screen.ContentChapters.route,
            arguments = listOf(
                navArgument("subjectId")   { type = NavType.StringType },
                navArgument("subjectName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val subjectName = android.net.Uri.decode(
                backStackEntry.arguments?.getString("subjectName") ?: ""
            )
            val subjectId = backStackEntry.arguments?.getString("subjectId") ?: ""
            ContentChaptersScreen(
                subjectName = subjectName,
                chapters = contentState.chapters,
                onBack = { navController.popBackStack() },
                onChapterClick = { chapter, mode ->
                    val idx = contentState.chapters.indexOf(chapter)
                    if (mode == "mcq") {
                        navController.navigate(
                            Screen.Quiz.createRoute(subjectId, chapterNumber = chapter.chapterNumber, chapterIndex = idx)
                        )
                    } else {
                        navController.navigate(
                            Screen.ContentPractice.createRoute(subjectId, idx, mode)
                        )
                    }
                }
            )
        }

        // Content Practice (MCQ or Short Q)
        composable(
            route = Screen.ContentPractice.route,
            arguments = listOf(
                navArgument("subjectId")    { type = NavType.IntType.let { NavType.StringType } },
                navArgument("chapterIndex") { type = NavType.IntType },
                navArgument("mode")         { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val idx  = backStackEntry.arguments?.getInt("chapterIndex") ?: 0
            val mode = backStackEntry.arguments?.getString("mode") ?: "mcq"
            val chapter = contentState.chapters.getOrNull(idx)
            if (chapter != null) {
                ContentPracticeScreen(
                    chapter = chapter,
                    mode = mode,
                    onClose = { navController.popBackStack() }
                )
            }
        }

        // Coursera-Style 20-Question MCQ Quiz
        composable(
            route = Screen.Quiz.route,
            arguments = listOf(
                navArgument("subjectId") { type = NavType.StringType },
                navArgument("chapterNumber") {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument("chapterIndex") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getString("subjectId") ?: "physics"
            val chNum = backStackEntry.arguments?.getInt("chapterNumber")?.takeIf { it != -1 }
            val rawIdx = backStackEntry.arguments?.getInt("chapterIndex") ?: -1
            val chIdx = if (rawIdx >= 0) rawIdx else null
            val app = context.applicationContext as PoruaApplication
            val quizViewModel: QuizViewModel = composeViewModel(
                factory = QuizViewModelFactory(context, app.repository)
            )
            LaunchedEffect(subjectId, chNum, chIdx) {
                quizViewModel.startQuiz(subjectId, chapterNumber = chNum, chapterIndex = chIdx)
            }
            QuizScreen(
                viewModel = quizViewModel,
                onClose = { navController.popBackStack() }
            )
        }
    }
}
