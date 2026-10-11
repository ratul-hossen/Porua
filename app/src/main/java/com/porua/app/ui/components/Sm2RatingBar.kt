package com.porua.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.porua.app.domain.sm2.Sm2Rating
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.ShapeMd

@Composable
fun Sm2RatingBar(
    onRatingSelected: (Sm2Rating) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalPoruaColors.current

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "How well did you know this?",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = colors.text
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (rating in Sm2Rating.values()) {
                val (bgColor, textColor, borderColor) = when (rating) {
                    Sm2Rating.AGAIN -> Triple(colors.dangerSoft, colors.danger, colors.danger.copy(alpha = 0.3f))
                    Sm2Rating.HARD -> Triple(colors.warningSoft, colors.warning, colors.warning.copy(alpha = 0.3f))
                    Sm2Rating.GOOD -> Triple(colors.primarySoft, colors.primary, colors.primary.copy(alpha = 0.3f))
                    Sm2Rating.EASY -> Triple(colors.successSoft, colors.success, colors.success.copy(alpha = 0.3f))
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(ShapeMd)
                        .background(bgColor)
                        .border(1.dp, borderColor, ShapeMd)
                        .clickable { onRatingSelected(rating) }
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = rating.label,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = textColor
                    )
                    Text(
                        text = rating.intervalDescription,
                        fontSize = 11.sp,
                        color = textColor.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "SM-2 uses your rating to schedule the next review.",
            fontSize = 11.sp,
            color = colors.textMuted,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
