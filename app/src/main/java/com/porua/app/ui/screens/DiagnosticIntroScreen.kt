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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.porua.app.ui.components.PoruaPrimaryButton
import com.porua.app.ui.components.PoruaSecondaryButton
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.ShapeLg
import com.porua.app.ui.theme.ShapeMd

@Composable
fun DiagnosticIntroScreen(
    onBack: () -> Unit,
    onStartDiagnostic: () -> Unit,
    onSkipForNow: () -> Unit
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
                text = "Step 4 of 4",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress Bar (all 4 steps completed)
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
                        .background(colors.primary)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Concentric Target Illustration Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(ShapeLg)
                .background(colors.surface2.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(colors.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.TrackChanges,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(42.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Let's find your starting point",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colors.text
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "20 quick questions across your 4 subjects. This only sets your first mastery map — no score, no pressure.",
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = colors.textMuted
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 3 Stat Cards in a row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DiagnosticStatCard(
                icon = Icons.Default.MenuBook,
                stat = "20",
                label = "questions",
                modifier = Modifier.weight(1f)
            )

            DiagnosticStatCard(
                icon = Icons.Default.AccessTime,
                stat = "~12",
                label = "minutes",
                modifier = Modifier.weight(1f)
            )

            DiagnosticStatCard(
                icon = Icons.Default.WifiOff,
                stat = "100%",
                label = "offline",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // On-device ML Info Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(ShapeLg)
                .background(colors.surface2.copy(alpha = 0.6f))
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Memory,
                contentDescription = null,
                tint = colors.text,
                modifier = Modifier
                    .size(22.dp)
                    .padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "On-device ML is learning",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your answers train a small model on this phone that predicts which topics you are likely to forget. Nothing leaves your device.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = colors.textMuted
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f, fill = false))
        Spacer(modifier = Modifier.height(32.dp))

        PoruaPrimaryButton(
            text = "Start diagnostic",
            onClick = onStartDiagnostic
        )

        Spacer(modifier = Modifier.height(12.dp))

        PoruaSecondaryButton(
            text = "Skip for now",
            onClick = onSkipForNow
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun DiagnosticStatCard(
    icon: ImageVector,
    stat: String,
    label: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalPoruaColors.current

    Column(
        modifier = modifier
            .clip(ShapeLg)
            .background(colors.surface)
            .border(1.dp, colors.border, ShapeLg)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stat,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colors.text
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = colors.textMuted
        )
    }
}
