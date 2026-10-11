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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.porua.app.data.local.entity.ChapterEntity
import com.porua.app.ui.components.MasteryRing
import com.porua.app.ui.components.PoruaFilterChip
import com.porua.app.ui.components.TopicBar
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.ShapeFull
import com.porua.app.ui.theme.ShapeLg
import com.porua.app.ui.theme.ShapeMd
import com.porua.app.ui.theme.ShapeSm
import com.porua.app.ui.theme.getMasteryColor
import com.porua.app.ui.theme.getMasterySoftColor

@Composable
fun ChaptersScreen(
    subjectTitle: String = "Chemistry • 2nd paper",
    chapters: List<ChapterEntity>,
    onBack: () -> Unit,
    onChapterClick: (ChapterEntity) -> Unit
) {
    val colors = LocalPoruaColors.current
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredChapters = remember(chapters, selectedFilter) {
        when (selectedFilter) {
            "Weak first" -> chapters.sortedBy { it.mastery }
            "Not started" -> chapters.filter { it.mastery == 0 }
            "Bookmarked" -> chapters.filter { it.isBookmarked }
            else -> chapters
        }
    }

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

            Text(
                text = subjectTitle,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = colors.text,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))

                // Mastery Overview Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ShapeLg)
                        .background(colors.surface)
                        .border(1.dp, colors.border, ShapeLg)
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MasteryRing(
                        percentage = 62,
                        size = 64.dp,
                        strokeWidth = 7.dp
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "62% chapter mastery",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.text
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "5 of 12 chapters strong • 2 need urgent revision",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = colors.textMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Horizontal Filter Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf("All", "Weak first", "Not started", "Bookmarked")
                    items(filters) { filter ->
                        PoruaFilterChip(
                            text = filter,
                            isSelected = selectedFilter == filter,
                            onClick = { selectedFilter = filter }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Chapter Cards
            items(filteredChapters) { chapter ->
                ChapterCard(
                    chapter = chapter,
                    onClick = { onChapterClick(chapter) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ChapterCard(
    chapter: ChapterEntity,
    onClick: () -> Unit
) {
    val colors = LocalPoruaColors.current
    val masteryColor = getMasteryColor(chapter.mastery)
    val masterySoft = getMasterySoftColor(chapter.mastery)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeLg)
            .background(colors.surface)
            .border(1.dp, colors.border, ShapeLg)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ch ${chapter.chapterNumber}  ${chapter.name}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text,
                modifier = Modifier.weight(1f)
            )

            // Percentage Badge
            Box(
                modifier = Modifier
                    .clip(ShapeSm)
                    .background(masterySoft)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "${chapter.mastery}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = masteryColor
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Progress Bar
        TopicBar(percentage = chapter.mastery)

        Spacer(modifier = Modifier.height(10.dp))

        // Subtitle Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${chapter.topicsCount} topics • ${chapter.questionsCount} Qs • last ${if (chapter.lastStudiedDaysAgo == 0) "today" else "${chapter.lastStudiedDaysAgo} d ago"}",
                fontSize = 12.sp,
                color = colors.textMuted
            )

            if (chapter.isBookmarked) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
