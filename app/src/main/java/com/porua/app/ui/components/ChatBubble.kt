package com.porua.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ThumbUp
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
import com.porua.app.ui.theme.ShapeLg
import com.porua.app.ui.theme.ShapeMd
import com.porua.app.ui.theme.ShapeSm

@Composable
fun UserChatBubble(
    message: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalPoruaColors.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(ShapeLg)
                .background(colors.primary)
                .padding(14.dp)
        ) {
            Text(
                text = message,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun AiResponseCard(
    steps: List<String>,
    answerSummary: String,
    syllabusNote: String,
    onSaveCard: () -> Unit,
    modifier: Modifier = Modifier
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
        // AI Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Porua • step by step",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.primary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Steps
        steps.forEach { step ->
            Text(
                text = step,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = colors.text,
                modifier = Modifier.padding(vertical = 3.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Green Answer Badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(ShapeMd)
                .background(colors.successSoft)
                .padding(10.dp)
        ) {
            Text(
                text = "Answer: $answerSummary • $syllabusNote",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.success
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Save as review card
            Row(
                modifier = Modifier
                    .clip(ShapeSm)
                    .background(colors.primarySoft)
                    .clickable { onSaveCard() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Save as review card",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primary
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy",
                tint = colors.textMuted,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Default.ThumbUp,
                contentDescription = "Helpful",
                tint = colors.textMuted,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { }
            )
        }
    }
}
