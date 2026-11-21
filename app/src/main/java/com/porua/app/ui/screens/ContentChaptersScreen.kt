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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.porua.app.data.content.ContentChapter
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.ShapeLg
import com.porua.app.ui.theme.ShapeSm

@Composable
fun ContentChaptersScreen(
    subjectName: String,
    chapters: List<ContentChapter>,
    onBack: () -> Unit,
    onChapterClick: (ContentChapter, String) -> Unit  // chapter + mode: "mcq" | "short"
) {
    val colors = LocalPoruaColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
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
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = subjectName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )
                Text(
                    text = "${chapters.size} chapters",
                    fontSize = 12.sp,
                    color = colors.textMuted
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            items(chapters) { chapter ->
                ContentChapterCard(
                    chapter = chapter,
                    onMcqClick    = { onChapterClick(chapter, "mcq") },
                    onShortClick  = { onChapterClick(chapter, "short") }
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ContentChapterCard(
    chapter: ContentChapter,
    onMcqClick: () -> Unit,
    onShortClick: () -> Unit
) {
    val colors = LocalPoruaColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeLg)
            .background(colors.surface)
            .border(1.dp, colors.border, ShapeLg)
            .padding(16.dp)
    ) {
        // Chapter number badge + title
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .clip(ShapeSm)
                    .background(colors.primarySoft)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Ch ${chapter.chapterNumber}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = chapter.paper,
                fontSize = 11.sp,
                color = colors.textMuted,
                modifier = Modifier
                    .clip(ShapeSm)
                    .background(colors.surface2)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = chapter.title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = colors.text
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${chapter.mcqs.size} MCQ  •  ${chapter.shortQuestions.size} Short Q",
            fontSize = 12.sp,
            color = colors.textMuted
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // MCQ button
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(ShapeLg)
                    .background(colors.primarySoft)
                    .clickable(enabled = chapter.mcqs.isNotEmpty()) { onMcqClick() }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Quiz,
                    contentDescription = null,
                    tint = if (chapter.mcqs.isNotEmpty()) colors.primary else colors.textMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "MCQ Practice",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (chapter.mcqs.isNotEmpty()) colors.primary else colors.textMuted
                )
            }

            // Short Q button
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(ShapeLg)
                    .background(colors.warningSoft)
                    .clickable(enabled = chapter.shortQuestions.isNotEmpty()) { onShortClick() }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.EditNote,
                    contentDescription = null,
                    tint = if (chapter.shortQuestions.isNotEmpty()) colors.warning else colors.textMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Short Q",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (chapter.shortQuestions.isNotEmpty()) colors.warning else colors.textMuted
                )
            }
        }
    }
}
