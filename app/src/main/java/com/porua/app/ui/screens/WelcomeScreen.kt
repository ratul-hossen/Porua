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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.porua.app.R
import com.porua.app.ui.components.MasteryRing
import com.porua.app.ui.components.PoruaPrimaryButton
import com.porua.app.ui.components.PoruaSecondaryButton
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.ShapeLg
import com.porua.app.ui.theme.ShapeSm

@Composable
fun WelcomeScreen(
    onSignUpClick: () -> Unit,
    onLoginClick: () -> Unit
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
                // Top Tag: "AI Study Engine"
                Row(
                    modifier = Modifier
                        .clip(ShapeSm)
                        .background(colors.surface)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.welcome_engine_tag),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Floating Goal Card
                Box(modifier = Modifier.fillMaxWidth(0.92f)) {
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
                            percentage = 100,
                            size = 56.dp,
                            strokeWidth = 6.dp
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = stringResource(R.string.welcome_target_title),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.text
                            )
                            Text(
                                text = stringResource(R.string.welcome_target_desc),
                                fontSize = 12.sp,
                                color = colors.textMuted
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            // Goal pill
                            Text(
                                text = stringResource(R.string.welcome_sm2_pill),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.primary,
                                modifier = Modifier
                                    .clip(ShapeSm)
                                    .background(colors.primarySoft)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Floating sparkle circle
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
                        tint = colors.success,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.welcome_offline_tag),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // App Logo & Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.welcome_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.text
                )
                Text(
                    text = stringResource(R.string.welcome_tagline),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Welcome Description
        Text(
            text = stringResource(R.string.welcome_description),
            fontSize = 14.sp,
            color = colors.textMuted,
            lineHeight = 20.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 3 Feature Bullets
        FeatureRow(
            icon = Icons.Default.TrackChanges,
            title = stringResource(R.string.welcome_feature_1_title),
            description = stringResource(R.string.welcome_feature_1_desc),
            tint = colors.danger
        )

        Spacer(modifier = Modifier.height(14.dp))

        FeatureRow(
            icon = Icons.Default.CalendarToday,
            title = stringResource(R.string.welcome_feature_2_title),
            description = stringResource(R.string.welcome_feature_2_desc),
            tint = colors.primary
        )

        Spacer(modifier = Modifier.height(14.dp))

        FeatureRow(
            icon = Icons.Default.WifiOff,
            title = stringResource(R.string.welcome_feature_3_title),
            description = stringResource(R.string.welcome_feature_3_desc),
            tint = colors.warning
        )

        Spacer(modifier = Modifier.weight(1f, fill = false))
        Spacer(modifier = Modifier.height(28.dp))

        // Action Buttons
        PoruaPrimaryButton(
            text = stringResource(R.string.btn_create_account),
            onClick = onSignUpClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        PoruaSecondaryButton(
            text = stringResource(R.string.btn_login),
            onClick = onLoginClick
        )

        Spacer(modifier = Modifier.height(24.dp))
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
                fontSize = 14.sp,
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
