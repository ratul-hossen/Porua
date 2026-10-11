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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Insights
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.porua.app.ui.components.BottomNavItem
import com.porua.app.ui.components.MasteryRing
import com.porua.app.ui.components.PoruaBottomNav
import com.porua.app.ui.components.PoruaFilterChip
import com.porua.app.ui.components.TopicBar
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.ShapeFull
import com.porua.app.ui.theme.ShapeLg
import com.porua.app.ui.theme.ShapeMd
import com.porua.app.ui.theme.ShapeSm
import com.porua.app.ui.theme.StatusDanger
import com.porua.app.ui.theme.StatusSuccess
import com.porua.app.ui.theme.StatusWarning
import com.porua.app.ui.theme.getMasteryColor
import com.porua.app.ui.viewmodel.PoruaUiState

@Composable
fun ProgressScreen(
    uiState: PoruaUiState,
    onNavigateBottom: (BottomNavItem) -> Unit,
    onOpenAiInsights: () -> Unit
) {
    val colors = LocalPoruaColors.current
    var selectedFilter by remember { mutableStateOf("All") }

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

                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mastery Map",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.text
                    )

                    // Open AI Insights shortcut
                    Row(
                        modifier = Modifier
                            .clip(ShapeSm)
                            .background(colors.surface)
                            .border(1.dp, colors.border, ShapeSm)
                            .clickable { onOpenAiInsights() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Insights,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Insights",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Overall Readiness Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ShapeLg)
                        .background(colors.surface)
                        .border(1.dp, colors.border, ShapeLg)
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MasteryRing(
                        percentage = uiState.userProfile.examReadyPercentage,
                        size = 64.dp,
                        strokeWidth = 7.dp
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "${uiState.userProfile.examReadyPercentage}% Overall Readiness",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.text
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Target: ${uiState.userProfile.examTrack} ${uiState.userProfile.examYear} • ${uiState.userProfile.daysRemaining} days remaining",
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Filter Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf("All", "Needs Revision", "On Track", "Mastered")
                    items(filters) { filter ->
                        PoruaFilterChip(
                            text = filter,
                            isSelected = selectedFilter == filter,
                            onClick = { selectedFilter = filter }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section: Weak Topics Needing Urgent Focus
                Text(
                    text = "Weak Topics (Action Needed)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Weak Topics List
            val weakTopics = listOf(
                Triple("Electrolysis", "Chemistry • Ch 4", 31),
                Triple("Alcohols", "Chemistry • Ch 2", 34),
                Triple("Electrochemistry", "Chemistry • Ch 4", 48),
                Triple("Integration by parts", "Higher Math • Ch 10", 52)
            )

            items(weakTopics) { (topic, sub, score) ->
                WeakTopicRow(topic = topic, subjectDesc = sub, mastery = score)
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))

                // Section: Subject Mastery
                Text(
                    text = "Subject Performance",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Subject Cards
            items(uiState.subjects) { subject ->
                SubjectMasteryCard(subjectName = subject.name, mastery = subject.mastery)
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        PoruaBottomNav(
            selectedItem = BottomNavItem.PROGRESS,
            onItemSelected = onNavigateBottom
        )
    }
}

@Composable
private fun WeakTopicRow(
    topic: String,
    subjectDesc: String,
    mastery: Int
) {
    val colors = LocalPoruaColors.current
    val masteryColor = getMasteryColor(mastery)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeLg)
            .background(colors.surface)
            .border(1.dp, colors.border, ShapeLg)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.dangerSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.TrackChanges,
                contentDescription = null,
                tint = colors.danger,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = topic,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text
            )
            Text(
                text = subjectDesc,
                fontSize = 11.sp,
                color = colors.textMuted
            )
        }

        Box(
            modifier = Modifier
                .clip(ShapeSm)
                .background(masteryColor.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "$mastery% mastery",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = masteryColor
            )
        }
    }
}

@Composable
private fun SubjectMasteryCard(
    subjectName: String,
    mastery: Int
) {
    val colors = LocalPoruaColors.current
    val masteryColor = getMasteryColor(mastery)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeLg)
            .background(colors.surface)
            .border(1.dp, colors.border, ShapeLg)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = subjectName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text
            )
            Text(
                text = "$mastery%",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = masteryColor
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        TopicBar(percentage = mastery)
    }
}
