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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Insights
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
import com.porua.app.data.local.entity.QuizAttemptEntity
import org.json.JSONArray
import com.porua.app.ui.components.BottomNavItem
import com.porua.app.ui.components.MasteryRing
import com.porua.app.ui.components.PoruaBottomNav
import com.porua.app.ui.components.PoruaFilterChip
import com.porua.app.ui.components.TopicBar
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.ShapeFull
import com.porua.app.ui.theme.ShapeLg
import com.porua.app.ui.theme.ShapeMd
import com.porua.app.ui.theme.ShapeSm
import com.porua.app.ui.theme.StatusDanger
import com.porua.app.ui.theme.StatusSuccess
import com.porua.app.ui.theme.StatusWarning
import com.porua.app.ui.theme.getMasteryColor
import com.porua.app.ui.viewmodel.PoruaUiState

@Composable
fun ProgressScreen(
    uiState: PoruaUiState,
    onNavigateBottom: (BottomNavItem) -> Unit,
    onOpenAiInsights: () -> Unit
) {
    val colors = LocalPoruaColors.current
    var selectedFilter by remember { mutableStateOf("All") }

    val chapterPerformances = remember(uiState.quizAttempts, uiState.currentSubjectChapters) {
        val map = mutableMapOf<String, Triple<String, Int, Int>>() // key -> (title, correct, wrong)
        val subjectNameMap = mutableMapOf<String, String>()

        uiState.quizAttempts.forEach { attempt ->
            subjectNameMap[attempt.subjectId] = attempt.subjectName
            try {
                val arr = org.json.JSONArray(attempt.questionResultsJson)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val chNum = obj.optInt("chapterNumber", 1)
                    val chTitle = obj.optString("chapterTitle", "Chapter $chNum")
                    val isCorrect = obj.optBoolean("isCorrect", false)
                    val isSkipped = obj.optBoolean("isSkipped", false)
                    if (isSkipped) continue
                    val key = "${attempt.subjectId}_$chNum"
                    val existing = map[key]
                    val c = (existing?.second ?: 0) + (if (isCorrect) 1 else 0)
                    val w = (existing?.third ?: 0) + (if (!isCorrect) 1 else 0)
                    map[key] = Triple(chTitle, c, w)
                }
            } catch (_: Exception) {}
        }

        val list = mutableListOf<ChapterPerformance>()
        map.forEach { (key, triple) ->
            val parts = key.split("_")
            val subId = parts[0]
            val chNum = parts.getOrNull(1)?.toIntOrNull() ?: 1
            val title = triple.first
            val correct = triple.second
            val wrong = triple.third
            val total = correct + wrong
            val acc = if (total > 0) (correct * 100) / total else 0
            val subName = subjectNameMap[subId] ?: subId.replaceFirstChar { it.uppercase() }
            val status = when {
                wrong > 0 || acc < 60 -> "Needs Revision"
                acc >= 85 -> "Mastered"
                else -> "On Track"
            }
            list.add(
                ChapterPerformance(
                    key = key,
                    subjectId = subId,
                    subjectName = subName,
                    chapterNumber = chNum,
                    chapterTitle = title,
                    correctCount = correct,
                    wrongCount = wrong,
                    totalAttempts = total,
                    accuracy = acc,
                    status = status
                )
            )
        }

        // Add baseline chapters from currentSubjectChapters if not already present
        uiState.currentSubjectChapters.forEach { ch ->
            val key = "${ch.subjectId}_${ch.chapterNumber}"
            if (!map.containsKey(key)) {
                val status = when {
                    ch.mastery in 1..49 -> "Needs Revision"
                    ch.mastery >= 85 -> "Mastered"
                    ch.mastery in 50..84 -> "On Track"
                    else -> "On Track"
                }
                list.add(
                    ChapterPerformance(
                        key = key,
                        subjectId = ch.subjectId,
                        subjectName = "Chemistry",
                        chapterNumber = ch.chapterNumber,
                        chapterTitle = ch.name,
                        correctCount = 0,
                        wrongCount = if (ch.mastery in 1..49) 3 else 0,
                        totalAttempts = 0,
                        accuracy = ch.mastery,
                        status = status
                    )
                )
            }
        }

        list
    }

    val subjectProgressList = remember(uiState.subjects, uiState.quizAttempts, uiState.userProfile.group) {
        val group = uiState.userProfile.group.ifBlank { "Science" }
        val contentSubjects = com.porua.app.data.content.ContentRepository.getSubjectsForGroup(group).ifEmpty {
            com.porua.app.data.content.ContentRepository.getSubjectsForGroup("Science")
        }

        val completedQuestionKeys = mutableSetOf<String>()
        uiState.quizAttempts.forEach { att ->
            try {
                val arr = org.json.JSONArray(att.questionResultsJson)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val qText = obj.optString("questionText", "").trim().lowercase()
                    val chNum = obj.optInt("chapterNumber", 1)
                    val qKey = if (qText.isNotBlank()) {
                        "${att.subjectId.lowercase()}_${chNum}_$qText"
                    } else {
                        "${att.subjectId.lowercase()}_${chNum}_q$i"
                    }
                    completedQuestionKeys.add(qKey)
                }
            } catch (_: Exception) {}
        }

        contentSubjects.map { cs ->
            val totalQs = com.porua.app.data.content.ContentRepository.getTotalQuestionsForSubject(cs.id)
            val compQs = completedQuestionKeys.count { key ->
                val subPart = key.substringBefore("_")
                subPart == cs.id.lowercase() ||
                subPart.contains(cs.id.lowercase()) ||
                cs.id.lowercase().contains(subPart) ||
                cs.name.lowercase().startsWith(subPart)
            }
            val pct = ((compQs * 100) / totalQs.coerceAtLeast(1)).coerceIn(0, 100)
            Triple(cs.name, pct, Pair(compQs, totalQs))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))

                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mastery Map",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.text
                    )

                    // Open AI Insights shortcut
                    Row(
                        modifier = Modifier
                            .clip(ShapeSm)
                            .background(colors.surface)
                            .border(1.dp, colors.border, ShapeSm)
                            .clickable { onOpenAiInsights() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Insights,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Insights",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Overall Readiness Card
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
                        percentage = uiState.userProfile.examReadyPercentage,
                        size = 64.dp,
                        strokeWidth = 7.dp
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "${uiState.userProfile.examReadyPercentage}% Overall Readiness",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.text
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val totalGroupQs = com.porua.app.data.content.ContentRepository.getTotalQuestionsForGroup(uiState.userProfile.group)
                        Text(
                            text = "${uiState.totalCompletedQuestions} of $totalGroupQs MCQs completed • ${uiState.userProfile.daysRemaining} days remaining",
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Filter Chips
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf("All", "Needs Revision", "On Track", "Mastered")
                    items(filters) { filter ->
                        PoruaFilterChip(
                            text = filter,
                            isSelected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            activeColor = when (filter) {
                                "Needs Revision" -> colors.danger
                                "On Track" -> colors.primary
                                "Mastered" -> colors.success
                                else -> colors.primary
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // If "On Track" is selected, render the Exam Performance Graph as requested
            if (selectedFilter == "On Track") {
                item {
                    ExamPerformanceTrendCard(attempts = uiState.quizAttempts)
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Section Header based on Selected Filter
            item {
                val headerText = when (selectedFilter) {
                    "Needs Revision" -> "Chapters Needing Urgent Revision (Most Mistakes)"
                    "On Track" -> "On Track Chapters (Solid Progress)"
                    "Mastered" -> "Mastered Chapters (High Accuracy)"
                    else -> "All Active Chapters"
                }
                Text(
                    text = headerText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Chapter list matching the selected filter
            val displayedChapters = when (selectedFilter) {
                "Needs Revision" -> chapterPerformances.filter { it.status == "Needs Revision" }
                    .sortedByDescending { it.wrongCount }
                "On Track" -> chapterPerformances.filter { it.status == "On Track" }
                    .sortedByDescending { it.accuracy }
                "Mastered" -> chapterPerformances.filter { it.status == "Mastered" }
                    .sortedByDescending { it.accuracy }
                else -> chapterPerformances
            }

            if (displayedChapters.isNotEmpty()) {
                items(displayedChapters) { perf ->
                    ChapterPerformanceCard(performance = perf)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(ShapeMd)
                            .background(colors.surface)
                            .border(1.dp, colors.border, ShapeMd)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val emptyMsg = when (selectedFilter) {
                            "Needs Revision" -> "🎉 No chapters need revision! You have zero weak areas from your quizzes."
                            "On Track" -> "No chapters currently in the on-track range. Take a practice quiz to get started."
                            "Mastered" -> "No mastered chapters yet. Score 85%+ on a chapter quiz to master it!"
                            else -> "No chapters found. Please select your active stream in Profile."
                        }
                        Text(
                            text = emptyMsg,
                            fontSize = 13.sp,
                            color = colors.textMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))

                // Section: Subject Mastery
                Text(
                    text = "Subject Performance",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Subject Cards with Real Completion Progress
            items(subjectProgressList) { (name, pct, counts) ->
                SubjectMasteryCard(
                    subjectName = name,
                    mastery = pct,
                    completedQuestions = counts.first,
                    totalQuestions = counts.second
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))

                // Section: Recent Quiz Attempts
                Text(
                    text = "Recent Quiz Attempts",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (uiState.quizAttempts.isNotEmpty()) {
                items(uiState.quizAttempts.take(10)) { attempt ->
                    QuizAttemptRow(attempt = attempt)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(ShapeMd)
                            .background(colors.surface)
                            .border(1.dp, colors.border, ShapeMd)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No quiz attempts yet • Complete a 20-MCQ quiz to see your performance history",
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        PoruaBottomNav(
            selectedItem = BottomNavItem.PROGRESS,
            onItemSelected = onNavigateBottom
        )
    }
}

@Composable
private fun WeakTopicRow(
    topic: String,
    subjectDesc: String,
    mastery: Int
) {
    val colors = LocalPoruaColors.current
    val masteryColor = getMasteryColor(mastery)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeLg)
            .background(colors.surface)
            .border(1.dp, colors.border, ShapeLg)
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
                text = topic,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text
            )
            Text(
                text = subjectDesc,
                fontSize = 11.sp,
                color = colors.textMuted
            )
        }

        Box(
            modifier = Modifier
                .clip(ShapeSm)
                .background(masteryColor.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "$mastery% mastery",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = masteryColor
            )
        }
    }
}

@Composable
private fun SubjectMasteryCard(
    subjectName: String,
    mastery: Int,
    completedQuestions: Int = 0,
    totalQuestions: Int = 0
) {
    val colors = LocalPoruaColors.current
    val masteryColor = getMasteryColor(mastery)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeLg)
            .background(colors.surface)
            .border(1.dp, colors.border, ShapeLg)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = subjectName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )
                if (totalQuestions > 0) {
                    Text(
                        text = "$completedQuestions of $totalQuestions MCQs completed",
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )
                }
            }
            Text(
                text = "$mastery%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = masteryColor
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        TopicBar(percentage = mastery)
    }
}

data class ChapterPerformance(
    val key: String,
    val subjectId: String,
    val subjectName: String,
    val chapterNumber: Int,
    val chapterTitle: String,
    val correctCount: Int,
    val wrongCount: Int,
    val totalAttempts: Int,
    val accuracy: Int,
    val status: String
)

@Composable
private fun ChapterPerformanceCard(performance: ChapterPerformance) {
    val colors = LocalPoruaColors.current
    val statusColor = when (performance.status) {
        "Needs Revision" -> colors.danger
        "Mastered" -> colors.success
        else -> colors.primary
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeLg)
            .background(colors.surface)
            .border(1.dp, statusColor.copy(alpha = 0.35f), ShapeLg)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = performance.chapterTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${performance.subjectName} • Chapter ${performance.chapterNumber}",
                    fontSize = 11.sp,
                    color = colors.textMuted
                )
            }

            Box(
                modifier = Modifier
                    .clip(ShapeSm)
                    .background(statusColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (performance.status == "Needs Revision") "${performance.wrongCount} Mistakes" else performance.status,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Accuracy: ${performance.accuracy}% (${performance.correctCount} correct, ${performance.wrongCount} wrong)",
                fontSize = 11.sp,
                color = colors.textMuted
            )
            Text(
                text = "${performance.accuracy}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        TopicBar(percentage = performance.accuracy)
    }
}

@Composable
private fun ExamPerformanceTrendCard(attempts: List<QuizAttemptEntity>) {
    val colors = LocalPoruaColors.current
    if (attempts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(ShapeLg)
                .background(colors.surface)
                .border(1.dp, colors.border, ShapeLg)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Complete at least one practice quiz to generate your Performance Trend Graph.",
                fontSize = 12.sp,
                color = colors.textMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        return
    }

    val reversedAttempts = attempts.take(8).reversed()
    val avgScore = (attempts.map { if (it.totalQuestions > 0) (it.score * 100) / it.totalQuestions else 0 }.average()).toInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeLg)
            .background(colors.surface)
            .border(1.dp, colors.border, ShapeLg)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "📈 Exam Performance Graph",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Total Exams: ${attempts.size} • Average Score: $avgScore%",
                    fontSize = 12.sp,
                    color = colors.textMuted
                )
            }

            Box(
                modifier = Modifier
                    .clip(ShapeSm)
                    .background(if (avgScore >= 75) StatusSuccess.copy(alpha = 0.15f) else colors.primary.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (avgScore >= 75) "On Track" else "Improving",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (avgScore >= 75) StatusSuccess else colors.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Visual Graph Bars
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            reversedAttempts.forEachIndexed { index, attempt ->
                val pct = if (attempt.totalQuestions > 0) (attempt.score * 100) / attempt.totalQuestions else 0
                val barColor = when {
                    pct >= 80 -> StatusSuccess
                    pct >= 60 -> colors.primary
                    else -> StatusDanger
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "$pct%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = barColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height((pct * 0.85).dp.coerceIn(12.dp, 85.dp))
                            .clip(ShapeSm)
                            .background(barColor)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Q${index + 1}",
                        fontSize = 10.sp,
                        color = colors.textMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizAttemptRow(attempt: QuizAttemptEntity) {
    val colors = LocalPoruaColors.current
    val percentage = if (attempt.totalQuestions > 0) (attempt.score * 100) / attempt.totalQuestions else 0
    val scoreColor = when {
        percentage >= 80 -> StatusSuccess
        percentage >= 50 -> StatusWarning
        else -> StatusDanger
    }

    val minutes = attempt.timeTakenSeconds / 60
    val seconds = attempt.timeTakenSeconds % 60
    val timeFormatted = "${minutes}m ${seconds}s"

    val dateDisplay = if (attempt.timestamp > 0) {
        val attemptDateObj = java.util.Date(attempt.timestamp)
        val isToday = android.text.format.DateUtils.isToday(attempt.timestamp)
        val timeStr = java.text.SimpleDateFormat("h:mm a", java.util.Locale.ENGLISH).format(attemptDateObj)
        if (isToday) "Today, $timeStr" else {
            val dateStr = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale.ENGLISH).format(attemptDateObj)
            "$dateStr • $timeStr"
        }
    } else {
        attempt.attemptDate
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeLg)
            .background(colors.surface)
            .border(1.dp, colors.border, ShapeLg)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = attempt.subjectName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$dateDisplay • $timeFormatted",
                    fontSize = 12.sp,
                    color = colors.textMuted
                )
            }

            Box(
                modifier = Modifier
                    .clip(ShapeSm)
                    .background(scoreColor.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$percentage%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = scoreColor
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "✓ ${attempt.score} Correct",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = StatusSuccess
            )
            Text(
                text = "✗ ${attempt.wrongCount} Wrong",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = StatusDanger
            )
            Text(
                text = "— ${attempt.skippedCount} Skipped",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textMuted
            )
        }
    }
}
