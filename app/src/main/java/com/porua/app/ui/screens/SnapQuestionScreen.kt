package com.porua.app.ui.screens

import androidx.compose.animation.Crossfade
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.porua.app.data.local.entity.QuestionEntity
import com.porua.app.ui.components.PoruaAiButton
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.ShapeFull
import com.porua.app.ui.theme.ShapeLg
import com.porua.app.ui.theme.ShapeMd
import com.porua.app.ui.theme.ShapeSm
import com.porua.app.ui.viewmodel.PoruaUiState

@Composable
fun SnapQuestionScreen(
    uiState: PoruaUiState,
    onClose: () -> Unit,
    onSolveWithAi: (String) -> Unit,
    onSelectOfflineMatch: (QuestionEntity) -> Unit
) {
    var hasSnapped by remember { mutableStateOf(false) }

    Crossfade(targetState = hasSnapped, label = "camera_crossfade") { snapped ->
        if (!snapped) {
            CameraViewFinderScreen(
                onClose = onClose,
                onShutterClick = { hasSnapped = true }
            )
        } else {
            SnapResultScreen(
                uiState = uiState,
                onBack = { hasSnapped = false },
                onSolveWithAi = onSolveWithAi,
                onSelectOfflineMatch = onSelectOfflineMatch
            )
        }
    }
}

@Composable
private fun CameraViewFinderScreen(
    onClose: () -> Unit,
    onShutterClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top controls: Close, Title, Flash
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF262626))
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = "Snap a question",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF262626))
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Flash",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Center Viewfinder with simulated textbook question
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(ShapeLg)
                    .background(Color(0xFF1E1E1E))
                    .border(2.dp, Color(0xFFE11D2E), ShapeLg)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(ShapeMd)
                            .background(Color(0xFFFDECEE))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "15. Calculate the standard emf of the cell Zn | Zn²⁺(1 M) || Cu²⁺(1 M) | Cu\ngiven E°(Zn²⁺/Zn) = -0.76 V and E°(Cu²⁺/Cu) = +0.34 V.\n(a) 0.42 V (b) 1.10 V (c) -1.10 V (d) 0.76 V",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = Color(0xFF0A0A0A)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Text detected pill
                    Row(
                        modifier = Modifier
                            .clip(ShapeFull)
                            .background(Color.Black.copy(alpha = 0.8f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrackChanges,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Text detected • ML Kit on-device",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Fit one question inside the frame",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }

        // Bottom Controls: Gallery, Shutter, Type it
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onShutterClick() }
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(ShapeMd)
                        .background(Color(0xFF262626)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Gallery",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Gallery", fontSize = 11.sp, color = Color.White)
            }

            // Big Red Shutter Button
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .border(4.dp, Color.White, CircleShape)
                    .padding(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE11D2E))
                    .clickable { onShutterClick() }
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onShutterClick() }
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(ShapeMd)
                        .background(Color(0xFF262626)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TextFields,
                        contentDescription = "Type",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Type it", fontSize = 11.sp, color = Color.White)
            }
        }
    }
}

@Composable
private fun SnapResultScreen(
    uiState: PoruaUiState,
    onBack: () -> Unit,
    onSolveWithAi: (String) -> Unit,
    onSelectOfflineMatch: (QuestionEntity) -> Unit
) {
    val colors = LocalPoruaColors.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
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

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = "Snapped question",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Scanned on this phone card
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(ShapeMd)
                        .background(Color(0xFF1E1E1E)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .border(2.dp, Color.White, ShapeSm)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Scanned on this phone",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(ShapeSm)
                                .background(colors.surface2)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Chemistry • Ch 4",
                                fontSize = 11.sp,
                                color = colors.textMuted
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(ShapeSm)
                                .background(colors.successSoft)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "97% confident",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.success
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recognised text box (Red outline with pencil icon)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ShapeLg)
                    .background(colors.surface)
                    .border(1.5.dp, colors.primary, ShapeLg)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recognised text",
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit text",
                        tint = colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = uiState.snappedQuestionText,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.text
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Solve with AI button
            PoruaAiButton(
                text = "Solve with AI",
                onClick = { onSolveWithAi(uiState.snappedQuestionText) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Needs internet • personal info is removed first",
                fontSize = 11.sp,
                color = colors.textMuted,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Similar in your offline bank
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Similar in your offline bank",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )

                Box(
                    modifier = Modifier
                        .clip(ShapeSm)
                        .background(colors.surface2)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "3 matches",
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3 Offline Matches
            uiState.similarOfflineQuestions.forEach { matchQ ->
                OfflineMatchRow(
                    question = matchQ,
                    onClick = { onSelectOfflineMatch(matchQ) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OfflineMatchRow(
    question: QuestionEntity,
    onClick: () -> Unit
) {
    val colors = LocalPoruaColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeLg)
            .background(colors.surface)
            .border(1.dp, colors.border, ShapeLg)
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.dangerSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.TrackChanges,
                contentDescription = null,
                tint = colors.danger,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = question.topicName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text
            )
            Text(
                text = "Chemistry • ${question.difficulty} • works offline",
                fontSize = 11.sp,
                color = colors.textMuted
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}
