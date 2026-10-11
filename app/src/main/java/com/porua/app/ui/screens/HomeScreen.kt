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
import com.porua.app.ui.components.AiInsightCard
import com.porua.app.ui.components.BottomNavItem
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
    onPracticeTask: (StudyTaskEntity) -> Unit
) {
    val colors = LocalPoruaColors.current
    val profile = uiState.userProfile

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
                    Column {
                        Text(
                            text = "Good evening, ${profile.name}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.text
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${profile.examTrack} ${profile.examYear} • ${profile.group} • 🔥 ${profile.streakDays}-day streak",
                            fontSize = 12.sp,
                            color = colors.textMuted
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
                            Column {
                                Text(
                                    text = "${profile.daysRemaining} days",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "to ${profile.examTrack} • ${profile.examDate}",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
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
                                    .fillMaxWidth(fraction = profile.examReadyPercentage / 100f)
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
                    Text(
                        text = "Today's plan • 45 min",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
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

                // AI Insight Card
                AiInsightCard(
                    text = "Electrode potential is likely to slip below 50% in 3 days. One 10-question set today keeps it on track.",
                    onClickSeeWhy = onSeeAiInsights
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Section: Weak areas & Ask Porua button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Weak areas",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.text
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PoruaWeakBadge("Alcohols 34%")
                            PoruaWeakBadge("Electrolysis 31%")
                        }
                    }

                    // Ask Porua Pill Button
                    PoruaAiButton(
                        text = "Ask Porua",
                        onClick = onAskPorua,
                        isPill = true
                    )
                }

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
