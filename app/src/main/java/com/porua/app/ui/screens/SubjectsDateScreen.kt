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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import com.porua.app.ui.theme.ShapeFull
import com.porua.app.ui.theme.ShapeLg
import com.porua.app.ui.theme.ShapeMd
import com.porua.app.ui.theme.ShapeSm

data class SubjectOption(
    val id: String,
    val name: String,
    val chaptersCount: Int,
    val icon: ImageVector
)

@Composable
fun SubjectsDateScreen(
    onBack: () -> Unit,
    onContinue: (selectedSubjects: List<String>, examDate: String, studyTime: String) -> Unit
) {
    val colors = LocalPoruaColors.current
    val scrollState = rememberScrollState()

    val subjectList = remember {
        listOf(
            SubjectOption("phy", "Physics", 10, Icons.Default.Whatshot),
            SubjectOption("chem", "Chemistry", 12, Icons.Default.Science),
            SubjectOption("math", "Higher Math", 10, Icons.Default.Functions),
            SubjectOption("eng", "English", 8, Icons.Default.TextFields),
            SubjectOption("bio", "Biology", 12, Icons.Default.Eco),
            SubjectOption("ict", "ICT", 6, Icons.Default.Computer)
        )
    }

    val selectedSubjects = remember {
        mutableStateMapOf(
            "phy" to true,
            "chem" to true,
            "math" to true,
            "eng" to true,
            "bio" to false,
            "ict" to false
        )
    }

    var selectedDailyTime by remember { mutableStateOf("1 hour") }

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
                text = "Step 2 of 4",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress Bar (Step 2 active)
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
                        .background(if (step <= 2) colors.primary else colors.border)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Pick your subjects",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colors.text
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 2-column Subject Grid
        for (i in subjectList.indices step 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val sub1 = subjectList[i]
                SubjectGridCard(
                    subject = sub1,
                    isSelected = selectedSubjects[sub1.id] == true,
                    onToggle = { selectedSubjects[sub1.id] = !(selectedSubjects[sub1.id] ?: false) },
                    modifier = Modifier.weight(1f)
                )

                if (i + 1 < subjectList.size) {
                    val sub2 = subjectList[i + 1]
                    SubjectGridCard(
                        subject = sub2,
                        isSelected = selectedSubjects[sub2.id] == true,
                        onToggle = { selectedSubjects[sub2.id] = !(selectedSubjects[sub2.id] ?: false) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Exam Date Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(ShapeLg)
                .background(colors.surface)
                .border(1.dp, colors.border, ShapeLg)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(ShapeSm)
                    .background(colors.primarySoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Exam date",
                    fontSize = 11.sp,
                    color = colors.textMuted
                )
                Text(
                    text = "HSC • 12 Apr 2027",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )
            }

            // Badge "186 days"
            Box(
                modifier = Modifier
                    .clip(ShapeFull)
                    .background(colors.surface2)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "186 days",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Daily Study Time
        Text(
            text = "Daily study time",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = colors.text
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("30 min", "1 hour", "2 hours", "3+ hours").forEach { duration ->
                PoruaFilterChip(
                    text = duration,
                    isSelected = selectedDailyTime == duration,
                    onClick = { selectedDailyTime = duration }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f, fill = false))
        Spacer(modifier = Modifier.height(36.dp))

        PoruaPrimaryButton(
            text = "Continue",
            onClick = {
                val chosen = selectedSubjects.filter { it.value }.keys.toList()
                onContinue(chosen, "12 Apr 2027", selectedDailyTime)
            }
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun SubjectGridCard(
    subject: SubjectOption,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalPoruaColors.current
    val borderColor = if (isSelected) colors.primary else colors.border
    val bgColor = if (isSelected) colors.primarySoft.copy(alpha = 0.35f) else colors.surface

    Column(
        modifier = modifier
            .clip(ShapeLg)
            .background(bgColor)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, ShapeLg)
            .clickable { onToggle() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.primarySoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = subject.icon,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Checkbox
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(ShapeSm)
                    .background(if (isSelected) colors.primary else Color.Transparent)
                    .border(2.dp, if (isSelected) colors.primary else colors.border, ShapeSm),
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

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = subject.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = colors.text
        )

        Text(
            text = "${subject.chaptersCount} chapters",
            fontSize = 12.sp,
            color = colors.textMuted
        )
    }
}
