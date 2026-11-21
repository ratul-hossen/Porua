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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.porua.app.data.content.ContentChapter
import com.porua.app.data.content.ParsedMcq
import com.porua.app.data.content.ParsedShortQuestion
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.ShapeLg
import com.porua.app.ui.theme.ShapeSm

/**
 * Practice screen for content loaded from JSON assets.
 * Supports both MCQ and Short Question modes.
 *
 * @param mode  "mcq" | "short"
 */
@Composable
fun ContentPracticeScreen(
    chapter: ContentChapter,
    mode: String,
    onClose: () -> Unit
) {
    val colors = LocalPoruaColors.current

    if (mode == "mcq") {
        McqPracticeView(chapter = chapter, onClose = onClose)
    } else {
        ShortQPracticeView(chapter = chapter, onClose = onClose)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  MCQ Practice
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun McqPracticeView(chapter: ContentChapter, onClose: () -> Unit) {
    val colors = LocalPoruaColors.current
    val mcqs = chapter.mcqs

    if (mcqs.isEmpty()) {
        EmptyState("No MCQs found in this chapter.", onClose)
        return
    }

    var currentIndex by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isChecked by remember { mutableStateOf(false) }

    val question = mcqs[currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // ── Top bar ──────────────────────────────────────────────────────────
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
                Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.text, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(ShapeLg)
                    .background(colors.surface2)
            ) {
                val fraction = ((currentIndex + 1).toFloat() / mcqs.size).coerceIn(0.01f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(6.dp)
                        .clip(ShapeLg)
                        .background(colors.primary)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${currentIndex + 1}/${mcqs.size}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textMuted
            )
        }

        // ── Question body ─────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Tags
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TagPill(chapter.title)
                TagPill("MCQ")
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = question.questionText,
                fontSize = 18.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.text
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Options
            listOf(
                "A" to question.optionA,
                "B" to question.optionB,
                "C" to question.optionC,
                "D" to question.optionD
            ).forEach { (key, text) ->
                if (text.isNotBlank()) {
                    McqOptionCard(
                        key = key,
                        text = text,
                        isSelected = selectedOption == key,
                        isChecked = isChecked,
                        isCorrect = question.correctOption == key,
                        onClick = { if (!isChecked) selectedOption = key }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Feedback
            if (isChecked) {
                Spacer(modifier = Modifier.height(12.dp))
                val correct = selectedOption == question.correctOption
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ShapeLg)
                        .background(if (correct) colors.successSoft else colors.dangerSoft)
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (correct) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (correct) colors.success else colors.danger,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (correct) "Correct answer! 🎉" else "Incorrect. Correct answer: ${question.correctOption}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (correct) colors.success else colors.danger
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Bottom buttons ────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!isChecked) {
                // Skip
                Box(
                    modifier = Modifier
                        .weight(0.35f)
                        .height(50.dp)
                        .clip(ShapeLg)
                        .background(colors.surface)
                        .border(1.dp, colors.border, ShapeLg)
                        .clickable {
                            if (currentIndex + 1 < mcqs.size) {
                                currentIndex++
                                selectedOption = null
                                isChecked = false
                            } else onClose()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Skip", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.text)
                }

                // Check answer
                Box(
                    modifier = Modifier
                        .weight(0.65f)
                        .height(50.dp)
                        .clip(ShapeLg)
                        .background(if (selectedOption != null) colors.primary else colors.surface2)
                        .clickable(enabled = selectedOption != null) { isChecked = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Check Answer",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedOption != null) Color.White else colors.textMuted
                    )
                }
            } else {
                // Next question / Finish
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(ShapeLg)
                        .background(colors.primary)
                        .clickable {
                            if (currentIndex + 1 < mcqs.size) {
                                currentIndex++
                                selectedOption = null
                                isChecked = false
                            } else onClose()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (currentIndex + 1 < mcqs.size) "Next Question" else "Finish",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (currentIndex + 1 < mcqs.size) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

private data class McqOptionStyle(
    val bg: androidx.compose.ui.graphics.Color,
    val border: androidx.compose.ui.graphics.Color,
    val letterBg: androidx.compose.ui.graphics.Color,
    val letterText: androidx.compose.ui.graphics.Color
)

@Composable
private fun McqOptionCard(
    key: String,
    text: String,
    isSelected: Boolean,
    isChecked: Boolean,
    isCorrect: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalPoruaColors.current

    val style = when {
        isChecked && isCorrect                -> McqOptionStyle(colors.successSoft, colors.success, colors.success, Color.White)
        isChecked && isSelected && !isCorrect -> McqOptionStyle(colors.dangerSoft, colors.danger, colors.danger, Color.White)
        isSelected                            -> McqOptionStyle(colors.primarySoft.copy(alpha = 0.4f), colors.primary, colors.primary, Color.White)
        else                                  -> McqOptionStyle(colors.surface, colors.border, colors.surface2, colors.text)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeLg)
            .background(style.bg)
            .border(if (isSelected || (isChecked && isCorrect)) 2.dp else 1.dp, style.border, ShapeLg)
            .clickable(enabled = !isChecked) { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(style.letterBg),
            contentAlignment = Alignment.Center
        ) {
            if (isChecked && isCorrect) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            } else if (isChecked && isSelected && !isCorrect) {
                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            } else {
                Text(key, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = style.letterText)
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.text, modifier = Modifier.weight(1f))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Short Question Practice
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ShortQPracticeView(chapter: ContentChapter, onClose: () -> Unit) {
    val colors = LocalPoruaColors.current
    val questions = chapter.shortQuestions

    if (questions.isEmpty()) {
        EmptyState("No short questions found in this chapter.", onClose)
        return
    }

    var currentIndex by remember { mutableStateOf(0) }
    var showAnswer by remember { mutableStateOf(false) }

    val question: ParsedShortQuestion = questions[currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // ── Top bar ──────────────────────────────────────────────────────────
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
                Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.text, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(ShapeLg)
                    .background(colors.surface2)
            ) {
                val fraction = ((currentIndex + 1).toFloat() / questions.size).coerceIn(0.01f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(6.dp)
                        .clip(ShapeLg)
                        .background(colors.warning)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${currentIndex + 1}/${questions.size}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textMuted
            )
        }

        // ── Question body ─────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TagPill(chapter.title)
                TagPill("Short Q")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Question ${currentIndex + 1}",
                fontSize = 12.sp,
                color = colors.textMuted
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = question.question,
                fontSize = 20.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.text
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (showAnswer && question.answer.isNotBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ShapeLg)
                        .background(colors.successSoft)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "✅ Answer",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.success
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = question.answer,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = colors.text
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Bottom buttons ────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (!showAnswer) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(ShapeLg)
                        .background(colors.warning)
                        .clickable { showAnswer = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Show Answer", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(ShapeLg)
                        .background(colors.primary)
                        .clickable {
                            if (currentIndex + 1 < questions.size) {
                                currentIndex++
                                showAnswer = false
                            } else onClose()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (currentIndex + 1 < questions.size) "Next Question" else "Finish",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (currentIndex + 1 < questions.size) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Shared helpers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TagPill(text: String) {
    val colors = LocalPoruaColors.current
    Box(
        modifier = Modifier
            .clip(ShapeSm)
            .background(colors.surface2)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = text, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = colors.textMuted)
    }
}

@Composable
private fun EmptyState(message: String, onClose: () -> Unit) {
    val colors = LocalPoruaColors.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = message, fontSize = 16.sp, color = colors.textMuted)
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(ShapeLg)
                    .background(colors.primary)
                    .clickable { onClose() }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text("Go Back", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
