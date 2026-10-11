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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.ShapeLg
import com.porua.app.ui.theme.ShapeSm

@Composable
fun AiInsightCard(
    text: String,
    modifier: Modifier = Modifier,
    tag: String = "On-device ML",
    onClickSeeWhy: () -> Unit = {}
) {
    val colors = LocalPoruaColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(ShapeLg)
            .background(colors.surface)
            .border(1.dp, colors.border, ShapeLg)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Red icon container
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colors.primary)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = colors.onPrimary,
                    modifier = Modifier.size(12.dp)
                )
            }

            Text(
                text = "PORUA AI INSIGHT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text,
                letterSpacing = 0.5.sp
            )

            // On-device ML pill
            Text(
                text = tag,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textMuted,
                modifier = Modifier
                    .clip(ShapeSm)
                    .background(colors.surface2)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = text,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.Normal,
            color = colors.text
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.clickable { onClickSeeWhy() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "See why",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
