package com.porua.app.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.speech.RecognizerIntent
import android.util.Base64
import android.widget.Toast
import java.io.ByteArrayOutputStream
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
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
import androidx.compose.ui.platform.LocalContext
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
    onSendMessage: (String, String?, String) -> Unit,
    onOpenCameraSnap: () -> Unit,
    onSaveReviewCard: () -> Unit,
    onToggleConsentDialog: (Boolean) -> Unit,
    onNewChat: () -> Unit = {}
) {
    val colors = LocalPoruaColors.current
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showImageSourceSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val imageSourceSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Camera launcher (takes photo directly)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            selectedImageUri = null
        }
    }

    // Speech-to-text launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = matches?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                inputText = if (inputText.isBlank()) spoken else "$inputText $spoken"
            }
        }
    }

    // Photo gallery picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            capturedBitmap = null
        }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(uiState.chatMessages.size, uiState.isAiTyping) {
        val totalCount = listState.layoutInfo.totalItemsCount
        if (totalCount > 0) {
            listState.animateScrollToItem(totalCount - 1)
        }
    }

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
                        text = "AI Tutor • Online",
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )
                }
            }

            // New Chat Button with '+' Icon
            Row(
                modifier = Modifier
                    .clip(ShapeFull)
                    .background(colors.primarySoft)
                    .clickable {
                        onNewChat()
                        Toast.makeText(context, "Started fresh new chat", Toast.LENGTH_SHORT).show()
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Chat",
                    tint = colors.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "New Chat",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primary
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

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

        // Context Badge
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
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "NCTB Curriculum • Ask or snap any question",
                fontSize = 12.sp,
                color = colors.textMuted,
                fontWeight = FontWeight.Medium
            )
        }

        // Chat Messages List or Welcome Placeholder
        if (uiState.chatMessages.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Ask Porua AI Tutor",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Get step-by-step help with Physics, Chemistry, Math, Biology, and ICT. Type, talk, or snap a photo of any question.",
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = colors.textMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
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
                            steps = msg.text.split("\n\n").ifEmpty { msg.text.split("\n") },
                            answerSummary = msg.answerSummary,
                            syllabusNote = msg.syllabusNote,
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
                                text = "Porua is thinking •••",
                                fontSize = 12.sp,
                                color = colors.textMuted,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }

        // Quick Suggestion Chips (Context-Aware)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val suggestions = if (uiState.chatMessages.isEmpty()) {
                listOf("Show formula sheet", "Explain Vectors basics", "How does Daniell Cell work?", "Explain Newton's Laws")
            } else {
                listOf("Explain simpler", "Give a similar MCQ", "Show formula sheet")
            }
            items(suggestions) { suggestion ->
                PoruaFilterChip(
                    text = suggestion,
                    isSelected = false,
                    onClick = {
                        if (uiState.isAiTyping) return@PoruaFilterChip
                        when (suggestion) {
                            "Explain simpler" -> {
                                val lastAiMsg = uiState.chatMessages.lastOrNull { it.sender == ChatSender.PORUA_AI }?.text ?: "the previous question"
                                val snippet = if (lastAiMsg.length > 70) lastAiMsg.take(70) + "..." else lastAiMsg
                                onSendMessage("Regarding: '$snippet', please explain this concept in much simpler and easier terms with an example.", null, "image/jpeg")
                            }
                            "Give a similar MCQ" -> {
                                val lastAiMsg = uiState.chatMessages.lastOrNull { it.sender == ChatSender.PORUA_AI }?.text ?: "the previous question"
                                val snippet = if (lastAiMsg.length > 70) lastAiMsg.take(70) + "..." else lastAiMsg
                                onSendMessage("Based on: '$snippet', give 1 similar NCTB board-standard MCQ with 4 options (A, B, C, D) and explain the correct answer.", null, "image/jpeg")
                            }
                            "Show formula sheet" -> {
                                onSendMessage("Show formula sheet with all relevant equations, laws, and SI units for this topic.", null, "image/jpeg")
                            }
                            else -> {
                                onSendMessage(suggestion, null, "image/jpeg")
                            }
                        }
                    }
                )
            }
        }

        // Attached Photo Preview Chip
        if (selectedImageUri != null || capturedBitmap != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(ShapeSm)
                        .background(colors.surface2)
                        .border(1.dp, colors.border, ShapeSm)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (capturedBitmap != null) Icons.Default.CameraAlt else Icons.Default.Image,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (capturedBitmap != null) "Camera photo attached" else "Gallery photo attached",
                            fontSize = 12.sp,
                            color = colors.text,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove photo",
                            tint = colors.danger,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable {
                                    selectedImageUri = null
                                    capturedBitmap = null
                                }
                        )
                    }
                }
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
                text = "Secure AI tutor • NCTB aligned",
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
            // Picture upload / Camera button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.surface2)
                    .clickable { showImageSourceSheet = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Upload or take picture",
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
                        text = if (selectedImageUri != null || capturedBitmap != null) "Ask a question about this picture..." else "Ask about your syllabus...",
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

            // Voice Mic button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.surface2)
                    .clickable {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your study question...")
                        }
                        try {
                            speechLauncher.launch(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Voice input not supported on this device", Toast.LENGTH_SHORT).show()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Input",
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
                        if (inputText.isNotBlank() || selectedImageUri != null || capturedBitmap != null) {
                            var base64: String? = null
                            var mime = "image/jpeg"

                            // Handle directly captured bitmap
                            if (capturedBitmap != null) {
                                try {
                                    val out = ByteArrayOutputStream()
                                    capturedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 85, out)
                                    val bytes = out.toByteArray()
                                    base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                                    mime = "image/jpeg"
                                } catch (e: Exception) {
                                    // ignore
                                }
                            } else if (selectedImageUri != null) {
                                // Handle gallery URI with safe downsampling
                                selectedImageUri?.let { uri ->
                                    try {
                                        val detectedMime = context.contentResolver.getType(uri) ?: "image/jpeg"
                                        val stream = context.contentResolver.openInputStream(uri)
                                        
                                        // Downsample to avoid OOM
                                        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                                        BitmapFactory.decodeStream(stream, null, options)
                                        stream?.close()
                                        
                                        var sampleSize = 1
                                        val maxDim = 1280
                                        while ((options.outWidth / sampleSize) > maxDim || (options.outHeight / sampleSize) > maxDim) {
                                            sampleSize *= 2
                                        }
                                        
                                        val decodeStream = context.contentResolver.openInputStream(uri)
                                        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                                        val downsampled = BitmapFactory.decodeStream(decodeStream, null, decodeOpts)
                                        decodeStream?.close()
                                        
                                        if (downsampled != null) {
                                            val out = ByteArrayOutputStream()
                                            downsampled.compress(Bitmap.CompressFormat.JPEG, 80, out)
                                            val bytes = out.toByteArray()
                                            base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                                            mime = "image/jpeg"
                                        }
                                    } catch (e: Exception) {
                                        // fallback to simple read if needed
                                        try {
                                            val stream = context.contentResolver.openInputStream(uri)
                                            val bytes = stream?.readBytes()
                                            stream?.close()
                                            if (bytes != null) {
                                                base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                                                mime = "image/jpeg"
                                            }
                                        } catch (ex: Exception) { }
                                    }
                                }
                            }

                            onSendMessage(inputText, base64, mime)
                            inputText = ""
                            selectedImageUri = null
                            capturedBitmap = null
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

    // Image Source Picker Bottom Sheet (Camera or Gallery)
    if (showImageSourceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showImageSourceSheet = false },
            sheetState = imageSourceSheetState,
            containerColor = colors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Attach a Study Question",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Take a picture of your textbook question or upload from your gallery.",
                    fontSize = 13.sp,
                    color = colors.textMuted
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Option 1: Take Photo with Camera
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ShapeMd)
                        .background(colors.surface2)
                        .clickable {
                            showImageSourceSheet = false
                            try {
                                cameraLauncher.launch(null)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Camera not available: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colors.primarySoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Take Photo with Camera",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.text
                        )
                        Text(
                            text = "Snap a textbook problem or exam paper",
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Option 2: Choose from Gallery
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ShapeMd)
                        .background(colors.surface2)
                        .clickable {
                            showImageSourceSheet = false
                            photoPickerLauncher.launch("image/*")
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colors.successSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = colors.success,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Choose from Gallery",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.text
                        )
                        Text(
                            text = "Select an existing screenshot or document",
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Option 3: Full Screen Snap Question Scanner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ShapeMd)
                        .background(colors.surface2)
                        .clickable {
                            showImageSourceSheet = false
                            onOpenCameraSnap()
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colors.primarySoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Snap Question Scanner",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.text
                        )
                        Text(
                            text = "Open full viewfinder to scan & match offline bank",
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }
                }
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
