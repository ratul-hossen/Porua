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
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrackChanges
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.porua.app.data.local.entity.StudyTaskEntity
import com.porua.app.domain.countdown.CountdownResult
import org.json.JSONArray
import com.porua.app.ui.components.AiInsightCard
import com.porua.app.ui.components.BottomNavItem
import com.porua.app.ui.components.ExamYearSetupDialog
import com.porua.app.ui.components.PoruaAiButton
import com.porua.app.ui.components.PoruaBottomNav
import com.porua.app.ui.components.PoruaWeakBadge
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.ShapeFull
import com.porua.app.ui.theme.ShapeLg
import com.porua.app.ui.theme.ShapeMd
import com.porua.app.ui.viewmodel.PoruaUiState

@Composable
fun HomeScreen(
    uiState: PoruaUiState,
    onNavigateBottom: (BottomNavItem) -> Unit,
    onToggleTask: (Long, Boolean) -> Unit,
    onSeeAllTasks: () -> Unit,
    onSeeAiInsights: () -> Unit,
    onAskPorua: () -> Unit,
    onPracticeTask: (StudyTaskEntity) -> Unit,
    onSetExamYear: (String) -> Unit = {}
) {
    val colors = LocalPoruaColors.current
    val profile = uiState.userProfile
    var showYearDialog by remember { mutableStateOf(false) }

    if (!uiState.isExamYearConfigured || showYearDialog) {
        ExamYearSetupDialog(
            initialYear = profile.examYear,
            onYearSelected = { year ->
                onSetExamYear(year)
                showYearDialog = false
            },
            onDismissRequest = if (uiState.isExamYearConfigured) {
                { showYearDialog = false }
            } else null
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        // Main Scrollable Body
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))

                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                    val greetingTime = when (currentHour) {
                        in 5..11 -> "Good morning"
                        in 12..16 -> "Good afternoon"
                        in 17..21 -> "Good evening"
                        else -> "Hello"
                    }
                    val greeting = if (profile.name.isNotBlank()) "$greetingTime, ${profile.name}" else greetingTime

                    Column {
                        Text(
                            text = greeting,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.text
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val streakLabel = if (profile.streakDays > 0) "🔥 ${profile.streakDays}-day streak" else "🌱 Day 1"
                        Text(
                            text = "${profile.examTrack} ${profile.examYear} • ${profile.group} • $streakLabel",
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }

                    // Action Buttons (Ask Porua AI + Notifications)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Top Right Ask Porua AI Chat Button
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(colors.primarySoft)
                                .border(1.dp, colors.primary.copy(alpha = 0.35f), CircleShape)
                                .clickable { onAskPorua() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Ask Porua AI Tutor",
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Bell Notification Button
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(colors.surface)
                                .border(1.dp, colors.border, CircleShape)
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = "Notifications",
                                tint = colors.text,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Hero Countdown Gradient Card
                val heroGradient = Brush.horizontalGradient(
                    colors = listOf(colors.heroStart, colors.heroEnd)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ShapeLg)
                        .background(heroGradient)
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            when (val countdown = uiState.countdownResult) {
                                is CountdownResult.Upcoming -> {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${countdown.daysRemaining}",
                                            fontSize = 36.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Days left",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${profile.examTrack} ${countdown.examYear} • April",
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                                is CountdownResult.Today -> {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Exam starts today!",
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Best of luck with ${profile.examTrack} ${countdown.examYear}",
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                }
                                is CountdownResult.Passed -> {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { showYearDialog = true }
                                    ) {
                                        Text(
                                            text = "Exam date has passed",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Tap to choose your next exam year →",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White.copy(alpha = 0.95f)
                                        )
                                    }
                                }
                            }

                            // 58% exam-ready pill
                            Column(
                                modifier = Modifier
                                    .clip(ShapeMd)
                                    .background(Color.White.copy(alpha = 0.18f))
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${profile.examReadyPercentage}%",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "exam-ready",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Progress line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(ShapeFull)
                                .background(Color.White.copy(alpha = 0.25f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = (profile.examReadyPercentage / 100f).coerceIn(0.01f, 1f))
                                    .height(4.dp)
                                    .clip(ShapeFull)
                                    .background(Color.White)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section: Today's plan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Today's plan • ${uiState.userProfile.dailyStudyTime.ifBlank { "1 hour" }}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.text
                        )
                        val daysLeft = if (uiState.userProfile.daysRemaining > 0) uiState.userProfile.daysRemaining else 400
                        val targetYear = uiState.userProfile.examYear.ifBlank { "2027" }
                        Text(
                            text = "$daysLeft days to HSC $targetYear • Daily Syllabus Target",
                            fontSize = 11.sp,
                            color = colors.textMuted
                        )
                    }
                    Text(
                        text = "See all",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.primary,
                        modifier = Modifier.clickable { onSeeAllTasks() }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Task Items
            items(uiState.studyTasks) { task ->
                StudyTaskRow(
                    task = task,
                    onToggle = { onToggleTask(task.id, task.isCompleted) },
                    onClick = { onPracticeTask(task) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                Spacer(modifier = Modifier.height(14.dp))

                // Section: Weak areas & Ask Porua button
                val mistakeChapters = remember(uiState.quizAttempts) {
                    val map = mutableMapOf<String, MistakeChapterInfo>()
                    uiState.quizAttempts.forEach { attempt ->
                        try {
                            val arr = JSONArray(attempt.questionResultsJson)
                            for (i in 0 until arr.length()) {
                                val obj = arr.getJSONObject(i)
                                val chNum = obj.optInt("chapterNumber", 1)
                                val chTitle = obj.optString("chapterTitle", "Chapter $chNum")
                                val isCorrect = obj.optBoolean("isCorrect", false)
                                val isSkipped = obj.optBoolean("isSkipped", false)
                                if (!isCorrect && !isSkipped) {
                                    val key = "${attempt.subjectId.lowercase()}_$chNum"
                                    val existing = map[key]
                                    val count = (existing?.mistakes ?: 0) + 1
                                    map[key] = MistakeChapterInfo(
                                        subjectName = attempt.subjectName,
                                        chapterTitle = chTitle,
                                        chapterNumber = chNum,
                                        mistakes = count
                                    )
                                }
                            }
                        } catch (_: Exception) {}
                    }
                    map.values.sortedByDescending { it.mistakes }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Weak areas",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )

                    // Ask Porua Pill Button placed neatly on the right
                    PoruaAiButton(
                        text = "Ask Porua",
                        onClick = onAskPorua,
                        isPill = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Chart displaying mistake distribution across subjects and chapters
                WeakAreasChartCard(mistakes = mistakeChapters)

                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // Bottom Navigation Bar
        PoruaBottomNav(
            selectedItem = BottomNavItem.HOME,
            onItemSelected = onNavigateBottom
        )
    }
}

@Composable
private fun StudyTaskRow(
    task: StudyTaskEntity,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    val colors = LocalPoruaColors.current

    val (icon, iconTint, iconBg) = when (task.title) {
        "Alcohols" -> Triple(Icons.Default.Refresh, colors.text, colors.surface2)
        "Electrode potential" -> Triple(Icons.Default.TrackChanges, colors.danger, colors.dangerSoft)
        else -> Triple(Icons.Default.MenuBook, colors.success, colors.successSoft)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeLg)
            .background(colors.surface)
            .border(1.dp, colors.border, ShapeLg)
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Icon
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Center Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text
            )
            val subtitle = buildString {
                append("${task.subject} • ${task.durationMinutes} min")
                if (task.questionCount > 0) {
                    append(" • ${task.questionCount} Qs")
                }
            }
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = colors.textMuted
            )
        }

        // Right Complete Checkbox
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (task.isCompleted) colors.success else Color.Transparent)
                .border(2.dp, if (task.isCompleted) colors.success else colors.border, CircleShape)
                .clickable { onToggle() },
            contentAlignment = Alignment.Center
        ) {
            if (task.isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Done",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

data class MistakeChapterInfo(
    val subjectName: String,
    val chapterTitle: String,
    val chapterNumber: Int,
    val mistakes: Int
)

@Composable
private fun WeakAreasChartCard(
    mistakes: List<MistakeChapterInfo>
) {
    val colors = LocalPoruaColors.current
    val maxMistakes = mistakes.maxOfOrNull { it.mistakes }?.coerceAtLeast(1) ?: 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeLg)
            .background(colors.surface)
            .border(1.dp, colors.border, ShapeLg)
            .padding(16.dp)
    ) {
        if (mistakes.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mistake Distribution by Chapter",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textMuted
                )
                Text(
                    text = "${mistakes.sumOf { it.mistakes }} total errors",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.danger
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Render up to 5 mistake chapters with proportional visual bars
            mistakes.take(5).forEachIndexed { index, item ->
                val barFraction = (item.mistakes.toFloat() / maxMistakes.toFloat()).coerceIn(0.1f, 1f)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${item.subjectName}: ${item.chapterTitle}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.text,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${item.mistakes} errors",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.danger
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Progress bar track and fill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(ShapeFull)
                            .background(colors.surface2)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(barFraction)
                                .height(8.dp)
                                .clip(ShapeFull)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(colors.warning, colors.danger)
                                    )
                                )
                        )
                    }
                }

                if (index < mistakes.take(5).size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎉 No Weak Areas Yet",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Practice chapter quizzes to see your mistake distribution chart here.",
                    fontSize = 12.sp,
                    color = colors.textMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
