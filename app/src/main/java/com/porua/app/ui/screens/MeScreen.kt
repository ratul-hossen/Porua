package com.porua.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.porua.app.PoruaApplication
import com.porua.app.data.ai.AiHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.porua.app.domain.countdown.ExamDateProvider
import com.porua.app.ui.components.BottomNavItem
import com.porua.app.ui.components.PoruaBottomNav
import com.porua.app.ui.components.PoruaFilterChip
import com.porua.app.ui.components.PoruaPrimaryButton
import com.porua.app.ui.components.StatTile
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.ShapeFull
import com.porua.app.ui.theme.ShapeLg
import com.porua.app.ui.theme.ShapeMd
import com.porua.app.ui.theme.ShapeSm
import com.porua.app.ui.viewmodel.PoruaUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MeScreen(
    uiState: PoruaUiState,
    onNavigateBottom: (BottomNavItem) -> Unit,
    onToggleOfflineMode: () -> Unit,
    onToggleCloudAi: () -> Unit,
    onToggleSubject: (String) -> Unit = {},
    onResetOnboarding: () -> Unit = {},
    onLogout: () -> Unit = {},
    onUpdateProfile: (name: String, track: String, group: String, year: String, date: String, goal: String, selectedSubjectIds: List<String>) -> Unit = { _, _, _, _, _, _, _ -> }
) {
    val colors = LocalPoruaColors.current
    val scrollState = rememberScrollState()
    val profile = uiState.userProfile

    var isOffline by remember(profile.isOfflineMode) { mutableStateOf(profile.isOfflineMode) }
    var cloudAiEnabled by remember(profile.cloudAiEnabled) { mutableStateOf(profile.cloudAiEnabled) }

    // Edit Profile Dialog state
    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(profile.name) }
    var editTrack by remember { mutableStateOf(profile.examTrack) }
    var editGroup by remember { mutableStateOf(profile.group) }
    var editYear by remember { mutableStateOf(profile.examYear) }
    var editDate by remember { mutableStateOf(profile.examDate) }
    var editDailyGoal by remember { mutableStateOf(profile.dailyStudyTime) }
    var editSelectedSubjectIds by remember(uiState.subjects) {
        mutableStateOf(uiState.subjects.filter { it.isSelected }.map { it.id }.toSet())
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val app = context.applicationContext as? PoruaApplication
    val authDataStore = app?.authDataStore

    var geminiInput by remember { mutableStateOf("") }
    var grokInput by remember { mutableStateOf("") }
    var showKeys by remember { mutableStateOf(false) }
    var keysStatusMessage by remember { mutableStateOf<String?>(null) }
    var isCheckingKeys by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authDataStore?.let { store ->
            val gKey = store.geminiApiKey.first() ?: ""
            val grKey = store.grokApiKey.first() ?: ""
            geminiInput = gKey
            grokInput = grKey
            AiHelper.userGoogleApiKey = gKey
            AiHelper.userGrokApiKey = grKey
        }
    }

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profile & Settings",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.text
                )

                // Edit Profile Icon Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.surface2)
                        .clickable {
                            editName = profile.name
                            editTrack = profile.examTrack
                            editGroup = profile.group
                            editYear = profile.examYear
                            editDate = profile.examDate
                            editDailyGoal = profile.dailyStudyTime
                            showEditDialog = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Profile Card (Clickable to edit)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ShapeLg)
                    .background(colors.surface)
                    .border(1.dp, colors.border, ShapeLg)
                    .clickable {
                        editName = profile.name
                        editTrack = profile.examTrack
                        editGroup = profile.group
                        editYear = profile.examYear
                        editDate = profile.examDate
                        editDailyGoal = profile.dailyStudyTime
                        showEditDialog = true
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User Avatar with Initial
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(colors.primary),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = if (profile.name.isNotBlank()) profile.name.take(1).uppercase() else "P"
                    Text(
                        text = initial,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.name.ifBlank { "Tap to add your name" },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (profile.name.isNotBlank()) colors.text else colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${profile.examTrack} ${profile.examYear} • ${profile.group} Group",
                        fontSize = 13.sp,
                        color = colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Target: ${profile.examDate} • ${profile.daysRemaining} days left",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.primary
                    )
                }

                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = colors.textMuted,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                StatTile(
                    title = "Daily Goal",
                    value = profile.dailyStudyTime.ifBlank { "1 hour" },
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.AccessTime
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Active Subjects Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Subjects (${uiState.subjects.count { it.isSelected }} active)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )
                Text(
                    text = "Tap switch to select",
                    fontSize = 11.sp,
                    color = colors.textMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ShapeLg)
                    .background(colors.surface)
                    .border(1.dp, colors.border, ShapeLg)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.subjects.forEach { subject ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(ShapeMd)
                            .background(if (subject.isSelected) colors.primarySoft.copy(alpha = 0.45f) else colors.surface2)
                            .clickable { onToggleSubject(subject.id) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = subject.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.text
                            )
                            Text(
                                text = "${subject.paper} • ${subject.chaptersCount} chapters",
                                fontSize = 11.sp,
                                color = colors.textMuted
                            )
                        }
                        Switch(
                            checked = subject.isSelected,
                            onCheckedChange = { onToggleSubject(subject.id) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = colors.primary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Re-run Onboarding & Syllabus Wizard Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ShapeLg)
                    .background(colors.surface)
                    .border(1.dp, colors.border, ShapeLg)
                    .clickable { onResetOnboarding() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(colors.primarySoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Re-run Syllabus & Exam Setup",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.text
                        )
                        Text(
                            text = "Change Track, Group, Date or Retake Diagnostic",
                            fontSize = 11.sp,
                            color = colors.textMuted
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = colors.textMuted
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
                        text = "Practice questions and SM-2 spaced repetition work without internet",
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
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

            // Cloud AI Toggle
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
                        text = "Cloud AI Model Assistance",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                    Text(
                        text = "Enable Google Gemini & Grok for step-by-step problem explanations",
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
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

            Spacer(modifier = Modifier.height(14.dp))

            // ── AI Configuration (BYOK - Bring Your Own Key) ────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ShapeLg)
                    .background(colors.surface)
                    .border(1.dp, colors.border, ShapeLg)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI API Keys (Gemini & Grok)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Provide your own API keys to run Ask Porua AI tutor. Keys are stored locally on-device and never leaked.",
                    fontSize = 12.sp,
                    color = colors.textMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Google Gemini API Key",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = geminiInput,
                    onValueChange = { geminiInput = it },
                    placeholder = { Text("AIzaSy...", fontSize = 12.sp, color = colors.textMuted) },
                    singleLine = true,
                    visualTransformation = if (showKeys) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Groq / OpenAI API Key (OpenAI models on Groq)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = grokInput,
                    onValueChange = { grokInput = it },
                    placeholder = { Text("gsk_... or sk-...", fontSize = 12.sp, color = colors.textMuted) },
                    singleLine = true,
                    visualTransformation = if (showKeys) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text
                    )
                )

                if (!keysStatusMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = keysStatusMessage ?: "",
                        fontSize = 12.sp,
                        color = if (keysStatusMessage?.startsWith("✓") == true) colors.success else colors.danger
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { showKeys = !showKeys }) {
                        Text(
                            text = if (showKeys) "Hide Keys" else "Show Keys",
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }

                    Button(
                        onClick = {
                            isCheckingKeys = true
                            keysStatusMessage = null
                            coroutineScope.launch {
                                val cleanG = geminiInput.trim().trim('"', '\'', '`')
                                val cleanGr = grokInput.trim().trim('"', '\'', '`')
                                authDataStore?.saveAiKeys(cleanG, cleanGr)
                                AiHelper.userGoogleApiKey = cleanG
                                AiHelper.userGrokApiKey = cleanGr

                                val check = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                    AiHelper.autoCheckActiveConnection()
                                }
                                isCheckingKeys = false
                                if (check.isValid) {
                                    keysStatusMessage = "✓ Verified & Active: ${check.message}"
                                } else {
                                    keysStatusMessage = check.message
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Text(
                            text = if (isCheckingKeys) "Verifying..." else "Save & Verify",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Data Info
            Text(
                text = "Offline Question Bank",
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
                Text(text = "Database Version: 2.0 (Room Offline Database)", fontSize = 12.sp, color = colors.text)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Syllabus: NCTB National Curriculum", fontSize = 12.sp, color = colors.textMuted)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Total Questions: 1,242 questions cached on device", fontSize = 12.sp, color = colors.textMuted)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "SM-2 Interval Cards: 18 due today", fontSize = 12.sp, color = colors.textMuted)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "App: Porua • Group 8 • INTI", fontSize = 11.sp, color = colors.primary)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Logout Card Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ShapeLg)
                    .background(colors.surface)
                    .border(1.dp, colors.danger.copy(alpha = 0.35f), ShapeLg)
                    .clickable { onLogout() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    tint = colors.danger,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Log Out",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.danger
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        // Bottom Navigation Bar
        PoruaBottomNav(
            selectedItem = BottomNavItem.ME,
            onItemSelected = onNavigateBottom
        )
    }

    // Edit Profile Modal Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    text = "Student Profile",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Name Field
                    Text(
                        text = "Student Name",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        placeholder = { Text("e.g. Arif Rahman") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Exam Track
                    Text(
                        text = "Exam Track",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("HSC").forEach { track ->
                            PoruaFilterChip(
                                text = track,
                                isSelected = editTrack == track,
                                onClick = { editTrack = track }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Group
                    Text(
                        text = "Group / Stream",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Science", "Business Studies", "Humanities").forEach { group ->
                            PoruaFilterChip(
                                text = group,
                                isSelected = editGroup == group,
                                onClick = { editGroup = group }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Exam Year
                    Text(
                        text = "Target Exam Year",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val availableYears = remember { ExamDateProvider.getAvailableExamYears().map { it.toString() } }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableYears.forEach { year ->
                            PoruaFilterChip(
                                text = year,
                                isSelected = editYear == year,
                                onClick = { editYear = year }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Daily Study Goal
                    Text(
                        text = "Daily Study Goal",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("30 min", "1 hour", "2 hours", "3 hours").forEach { goal ->
                            PoruaFilterChip(
                                text = goal,
                                isSelected = editDailyGoal == goal,
                                onClick = { editDailyGoal = goal }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Subjects Selection
                    Text(
                        text = "Selected Subjects (${editSelectedSubjectIds.size} active)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        uiState.subjects.forEach { subject ->
                            val isSelected = editSelectedSubjectIds.contains(subject.id)
                            PoruaFilterChip(
                                text = subject.name,
                                isSelected = isSelected,
                                onClick = {
                                    editSelectedSubjectIds = if (isSelected) {
                                        editSelectedSubjectIds - subject.id
                                    } else {
                                        editSelectedSubjectIds + subject.id
                                    }
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val chosenYear = editYear.toIntOrNull() ?: ExamDateProvider.getAvailableExamYears().first()
                        onUpdateProfile(
                            editName,
                            editTrack,
                            editGroup,
                            editYear,
                            ExamDateProvider.formatExamDate(chosenYear),
                            editDailyGoal,
                            editSelectedSubjectIds.toList()
                        )
                        showEditDialog = false
                    }
                ) {
                    Text("Save", fontWeight = FontWeight.Bold, color = colors.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = colors.textMuted)
                }
            },
            containerColor = colors.surface
        )
    }
}
