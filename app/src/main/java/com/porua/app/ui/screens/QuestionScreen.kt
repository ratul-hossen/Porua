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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.porua.app.data.local.entity.QuestionEntity
import com.porua.app.domain.sm2.Sm2Rating
import com.porua.app.ui.components.PoruaAiButton
import com.porua.app.ui.components.PoruaPrimaryButton
import com.porua.app.ui.components.PoruaSecondaryButton
import com.porua.app.ui.components.Sm2RatingBar
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.ShapeFull
import com.porua.app.ui.theme.ShapeLg
import com.porua.app.ui.theme.ShapeMd
import com.porua.app.ui.theme.ShapeSm
import com.porua.app.ui.viewmodel.PoruaUiState

@Composable
fun QuestionScreen(
    uiState: PoruaUiState,
    onClose: () -> Unit,
    onSelectOption: (String) -> Unit,
    onCheckAnswer: () -> Unit,
    onSkip: () -> Unit,
    onApplySm2Rating: (Sm2Rating) -> Unit,
    onExplainWithAi: () -> Unit,
    onNextQuestion: () -> Unit
) {
    val colors = LocalPoruaColors.current
    val scrollState = rememberScrollState()

    val questions = uiState.currentQuestions
    val currentIdx = uiState.currentQuestionIndex.coerceIn(0, (questions.size - 1).coerceAtLeast(0))
    val question: QuestionEntity? = questions.getOrNull(currentIdx)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Bar: Close 'X', Progress Bar, Timer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.surface2)
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = colors.text,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(ShapeFull)
                    .background(colors.surface2)
            ) {
                val totalQ = if (questions.isNotEmpty()) questions.size else 10
                val progressFraction = ((currentIdx + 1).toFloat() / totalQ).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progressFraction)
                        .height(6.dp)
                        .clip(ShapeFull)
                        .background(colors.primary)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Timer "0:42"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "0:${uiState.quizSecondsElapsed.toString().padStart(2, '0')}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textMuted
                )
            }
        }

        // Question Body
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Tags: Subject, Topic, Difficulty
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TagChip(text = "Chemistry • Ch 4")
                TagChip(text = question?.topicName ?: "Electrode potential")
                DifficultyTag(difficulty = question?.difficulty ?: "Medium")
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Question ${currentIdx + 1} of ${if (questions.isNotEmpty()) questions.size else 10}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = question?.questionText
                    ?: "The standard hydrogen electrode (SHE) potential at 25 °C is...",
                fontSize = 18.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.text
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Options A, B, C, D
            val options = listOf(
                "A" to (question?.optionA ?: "1.00 V"),
                "B" to (question?.optionB ?: "0.00 V"),
                "C" to (question?.optionC ?: "-0.76 V"),
                "D" to (question?.optionD ?: "+0.34 V")
            )

            options.forEach { (key, optionText) ->
                OptionCard(
                    key = key,
                    text = optionText,
                    isSelected = uiState.selectedOption == key,
                    isAnswerChecked = uiState.isAnswerChecked,
                    isCorrectOption = (question?.correctOption ?: "B") == key,
                    onClick = { onSelectOption(key) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // If Answer Checked: Show Explanation Card & Explain with AI
            if (uiState.isAnswerChecked) {
                Spacer(modifier = Modifier.height(14.dp))

                val isCorrect = uiState.isAnswerCorrect
                val explanationBg = if (isCorrect) colors.successSoft else colors.dangerSoft
                val explanationText = if (isCorrect) colors.success else colors.danger

                // Feedback Explanation Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ShapeLg)
                        .background(explanationBg)
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = explanationText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isCorrect) "Correct! Excellent" else "Not quite — correct answer is ${question?.correctOption ?: "B"}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = explanationText
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = question?.explanation
                            ?: "SHE is the reference electrode. Every other electrode potential is measured against it, so its value is fixed at 0.00 V by definition.",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = colors.text
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Explain with AI button
                PoruaAiButton(
                    text = "Explain with AI",
                    onClick = onExplainWithAi,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // SM-2 Spaced Repetition Ratings
                Sm2RatingBar(
                    onRatingSelected = { rating ->
                        onApplySm2Rating(rating)
                        onNextQuestion()
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))
            } else {
                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Offline info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = colors.textMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Offline • 312 Chemistry questions on this phone",
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Bottom Action Buttons (Only when answer is not yet checked)
        if (!uiState.isAnswerChecked) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PoruaSecondaryButton(
                    text = "Skip",
                    onClick = onSkip,
                    modifier = Modifier.weight(0.35f),
                    height = 50.dp
                )

                PoruaPrimaryButton(
                    text = "Check answer",
                    onClick = onCheckAnswer,
                    enabled = uiState.selectedOption != null,
                    modifier = Modifier.weight(0.65f),
                    height = 50.dp
                )
            }
        }
    }
}

@Composable
private fun OptionCard(
    key: String,
    text: String,
    isSelected: Boolean,
    isAnswerChecked: Boolean,
    isCorrectOption: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalPoruaColors.current

    val (borderColor, bgColor, letterBg, letterColor) = when {
        isAnswerChecked && isCorrectOption -> Quad(
            colors.success,
            colors.successSoft,
            colors.success,
            Color.White
        )
        isAnswerChecked && isSelected && !isCorrectOption -> Quad(
            colors.danger,
            colors.dangerSoft,
            colors.danger,
            Color.White
        )
        isSelected -> Quad(
            colors.primary,
            colors.primarySoft.copy(alpha = 0.4f),
            colors.primary,
            Color.White
        )
        else -> Quad(
            colors.border,
            colors.surface,
            colors.surface2,
            colors.text
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeLg)
            .background(bgColor)
            .border(if (isSelected || (isAnswerChecked && isCorrectOption)) 2.dp else 1.dp, borderColor, ShapeLg)
            .clickable(enabled = !isAnswerChecked) { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circle with A / B / C / D
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(letterBg),
            contentAlignment = Alignment.Center
        ) {
            if (isAnswerChecked && isCorrectOption) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            } else if (isAnswerChecked && isSelected && !isCorrectOption) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = key,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = letterColor
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.text,
            modifier = Modifier.weight(1f)
        )
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun TagChip(text: String) {
    val colors = LocalPoruaColors.current
    Box(
        modifier = Modifier
            .clip(ShapeSm)
            .background(colors.surface2)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textMuted
        )
    }
}

@Composable
private fun DifficultyTag(difficulty: String) {
    val colors = LocalPoruaColors.current
    Box(
        modifier = Modifier
            .clip(ShapeSm)
            .background(colors.warningSoft)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = difficulty,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.warning
        )
    }
}
