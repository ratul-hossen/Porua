package com.porua.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.porua.app.ui.components.MasteryRing
import com.porua.app.ui.components.PoruaPrimaryButton
import com.porua.app.ui.components.PoruaSecondaryButton
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.ShapeLg
import com.porua.app.ui.theme.ShapeMd
import com.porua.app.ui.theme.ShapeSm

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onRestoreBackup: () -> Unit
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
        Spacer(modifier = Modifier.height(16.dp))

        // Hero Preview Card Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(ShapeLg)
                .background(colors.primarySoft.copy(alpha = 0.5f))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Tag: "Today • 45 min plan"
                Row(
                    modifier = Modifier
                        .clip(ShapeSm)
                        .background(colors.surface)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Today • 45 min plan",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Floating Mock Card
                Box(modifier = Modifier.fillMaxWidth(0.9f)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(ShapeLg)
                            .background(colors.surface)
                            .border(1.dp, colors.border, ShapeLg)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MasteryRing(
                            percentage = 64,
                            size = 56.dp,
                            strokeWidth = 6.dp
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Chemistry",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.text
                            )
                            Text(
                                text = "Mastery",
                                fontSize = 12.sp,
                                color = colors.textMuted
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            // Weak pill
                            Text(
                                text = "Weak: Alcohols",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.danger,
                                modifier = Modifier
                                    .clip(ShapeSm)
                                    .background(colors.dangerSoft)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Floating red sparkle circle
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .align(Alignment.TopEnd)
                            .clip(CircleShape)
                            .background(colors.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Tag: "Works offline"
                Row(
                    modifier = Modifier
                        .clip(ShapeSm)
                        .background(colors.surface)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = colors.warning,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Works offline",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Title
        Text(
            text = "Study smarter for SSC & HSC",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colors.text,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 3 Feature Bullets
        FeatureRow(
            icon = Icons.Default.TrackChanges,
            title = "Find your weak topics",
            description = "Mastery map from your own answers",
            tint = colors.danger
        )

        Spacer(modifier = Modifier.height(16.dp))

        FeatureRow(
            icon = Icons.Default.CalendarToday,
            title = "Get a day-by-day plan",
            description = "Built around your exam date",
            tint = colors.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        FeatureRow(
            icon = Icons.Default.WifiOff,
            title = "Practise anywhere",
            description = "Questions & ML work without internet",
            tint = colors.warning
        )

        Spacer(modifier = Modifier.weight(1f, fill = false))
        Spacer(modifier = Modifier.height(32.dp))

        // Action Buttons
        PoruaPrimaryButton(
            text = "Get started",
            onClick = onGetStarted
        )

        Spacer(modifier = Modifier.height(12.dp))

        PoruaSecondaryButton(
            text = "Restore from backup",
            onClick = onRestoreBackup
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    title: String,
    description: String,
    tint: Color
) {
    val colors = LocalPoruaColors.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(colors.surface2),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = colors.textMuted
            )
        }
    }
}
