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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.porua.app.ui.components.AiResponseCard
import com.porua.app.ui.components.PoruaFilterChip
import com.porua.app.ui.components.PoruaPrimaryButton
import com.porua.app.ui.components.PoruaSecondaryButton
import com.porua.app.ui.components.UserChatBubble
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.ShapeFull
import com.porua.app.ui.theme.ShapeLg
import com.porua.app.ui.theme.ShapeMd
import com.porua.app.ui.theme.ShapeSm
import com.porua.app.ui.viewmodel.ChatSender
import com.porua.app.ui.viewmodel.PoruaUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskPoruaScreen(
    uiState: PoruaUiState,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    onOpenCameraSnap: () -> Unit,
    onSaveReviewCard: () -> Unit,
    onToggleConsentDialog: (Boolean) -> Unit
) {
    val colors = LocalPoruaColors.current
    var inputText by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
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

            Spacer(modifier = Modifier.width(12.dp))

            // AI Avatar
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ask Porua",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(colors.success)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Gemini • online",
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onToggleConsentDialog(true) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = colors.text,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Context Badge: "Context: Electrochemistry • Q4 you just missed"
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .fillMaxWidth()
                .clip(ShapeSm)
                .background(colors.surface2)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.TrackChanges,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Context: Electrochemistry • Q4 you just missed",
                fontSize = 12.sp,
                color = colors.textMuted,
                fontWeight = FontWeight.Medium
            )
        }

        // Chat Messages List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(6.dp)) }

            items(uiState.chatMessages) { msg ->
                if (msg.sender == ChatSender.USER) {
                    UserChatBubble(message = msg.text)
                } else {
                    AiResponseCard(
                        steps = msg.text.split("\n").filter { it.isNotBlank() },
                        answerSummary = msg.answerSummary.ifEmpty { "1.10 V" },
                        syllabusNote = msg.syllabusNote.ifEmpty { "NCTB HSC Chemistry 2nd paper, Ch 4" },
                        onSaveCard = onSaveReviewCard
                    )
                }
            }

            if (uiState.isAiTyping) {
                item {
                    Row(
                        modifier = Modifier
                            .clip(ShapeFull)
                            .background(colors.surface2)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Porua is typing •••",
                            fontSize = 12.sp,
                            color = colors.textMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        // Quick Suggestion Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val suggestions = listOf("Explain simpler", "Give a similar MCQ", "Show formula sheet")
            items(suggestions) { suggestion ->
                PoruaFilterChip(
                    text = suggestion,
                    isSelected = false,
                    onClick = { onSendMessage(suggestion) }
                )
            }
        }

        // Privacy indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Name & phone are removed before sending to Gemini",
                fontSize = 11.sp,
                color = colors.textMuted
            )
        }

        // Bottom Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Camera Snap button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.surface2)
                    .clickable { onOpenCameraSnap() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Snap question",
                    tint = colors.text,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Text input box
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        text = "Ask about your syllabus...",
                        fontSize = 13.sp,
                        color = colors.textMuted
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colors.surface2,
                    unfocusedContainerColor = colors.surface2,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = ShapeFull,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Mic button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.surface2)
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice",
                    tint = colors.text,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Send button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.primary)
                    .clickable {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    // Cloud AI Consent Bottom Sheet (Screen 18c)
    if (uiState.showCloudConsent) {
        ModalBottomSheet(
            onDismissRequest = { onToggleConsentDialog(false) },
            sheetState = sheetState,
            containerColor = colors.surface
        ) {
            CloudConsentContent(
                onAllow = { onToggleConsentDialog(false) },
                onNotNow = { onToggleConsentDialog(false) }
            )
        }
    }
}

@Composable
private fun CloudConsentContent(
    onAllow: () -> Unit,
    onNotNow: () -> Unit
) {
    val colors = LocalPoruaColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Red Shield Icon in circle
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(colors.dangerSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = colors.danger,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Send this question to Google Gemini?",
            fontSize = 19.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colors.text
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Cloud AI gives longer, step-by-step answers. Porua checks your text first so only study content leaves your phone.",
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = colors.primary,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 4 Checklist Items with Green Checks
        ConsentCheckItem(text = "Only the question text is sent")
        ConsentCheckItem(text = "Name, phone & email are removed automatically")
        ConsentCheckItem(text = "Off-topic or unsafe replies are filtered")
        ConsentCheckItem(text = "You can turn cloud AI off any time in Settings")

        Spacer(modifier = Modifier.height(28.dp))

        PoruaPrimaryButton(
            text = "Allow & ask",
            onClick = onAllow
        )

        Spacer(modifier = Modifier.height(12.dp))

        PoruaSecondaryButton(
            text = "Not now — use offline help",
            onClick = onNotNow
        )
    }
}

@Composable
private fun ConsentCheckItem(text: String) {
    val colors = LocalPoruaColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(colors.successSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = colors.success,
                modifier = Modifier.size(13.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = colors.text,
            fontWeight = FontWeight.Medium
        )
    }
}
