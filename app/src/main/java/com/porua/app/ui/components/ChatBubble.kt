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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    // Clean and organize markdown, headers, and tables
    val cleanedText = cleanEducationalText(steps.joinToString("\n\n"))
    val blocks = cleanedText.split("\n\n").filter { it.isNotBlank() }

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
                text = "Porua AI • Step-by-Step Solution",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.primary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Organized Educational Content
        blocks.forEach { block ->
            val trimmed = block.trim()
            if (trimmed.startsWith("#")) {
                val cleanHeader = trimmed.replace(Regex("^#+\\s*"), "")
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = cleanHeader,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text,
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            } else {
                val annotated = parseMarkdownToAnnotatedString(
                    text = trimmed,
                    primaryColor = colors.primary,
                    textColor = colors.text
                )
                Text(
                    text = annotated,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = colors.text,
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }
        }

        // Green Answer Badge (Only displayed when real answer exists, not hardcoded dummy tags)
        val hasRealAnswer = answerSummary.isNotBlank() && answerSummary != "Porua AI"
        if (hasRealAnswer || syllabusNote.isNotBlank()) {
            val badgeText = when {
                hasRealAnswer && syllabusNote.isNotBlank() -> "Answer: $answerSummary • $syllabusNote"
                hasRealAnswer -> "Answer: $answerSummary"
                else -> syllabusNote
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ShapeMd)
                    .background(colors.successSoft)
                    .padding(10.dp)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.success
                )
            }
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
                    .clickable {
                        val fullText = buildString {
                            append(cleanedText)
                            if (hasRealAnswer) appendLine("\nAnswer: $answerSummary")
                            if (syllabusNote.isNotBlank()) appendLine("\n$syllabusNote")
                        }.trim()
                        clipboardManager.setText(AnnotatedString(fullText))
                        android.widget.Toast.makeText(context, "Copied response to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                    }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Default.ThumbUp,
                contentDescription = "Helpful",
                tint = colors.textMuted,
                modifier = Modifier
                    .size(18.dp)
                    .clickable {
                        android.widget.Toast.makeText(context, "Marked as helpful!", android.widget.Toast.LENGTH_SHORT).show()
                    }
            )
        }
    }
}

private fun cleanEducationalText(rawText: String): String {
    val lines = rawText.lines()
    val cleanedLines = mutableListOf<String>()

    for (line in lines) {
        val trimmed = line.trim()

        // 1. Skip table divider lines like |---|---| or dashes ---
        if ((trimmed.startsWith("|") && trimmed.contains("---")) || trimmed.matches(Regex("^-{3,}$"))) {
            continue
        }

        // 2. Convert table rows | col1 | col2 | into clean bullet points: • col1: col2
        if (trimmed.startsWith("|") && trimmed.endsWith("|")) {
            val cols = trimmed.split("|")
                .map { it.replace("*", "").trim() }
                .filter { it.isNotBlank() }

            if (cols.isNotEmpty()) {
                if (cols.size == 1) {
                    cleanedLines.add("• ${cols[0]}")
                } else if (cols.size == 2) {
                    cleanedLines.add("• ${cols[0]}: ${cols[1]}")
                } else {
                    cleanedLines.add("• ${cols[0]}: ${cols.subList(1, cols.size).joinToString(" — ")}")
                }
                continue
            }
        }

        // 3. Clean redundant answer headers like "**Answer (in Bangla + English for clarity):**"
        if (trimmed.startsWith("**Answer", ignoreCase = true) && trimmed.endsWith("**")) {
            continue
        }

        // 4. Remove leading markdown hashtags (#, ##, ###)
        val cleanLine = trimmed.replace(Regex("^#+\\s*"), "")

        cleanedLines.add(cleanLine)
    }

    return cleanedLines.joinToString("\n")
}

private fun parseMarkdownToAnnotatedString(
    text: String,
    primaryColor: Color,
    textColor: Color
): AnnotatedString {
    return buildAnnotatedString {
        // Match **bold** or `code` or strip stray asterisks cleanly
        val regex = Regex("\\*\\*(.*?)\\*\\*|`(.*?)`|\\*(.*?)\\*")
        var currentIndex = 0

        for (match in regex.findAll(text)) {
            if (match.range.first > currentIndex) {
                append(text.substring(currentIndex, match.range.first).replace("*", ""))
            }

            val boldContent = match.groups[1]?.value
            val codeContent = match.groups[2]?.value
            val italicContent = match.groups[3]?.value

            if (boldContent != null) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = textColor)) {
                    append(boldContent.replace("*", ""))
                }
            } else if (codeContent != null) {
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = primaryColor)) {
                    append(codeContent)
                }
            } else if (italicContent != null) {
                withStyle(SpanStyle(fontWeight = FontWeight.Medium, color = textColor)) {
                    append(italicContent.replace("*", ""))
                }
            }

            currentIndex = match.range.last + 1
        }

        if (currentIndex < text.length) {
            append(text.substring(currentIndex).replace("*", ""))
        }
    }
}
