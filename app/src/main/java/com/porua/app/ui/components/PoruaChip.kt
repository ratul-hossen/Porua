package com.porua.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.ShapeFull

@Composable
fun PoruaFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color? = null
) {
    val colors = LocalPoruaColors.current
    val effectiveActive = activeColor ?: colors.primary

    val bgColor = if (isSelected) effectiveActive else colors.surface2
    val textColor = if (isSelected) colors.onPrimary else colors.text
    val borderColor = if (isSelected) effectiveActive else colors.border

    Box(
        modifier = modifier
            .clip(ShapeFull)
            .background(bgColor)
            .border(1.dp, borderColor, ShapeFull)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
fun PoruaWeakBadge(
    text: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalPoruaColors.current

    Box(
        modifier = modifier
            .clip(ShapeFull)
            .background(colors.dangerSoft)
            .border(1.dp, colors.danger.copy(alpha = 0.3f), ShapeFull)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = colors.danger,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
