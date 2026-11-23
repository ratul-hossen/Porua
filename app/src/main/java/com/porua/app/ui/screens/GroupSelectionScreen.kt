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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.porua.app.data.content.GroupInfo
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.ShapeLg
import com.porua.app.ui.theme.ShapeSm

@Composable
fun GroupSelectionScreen(
    groups: List<GroupInfo>,
    onGroupSelected: (String) -> Unit
) {
    val colors = LocalPoruaColors.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Header icon
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(colors.primarySoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Select Your Group",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colors.text
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Subjects and questions will be loaded based on your HSC stream.",
            fontSize = 14.sp,
            color = colors.textMuted,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        groups.forEach { group ->
            GroupCard(
                group = group,
                onClick = { if (group.isAvailable) onGroupSelected(group.name) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Common note
        Text(
            text = "English and ICT are included as common subjects across all groups.",
            fontSize = 12.sp,
            color = colors.textMuted
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun GroupCard(
    group: GroupInfo,
    onClick: () -> Unit
) {
    val colors = LocalPoruaColors.current

    val (icon, iconTint) = when (group.name) {
        "Science"  -> Icons.Default.Science    to colors.primary
        "Arts"     -> Icons.Default.MenuBook   to colors.warning
        "Commerce" -> Icons.Default.AccountBalance to colors.success
        else       -> Icons.Default.Functions  to colors.text
    }

    val alpha = if (group.isAvailable) 1f else 0.45f
    val borderColor = if (group.isAvailable) colors.primary.copy(alpha = 0.4f) else colors.border
    val bgColor = if (group.isAvailable) colors.primarySoft.copy(alpha = 0.25f) else colors.surface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .clip(ShapeLg)
            .background(bgColor)
            .border(
                width = if (group.isAvailable) 2.dp else 1.dp,
                color = borderColor,
                shape = ShapeLg
            )
            .clickable(enabled = group.isAvailable) { onClick() }
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon circle
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (group.isAvailable) colors.primarySoft else colors.surface2),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (group.isAvailable) iconTint else colors.textMuted,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = group.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (group.isAvailable) colors.text else colors.textMuted
                )
                if (!group.isAvailable) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(ShapeSm)
                            .background(colors.surface2)
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Soon",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = group.subjectPreview,
                fontSize = 12.sp,
                color = colors.textMuted,
                lineHeight = 16.sp
            )

            if (group.isAvailable && group.subjectCount > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${group.subjectCount} subjects available ✓",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primary
                )
            }
        }

        if (!group.isAvailable) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
