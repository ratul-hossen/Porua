package com.porua.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.getMasteryColor

/**
 * TopicBar - Linear progress bar with color-coded mastery.
 * Red (<50%), Amber (50-74%), Green (>=75%).
 */
@Composable
fun TopicBar(
    percentage: Int,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
    customColor: Color? = null
) {
    val colors = LocalPoruaColors.current
    val progressColor = customColor ?: getMasteryColor(percentage)
    val progressFraction = (percentage.coerceIn(0, 100) / 100f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(colors.surface2)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = progressFraction)
                .height(height)
                .clip(RoundedCornerShape(height / 2))
                .background(progressColor)
        )
    }
}
