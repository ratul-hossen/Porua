package com.porua.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.porua.app.ui.components.PoruaPrimaryButton
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.ShapeFull
import com.porua.app.ui.theme.ShapeLg
import com.porua.app.ui.theme.ShapeMd
import com.porua.app.ui.theme.ShapeSm
import com.porua.app.ui.viewmodel.QuizQuestion
import com.porua.app.ui.viewmodel.QuizViewModel
import kotlinx.coroutines.launch

@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    onClose: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val colors = LocalPoruaColors.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var showExitDialog by remember { mutableStateOf(false) }
    var showSubmitDialog by remember { mutableStateOf(false) }

    // Intercept back button during active quiz
    BackHandler(enabled = !state.isSubmitted) {
        showExitDialog = true
    }

    if (state.isSubmitted && state.result != null) {
        QuizResultScreen(
            state = state,
            onFilterChange = { viewModel.setReviewFilter(it) },
            onRetryWrong = { viewModel.retryWrongQuestions() },
            onNewSet = { viewModel.startNextSession() },
            onExplainAi = { viewModel.explainWithAi(it) },
            onBackToPractice = onClose
        )
        return
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bg),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = colors.primary, strokeWidth = 3.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Preparing your 20-question practice set...", color = colors.textMuted, fontSize = 14.sp)
            }
        }
        return
    }

    val totalQuestions = state.questions.size
    val answeredCount = state.selectedAnswers.size
    val unansweredCount = totalQuestions - answeredCount

    val minutes = state.elapsedSeconds / 60
    val seconds = state.elapsedSeconds % 60
    val timerText = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // ── Sticky Top Bar: Close, Progress, Timer ───────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .border(width = 0.5.dp, color = colors.border)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Close button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.surface2)
                        .clickable { showExitDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit Quiz",
                        tint = colors.text,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Title & Answered Count
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.subjectName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                    Text(
                        text = "Answered $answeredCount of $totalQuestions",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (answeredCount == totalQuestions) colors.success else colors.primary
                    )
                }

                // Timer
                Row(
                    modifier = Modifier
                        .clip(ShapeSm)
                        .background(colors.surface2)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = colors.textMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = timerText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { if (totalQuestions > 0) (answeredCount.toFloat() / totalQuestions) else 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(ShapeFull),
                color = colors.primary,
                trackColor = colors.surface2
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Question Navigator Strip (1 to 20) ───────────────────────────
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                itemsIndexed(state.questions) { index, _ ->
                    val isAnswered = state.selectedAnswers.containsKey(index)
                    val bgColor = if (isAnswered) colors.primary else colors.surface2
                    val textColor = if (isAnswered) Color.White else colors.textMuted
                    val borderColor = if (isAnswered) colors.primary else colors.border

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(bgColor)
                            .border(1.dp, borderColor, CircleShape)
                            .clickable {
                                coroutineScope.launch {
                                    // +1 offset for header item
                                    listState.animateScrollToItem(index + 1)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }
            }
        }

        // ── Scrollable Exam Body ─────────────────────────────────────────────
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Note banner if applicable
            item {
                Spacer(modifier = Modifier.height(12.dp))
                if (state.availabilityNote != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(ShapeMd)
                            .background(colors.primarySoft)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.availabilityNote ?: "",
                            fontSize = 12.sp,
                            color = colors.text,
                            lineHeight = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // 20 Questions on one scrollable page
            itemsIndexed(state.questions) { qIndex, question ->
                ExamQuestionCard(
                    questionIndex = qIndex,
                    question = question,
                    selectedOption = state.selectedAnswers[qIndex],
                    onSelectOption = { option -> viewModel.selectOption(qIndex, option) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Submit Exam Button
            item {
                Spacer(modifier = Modifier.height(12.dp))
                PoruaPrimaryButton(
                    text = "Submit Exam",
                    onClick = {
                        if (unansweredCount > 0) {
                            showSubmitDialog = true
                        } else {
                            viewModel.submitQuiz()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // ── Dialog: Confirm Submission ───────────────────────────────────────────
    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = {
                Text(
                    text = "Submit Exam?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colors.text
                )
            },
            text = {
                Text(
                    text = "You have $unansweredCount unanswered question${if (unansweredCount > 1) "s" else ""}. Would you like to submit anyway?",
                    fontSize = 14.sp,
                    color = colors.textMuted
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSubmitDialog = false
                        viewModel.submitQuiz()
                    }
                ) {
                    Text("Submit Anyway", color = colors.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) {
                    Text("Keep Answering", color = colors.textMuted)
                }
            },
            containerColor = colors.surface
        )
    }

    // ── Dialog: Confirm Exit / Discard ───────────────────────────────────────
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = {
                Text(
                    text = "Exit Quiz?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colors.text
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to leave? Your answers for this practice exam will be lost.",
                    fontSize = 14.sp,
                    color = colors.textMuted
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        onClose()
                    }
                ) {
                    Text("Exit", color = colors.danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Cancel", color = colors.textMuted)
                }
            },
            containerColor = colors.surface
        )
    }
}

@Composable
private fun ExamQuestionCard(
    questionIndex: Int,
    question: QuizQuestion,
    selectedOption: String?,
    onSelectOption: (String) -> Unit
) {
    val colors = LocalPoruaColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeLg)
            .background(colors.surface)
            .border(1.dp, colors.border, ShapeLg)
            .padding(16.dp)
    ) {
        // Header: Question number & Chapter Tag
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Question ${questionIndex + 1}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .clip(ShapeSm)
                        .background(colors.surface2)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Ch ${question.chapterNumber}: ${question.chapterTitle.take(18)}${if (question.chapterTitle.length > 18) "..." else ""}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Question text
        Text(
            text = question.questionText,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 23.sp,
            color = colors.text
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Options A, B, C, D
        val options = listOf(
            "A" to question.optionA,
            "B" to question.optionB,
            "C" to question.optionC,
            "D" to question.optionD
        )

        options.forEach { (key, optionText) ->
            val isSelected = selectedOption == key
            val optionBg = if (isSelected) colors.primarySoft else colors.surface2
            val optionBorder = if (isSelected) colors.primary else colors.border
            val optionTextColor = if (isSelected) colors.primary else colors.text

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ShapeMd)
                    .background(optionBg)
                    .border(1.5.dp, optionBorder, ShapeMd)
                    .clickable { onSelectOption(key) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Option key badge "A"
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) colors.primary else colors.surface)
                        .border(1.dp, if (isSelected) colors.primary else colors.border, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = key,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else colors.text
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Option text
                Text(
                    text = optionText,
                    fontSize = 14.sp,
                    color = optionTextColor,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
