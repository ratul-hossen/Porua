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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.porua.app.domain.countdown.ExamDateProvider
import com.porua.app.ui.components.PoruaFilterChip
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.ShapeMd

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GetStartedScreen(
    onBack: () -> Unit,
    onContinue: (
        name: String,
        track: String,
        group: String,
        year: String,
        goal: String,
        selectedSubjectIds: List<String>
    ) -> Unit
) {
    val colors = LocalPoruaColors.current
    val scrollState = rememberScrollState()

    var studentName by remember { mutableStateOf("") }
    val examTrack = "HSC"
    var selectedGroup by remember { mutableStateOf("Science") }
    var selectedYear by remember { mutableStateOf("2027") }
    var selectedGoal by remember { mutableStateOf("1 hour") }
    var nameError by remember { mutableStateOf<String?>(null) }

    val availableYears = remember {
        ExamDateProvider.getAvailableExamYears().map { it.toString() }
    }

    var selectedSubjectIds by remember {
        mutableStateOf(getSubjectsForStream("Science").map { it.id }.toSet())
    }

    // Update subjects when group / stream is switched
    LaunchedEffect(selectedGroup) {
        selectedSubjectIds = getSubjectsForStream(selectedGroup).map { it.id }.toSet()
    }

    fun handleSave() {
        val trimmed = studentName.trim()
        if (trimmed.isBlank()) {
            nameError = "Please enter your name"
        } else {
            nameError = null
            onContinue(
                trimmed,
                examTrack,
                selectedGroup,
                selectedYear,
                selectedGoal,
                selectedSubjectIds.toList()
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F11))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        // Modal Card matching the user's screenshot
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                .verticalScroll(scrollState)
                .padding(22.dp)
        ) {
            // Title
            Text(
                text = "Student Profile",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 1. Student Name
            Text(
                text = "Student Name",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = studentName,
                onValueChange = {
                    studentName = it
                    if (nameError != null) nameError = null
                },
                placeholder = { Text("e.g. Arif Rahman", color = colors.textMuted) },
                isError = nameError != null,
                supportingText = nameError?.let {
                    {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = colors.danger,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(it, color = colors.danger, fontSize = 11.sp)
                        }
                    }
                },
                singleLine = true,
                shape = ShapeMd,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.border,
                    focusedContainerColor = colors.surface2,
                    unfocusedContainerColor = colors.surface2,
                    errorContainerColor = colors.surface2,
                    focusedTextColor = colors.text,
                    unfocusedTextColor = colors.text
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Exam Track
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
                PoruaFilterChip(
                    text = examTrack,
                    isSelected = true,
                    onClick = { /* Fixed for HSC */ }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Group / Stream
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
                listOf("Science", "Arts", "Commerce").forEach { group ->
                    PoruaFilterChip(
                        text = group,
                        isSelected = selectedGroup == group,
                        onClick = { selectedGroup = group }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Target Exam Year
            Text(
                text = "Target Exam Year",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableYears.forEach { year ->
                    PoruaFilterChip(
                        text = year,
                        isSelected = selectedYear == year,
                        onClick = { selectedYear = year }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Daily Study Goal
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
                        isSelected = selectedGoal == goal,
                        onClick = { selectedGoal = goal }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 6. Selected Subjects
            Text(
                text = "Selected Subjects (${selectedSubjectIds.size} active)",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text
            )
            Spacer(modifier = Modifier.height(6.dp))
            val currentSubjects = remember(selectedGroup) { getSubjectsForStream(selectedGroup) }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                currentSubjects.forEach { subject ->
                    val isSelected = selectedSubjectIds.contains(subject.id)
                    PoruaFilterChip(
                        text = subject.name,
                        isSelected = isSelected,
                        onClick = {
                            selectedSubjectIds = if (isSelected) {
                                selectedSubjectIds - subject.id
                            } else {
                                selectedSubjectIds + subject.id
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 7. Action Buttons (Cancel & Save)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text(
                        text = "Cancel",
                        color = colors.textMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                TextButton(onClick = { handleSave() }) {
                    Text(
                        text = "Save",
                        color = colors.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private data class SubjectItem(val id: String, val name: String)

private fun getSubjectsForStream(group: String): List<SubjectItem> {
    val common = listOf(
        SubjectItem("ict", "ICT"),
        SubjectItem("english", "English")
    )
    val stream = when (group.lowercase().trim()) {
        "commerce" -> listOf(
            SubjectItem("accounting", "Accounting"),
            SubjectItem("business_organization", "Business Org"),
            SubjectItem("finance_banking", "Finance")
        )
        "arts" -> listOf(
            SubjectItem("economics", "Economics"),
            SubjectItem("civics", "Civics"),
            SubjectItem("history", "History")
        )
        else -> listOf(
            SubjectItem("physics", "Physics"),
            SubjectItem("chemistry", "Chemistry"),
            SubjectItem("biology", "Biology"),
            SubjectItem("higher_mathematics", "Higher Math")
        )
    }
    return common + stream
}
