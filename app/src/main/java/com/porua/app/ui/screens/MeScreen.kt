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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.porua.app.ui.components.StatTile
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.ShapeFull
import com.porua.app.ui.theme.ShapeLg
import com.porua.app.ui.theme.ShapeMd
import com.porua.app.ui.viewmodel.PoruaUiState

@Composable
fun MeScreen(
    uiState: PoruaUiState,
    onNavigateBottom: (BottomNavItem) -> Unit,
    onToggleOfflineMode: () -> Unit,
    onToggleCloudAi: () -> Unit
) {
    val colors = LocalPoruaColors.current
    val scrollState = rememberScrollState()
    val profile = uiState.userProfile

    var isOffline by remember { mutableStateOf(profile.isOfflineMode) }
    var cloudAiEnabled by remember { mutableStateOf(profile.cloudAiEnabled) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Profile & Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.text
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Profile Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ShapeLg)
                    .background(colors.surface)
                    .border(1.dp, colors.border, ShapeLg)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User Avatar with Initial
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(colors.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile.name.take(1).uppercase(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${profile.examTrack} • ${profile.group} Group",
                        fontSize = 13.sp,
                        color = colors.textMuted
                    )
                    Text(
                        text = "Target: ${profile.examDate} (${profile.daysRemaining} days left)",
                        fontSize = 12.sp,
                        color = colors.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Stats Tiles Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatTile(
                    title = "Streak",
                    value = "${profile.streakDays} days",
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.LocalFireDepartment
                )
                StatTile(
                    title = "Readiness",
                    value = "${profile.examReadyPercentage}%",
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.School
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Settings Section
            Text(
                text = "Application Settings",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Offline First Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ShapeLg)
                    .background(colors.surface)
                    .border(1.dp, colors.border, ShapeLg)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Offline Mode (Local Room DB)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                    Text(
                        text = "Run practice & SM-2 algorithms completely without internet",
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                }
                Switch(
                    checked = isOffline,
                    onCheckedChange = {
                        isOffline = it
                        onToggleOfflineMode()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = colors.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cloud AI Consent Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ShapeLg)
                    .background(colors.surface)
                    .border(1.dp, colors.border, ShapeLg)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Google Gemini Cloud AI",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                    Text(
                        text = "Allow step-by-step reasoning via cloud model (PII removed)",
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                }
                Switch(
                    checked = cloudAiEnabled,
                    onCheckedChange = {
                        cloudAiEnabled = it
                        onToggleCloudAi()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = colors.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Data Info
            Text(
                text = "Offline Database Info",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ShapeLg)
                    .background(colors.surface)
                    .border(1.dp, colors.border, ShapeLg)
                    .padding(16.dp)
            ) {
                Text(text = "Database Version: 1.0 (NCTB 2026/2027 Syllabus)", fontSize = 12.sp, color = colors.text)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Total Questions: 1,242 questions cached on device", fontSize = 12.sp, color = colors.textMuted)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "SM-2 Interval Cards: 18 due today", fontSize = 12.sp, color = colors.textMuted)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "App Build: v1.0 • Group 8 • INTI", fontSize = 11.sp, color = colors.primary)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        PoruaBottomNav(
            selectedItem = BottomNavItem.ME,
            onItemSelected = onNavigateBottom
        )
    }
}
