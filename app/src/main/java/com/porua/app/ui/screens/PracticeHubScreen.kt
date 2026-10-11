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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Whatshot
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
import com.porua.app.data.local.entity.SubjectEntity
import com.porua.app.ui.components.BottomNavItem
import com.porua.app.ui.components.MasteryRing
import com.porua.app.ui.components.OfflineBanner
import com.porua.app.ui.components.PoruaBottomNav
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.ShapeLg
import com.porua.app.ui.theme.ShapeMd
import com.porua.app.ui.viewmodel.PoruaUiState

@Composable
fun PracticeHubScreen(
    uiState: PoruaUiState,
    onNavigateBottom: (BottomNavItem) -> Unit,
    onSubjectClick: (SubjectEntity) -> Unit,
    onSmartMixClick: () -> Unit,
    onReviewDueClick: () -> Unit,
    onPracticeWithAiClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val colors = LocalPoruaColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))

                // Title & Search
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Practice",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.text
                    )

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(colors.surface)
                            .border(1.dp, colors.border, CircleShape)
                            .clickable { onSearchClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = colors.text,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Offline Banner
                OfflineBanner()

                Spacer(modifier = Modifier.height(16.dp))

                // Hero Card: Practice with Porua AI
                val aiGradient = Brush.horizontalGradient(
                    colors = listOf(colors.heroStart, colors.primary)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ShapeLg)
                        .background(aiGradient)
                        .clickable { onPracticeWithAiClick() }
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Practice with Porua AI",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Porua asks, you answer, it explains every step",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 16.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2 Action Cards in a Row: Smart mix & Review due
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Smart mix (Red gradient card)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(ShapeLg)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(colors.heroStart, colors.primary)
                                )
                            )
                            .clickable { onSmartMixClick() }
                            .padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Smart mix",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "10 Qs picked by ML from your weak topics",
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    // Review due (Yellow/Amber Card with SM-2 count)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(ShapeLg)
                            .background(colors.warningSoft)
                            .border(1.dp, colors.warning.copy(alpha = 0.2f), ShapeLg)
                            .clickable { onReviewDueClick() }
                            .padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colors.warning.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = colors.warning,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Review due",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.text
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "18 cards • SM-2 schedule",
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = colors.textMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Subjects",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Subject Items with Mastery Rings
            items(uiState.subjects) { subject ->
                SubjectRowItem(
                    subject = subject,
                    onClick = { onSubjectClick(subject) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Bottom Navigation Bar
        PoruaBottomNav(
            selectedItem = BottomNavItem.PRACTICE,
            onItemSelected = onNavigateBottom
        )
    }
}

@Composable
private fun SubjectRowItem(
    subject: SubjectEntity,
    onClick: () -> Unit
) {
    val colors = LocalPoruaColors.current

    val iconVector = when (subject.iconType) {
        "flask" -> Icons.Default.Science
        "atom" -> Icons.Default.Whatshot
        "sigma" -> Icons.Default.Functions
        else -> Icons.Default.Science
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
        // Mastery Ring
        MasteryRing(
            percentage = subject.mastery,
            size = 50.dp,
            strokeWidth = 5.dp
        )

        Spacer(modifier = Modifier.width(14.dp))

        // Subject Details
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = subject.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${subject.chaptersCount} chapters • ${subject.questionsCount} Qs",
                fontSize = 12.sp,
                color = colors.textMuted
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}
