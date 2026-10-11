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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.porua.app.ui.components.PoruaFilterChip
import com.porua.app.ui.components.PoruaPrimaryButton
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.ShapeLg
import com.porua.app.ui.theme.ShapeMd

@Composable
fun ExamTrackScreen(
    onBack: () -> Unit,
    onContinue: (track: String, group: String) -> Unit
) {
    val colors = LocalPoruaColors.current
    var selectedTrack by remember { mutableStateOf("HSC") }
    var selectedGroup by remember { mutableStateOf("Science") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Top Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.surface2)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.text,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Step 1 of 4",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4-segment Progress Bar (Step 1 active)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (step in 1..4) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(ShapeMd)
                        .background(if (step <= 1) colors.primary else colors.border)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Which exam are you\npreparing for?",
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colors.text
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "We use this to load the right NCTB syllabus and question bank.",
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = colors.textMuted
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Exam Track Cards
        ExamTrackCard(
            title = "SSC",
            subtitle = "Class 9–10 • Board exam",
            icon = Icons.Default.MenuBook,
            isSelected = selectedTrack == "SSC",
            onClick = { selectedTrack = "SSC" }
        )

        Spacer(modifier = Modifier.height(12.dp))

        ExamTrackCard(
            title = "HSC",
            subtitle = "Class 11–12 • Board exam",
            icon = Icons.Default.School,
            isSelected = selectedTrack == "HSC",
            onClick = { selectedTrack = "HSC" }
        )

        Spacer(modifier = Modifier.height(12.dp))

        ExamTrackCard(
            title = "University admission",
            subtitle = "After HSC • MCQ-based",
            icon = Icons.Default.AccountBalance,
            isSelected = selectedTrack == "University admission",
            onClick = { selectedTrack = "University admission" }
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Group Selection
        Text(
            text = "Your group",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = colors.text
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Science", "Business Studies", "Humanities").forEach { grp ->
                PoruaFilterChip(
                    text = grp,
                    isSelected = selectedGroup == grp,
                    onClick = { selectedGroup = grp }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f, fill = false))
        Spacer(modifier = Modifier.height(36.dp))

        PoruaPrimaryButton(
            text = "Continue",
            onClick = { onContinue(selectedTrack, selectedGroup) }
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun ExamTrackCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalPoruaColors.current

    val borderColor = if (isSelected) colors.primary else colors.border
    val bgColor = if (isSelected) colors.primarySoft.copy(alpha = 0.4f) else colors.surface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeLg)
            .background(bgColor)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, ShapeLg)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (isSelected) colors.primary else colors.primarySoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else colors.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = colors.textMuted
            )
        }

        // Radio circle indicator
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (isSelected) colors.primary else Color.Transparent)
                .border(2.dp, if (isSelected) colors.primary else colors.border, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
