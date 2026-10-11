package com.porua.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.ShapeMd

/**
 * Offline status banner matching Figma Component Library.
 * "You're offline — practice, plan & ML insights still work."
 */
@Composable
fun OfflineBanner(
    modifier: Modifier = Modifier,
    text: String = "You're offline — practice, plan & ML insights still work."
) {
    val colors = LocalPoruaColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(ShapeMd)
            .background(colors.warningSoft)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.WifiOff,
            contentDescription = "Offline",
            tint = colors.warning,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = colors.warning,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 16.sp
        )
    }
}
