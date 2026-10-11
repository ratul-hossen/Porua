package com.porua.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.ShapeMd

@Composable
fun PoruaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 52.dp
) {
    val colors = LocalPoruaColors.current

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = ShapeMd,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.primary,
            contentColor = colors.onPrimary,
            disabledContainerColor = colors.surface2,
            disabledContentColor = colors.textMuted
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PoruaSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 52.dp
) {
    val colors = LocalPoruaColors.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(ShapeMd)
            .background(colors.surface)
            .border(1.dp, colors.border, ShapeMd)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.text
        )
    }
}

@Composable
fun PoruaAiButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPill: Boolean = false
) {
    val colors = LocalPoruaColors.current
    val gradient = Brush.horizontalGradient(
        colors = listOf(colors.heroStart, colors.primary)
    )
    val shape = if (isPill) CircleShape else ShapeMd

    Row(
        modifier = modifier
            .clip(shape)
            .background(gradient)
            .clickable { onClick() }
            .padding(horizontal = if (isPill) 16.dp else 20.dp, vertical = if (isPill) 10.dp else 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = Color.White,
            fontSize = if (isPill) 13.sp else 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
