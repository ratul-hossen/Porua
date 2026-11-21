package com.porua.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.porua.app.domain.countdown.ExamDateProvider
import com.porua.app.ui.theme.LocalPoruaColors

@Composable
fun ExamYearSetupDialog(
    initialYear: String? = null,
    onYearSelected: (String) -> Unit,
    onDismissRequest: (() -> Unit)? = null
) {
    val colors = LocalPoruaColors.current
    val availableYears = remember { ExamDateProvider.getAvailableExamYears() }
    var selectedYear by remember {
        mutableStateOf(
            initialYear?.toIntOrNull()?.takeIf { it in availableYears } ?: availableYears.first()
        )
    }

    AlertDialog(
        onDismissRequest = { onDismissRequest?.invoke() },
        title = {
            Text(
                text = "Which year's HSC exam are you preparing for?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text,
                lineHeight = 24.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Porua will set up your personalized study countdown to April 1.",
                    fontSize = 13.sp,
                    color = colors.textMuted,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Select HSC Exam Year:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    availableYears.forEach { year ->
                        PoruaFilterChip(
                            text = year.toString(),
                            isSelected = selectedYear == year,
                            onClick = { selectedYear = year }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onYearSelected(selectedYear.toString()) }
            ) {
                Text(
                    text = "Set Target Year",
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    fontSize = 14.sp
                )
            }
        },
        dismissButton = if (onDismissRequest != null) {
            {
                TextButton(onClick = { onDismissRequest() }) {
                    Text(text = "Cancel", color = colors.textMuted)
                }
            }
        } else null,
        containerColor = colors.surface
    )
}
