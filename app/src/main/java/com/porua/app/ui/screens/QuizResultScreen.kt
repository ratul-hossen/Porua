package com.porua.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.porua.app.ui.components.PoruaAiButton
import com.porua.app.ui.components.PoruaFilterChip
import com.porua.app.ui.components.PoruaPrimaryButton
import com.porua.app.ui.components.PoruaSecondaryButton
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.ShapeFull
import com.porua.app.ui.theme.ShapeLg
import com.porua.app.ui.theme.ShapeMd
import com.porua.app.ui.theme.ShapeSm
import com.porua.app.ui.viewmodel.ChapterAccuracy
import com.porua.app.ui.viewmodel.QuizQuestion
import com.porua.app.ui.viewmodel.QuizReviewItem
import com.porua.app.ui.viewmodel.QuizUiState
import com.porua.app.ui.viewmodel.ReviewFilter

@Composable
fun QuizResultScreen(
    state: QuizUiState,
    onFilterChange: (ReviewFilter) -> Unit,
    onRetryWrong: () -> Unit,
    onNewSet: () -> Unit,
    onExplainAi: (QuizQuestion) -> Unit,
    onBackToPractice: () -> Unit
) {
    val colors = LocalPoruaColors.current
    val result = state.result ?: return

    val minutes = result.timeTakenSeconds / 60
    val seconds = result.timeTakenSeconds % 60
    val timeFormatted = if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"

    val filteredItems = when (state.reviewFilter) {
        ReviewFilter.ALL -> result.reviewItems
        ReviewFilter.WRONG_ONLY -> result.reviewItems.filter { !it.isCorrect && !it.isSkipped }
        ReviewFilter.SKIPPED -> result.reviewItems.filter { it.isSkipped }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Exam Results",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.text
                    )
                    Text(
                        text = state.subjectName,
                        fontSize = 13.sp,
                        color = colors.textMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.surface2)
                        .clickable { onBackToPractice() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.text,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Big Score Card ───────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ShapeLg)
                    .background(colors.surface)
                    .border(1.dp, colors.border, ShapeLg)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Score Percentage
                val scoreColor = when {
                    result.percentage >= 80 -> colors.success
                    result.percentage >= 50 -> colors.primary
                    else -> colors.danger
                }

                Text(
                    text = "${result.percentage}%",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = scoreColor
                )

                Text(
                    text = when {
                        result.percentage >= 80 -> "Excellent! HSC Exam Ready"
                        result.percentage >= 50 -> "Good Effort! Review Weak Topics"
                        else -> "Needs Revision — Practice Below"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Metric Cards: Score, Correct, Wrong, Skipped, Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricBox(label = "Score", value = "${result.score}/${result.totalQuestions}", tint = colors.text)
                    MetricBox(label = "Correct", value = "${result.score}", tint = colors.success)
                    MetricBox(label = "Wrong", value = "${result.wrongCount}", tint = colors.danger)
                    MetricBox(label = "Skipped", value = "${result.skippedCount}", tint = colors.textMuted)
                    MetricBox(label = "Time", value = timeFormatted, tint = colors.primary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Breakdown by Chapter ─────────────────────────────────────────
            Text(
                text = "Accuracy by Chapter",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ShapeLg)
                    .background(colors.surface)
                    .border(1.dp, colors.border, ShapeLg)
                    .padding(16.dp)
            ) {
                result.chapterBreakdowns.forEach { ch ->
                    ChapterAccuracyRow(accuracy = ch)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Review Answers Filter Tabs ───────────────────────────────────
            Text(
                text = "Review Answers",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PoruaFilterChip(
                    text = "All (${result.totalQuestions})",
                    isSelected = state.reviewFilter == ReviewFilter.ALL,
                    onClick = { onFilterChange(ReviewFilter.ALL) }
                )
                PoruaFilterChip(
                    text = "Wrong (${result.wrongCount})",
                    isSelected = state.reviewFilter == ReviewFilter.WRONG_ONLY,
                    onClick = { onFilterChange(ReviewFilter.WRONG_ONLY) },
                    activeColor = colors.danger
                )
                PoruaFilterChip(
                    text = "Skipped (${result.skippedCount})",
                    isSelected = state.reviewFilter == ReviewFilter.SKIPPED,
                    onClick = { onFilterChange(ReviewFilter.SKIPPED) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Review Question Cards ────────────────────────────────────────────
        items(filteredItems) { reviewItem ->
            ReviewQuestionCard(
                reviewItem = reviewItem,
                aiExplanation = state.aiExplanations[reviewItem.question.id],
                isAiLoading = state.aiLoadingQuestionId == reviewItem.question.id,
                onExplainAi = { onExplainAi(reviewItem.question) }
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // ── Bottom Action Buttons (Adaptive 100% Next vs Learn from Mistakes Loop) ──
        item {
            Spacer(modifier = Modifier.height(16.dp))

            val isPerfectScore = result.score == result.totalQuestions && result.totalQuestions > 0

            if (isPerfectScore) {
                // 100% Score: Celebratory Next Session Path
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ShapeLg)
                        .background(colors.surface)
                        .border(1.dp, colors.success.copy(alpha = 0.5f), ShapeLg)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎉 100% Perfect Mastery!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.success
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Outstanding! You got all ${result.totalQuestions} questions correct. Ready for the next challenge?",
                        fontSize = 13.sp,
                        color = colors.textMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                PoruaPrimaryButton(
                    text = "NEXT SESSION ➔ (পরবর্তী সেশন)",
                    onClick = onNewSet
                )

                Spacer(modifier = Modifier.height(10.dp))

                PoruaSecondaryButton(
                    text = "Back to Practice",
                    onClick = onBackToPractice
                )
            } else {
                // Mistakes Made: Dual-Branch (Learn from Mistakes vs Skip)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ShapeLg)
                        .background(colors.surface)
                        .border(1.dp, colors.primary.copy(alpha = 0.3f), ShapeLg)
                        .padding(14.dp)
                ) {
                    Text(
                        text = "💡 Adaptive Remedial Practice",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Practice a new targeted 20-question set focused on the topics you missed (${result.wrongCount} mistakes).",
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                PoruaPrimaryButton(
                    text = "✦ Learn from Mistakes (ভুল থেকে শেখো)",
                    onClick = onRetryWrong
                )

                Spacer(modifier = Modifier.height(10.dp))

                PoruaSecondaryButton(
                    text = "Skip ➔ (স্কিপ)",
                    onClick = onNewSet
                )

                Spacer(modifier = Modifier.height(10.dp))

                PoruaSecondaryButton(
                    text = "Back to Practice",
                    onClick = onBackToPractice
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun MetricBox(label: String, value: String, tint: Color) {
    val colors = LocalPoruaColors.current
    Column(
        modifier = Modifier
            .clip(ShapeSm)
            .background(colors.surface2)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = tint)
        Text(text = label, fontSize = 11.sp, color = colors.textMuted)
    }
}

@Composable
private fun ChapterAccuracyRow(accuracy: ChapterAccuracy) {
    val colors = LocalPoruaColors.current
    val barColor = when {
        accuracy.percentage >= 80 -> colors.success
        accuracy.percentage >= 50 -> colors.primary
        else -> colors.danger
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Ch ${accuracy.chapterNumber}: ${accuracy.chapterTitle}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${accuracy.correctCount}/${accuracy.totalCount} (${accuracy.percentage}%)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { accuracy.percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(ShapeFull),
            color = barColor,
            trackColor = colors.surface2
        )
    }
}

@Composable
private fun ReviewQuestionCard(
    reviewItem: QuizReviewItem,
    aiExplanation: String?,
    isAiLoading: Boolean,
    onExplainAi: () -> Unit
) {
    val colors = LocalPoruaColors.current
    val question = reviewItem.question
    val userAns = reviewItem.selectedOption
    val correctAns = question.correctOption

    val cardBorder = when {
        reviewItem.isCorrect -> colors.success.copy(alpha = 0.5f)
        reviewItem.isSkipped -> colors.border
        else -> colors.danger.copy(alpha = 0.5f)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeLg)
            .background(colors.surface)
            .border(1.dp, cardBorder, ShapeLg)
            .padding(16.dp)
    ) {
        // Status badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Question ${question.index}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text
            )

            val statusText = when {
                reviewItem.isCorrect -> "Correct"
                reviewItem.isSkipped -> "Skipped"
                else -> "Incorrect"
            }
            val statusBg = when {
                reviewItem.isCorrect -> colors.successSoft
                reviewItem.isSkipped -> colors.surface2
                else -> colors.dangerSoft
            }
            val statusColor = when {
                reviewItem.isCorrect -> colors.success
                reviewItem.isSkipped -> colors.textMuted
                else -> colors.danger
            }

            Box(
                modifier = Modifier
                    .clip(ShapeSm)
                    .background(statusBg)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Question text
        Text(
            text = question.questionText,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.text
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Options review
        val options = listOf(
            "A" to question.optionA,
            "B" to question.optionB,
            "C" to question.optionC,
            "D" to question.optionD
        )

        options.forEach { (key, optText) ->
            val isUserChoice = userAns == key
            val isTargetCorrect = correctAns.equals(key, ignoreCase = true)

            val optBg = when {
                isTargetCorrect -> colors.successSoft
                isUserChoice && !reviewItem.isCorrect -> colors.dangerSoft
                else -> colors.surface2
            }
            val optBorder = when {
                isTargetCorrect -> colors.success
                isUserChoice && !reviewItem.isCorrect -> colors.danger
                else -> colors.border
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ShapeMd)
                    .background(optBg)
                    .border(1.dp, optBorder, ShapeMd)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$key.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isTargetCorrect) colors.success else if (isUserChoice) colors.danger else colors.text
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = optText,
                    fontSize = 13.sp,
                    color = colors.text,
                    modifier = Modifier.weight(1f)
                )

                if (isTargetCorrect) {
                    Text(
                        text = "Correct Answer",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.success
                    )
                } else if (isUserChoice) {
                    Text(
                        text = "Your Answer",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.danger
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Explanation card when user clicks "Explain"
        if (aiExplanation != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ShapeMd)
                    .background(colors.surface2)
                    .border(1.dp, colors.primary.copy(alpha = 0.5f), ShapeMd)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Explanation",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                    }

                    Text(
                        text = "Hide",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textMuted,
                        modifier = Modifier
                            .clickable { onExplainAi() }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = aiExplanation,
                    fontSize = 12.sp,
                    color = colors.text,
                    lineHeight = 17.sp
                )
            }
        } else if (isAiLoading) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = colors.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Loading explanation...", fontSize = 12.sp, color = colors.textMuted)
            }
        } else {
            PoruaAiButton(
                text = "Explain",
                onClick = onExplainAi,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
