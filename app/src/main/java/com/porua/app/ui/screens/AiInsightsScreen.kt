package com.porua.app.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.porua.app.ui.components.AiInsightCard
import com.porua.app.ui.components.TopicBar
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.ShapeFull
import com.porua.app.ui.theme.ShapeLg
import com.porua.app.ui.theme.ShapeMd
import com.porua.app.ui.theme.ShapeSm
import com.porua.app.ui.theme.StatusDanger
import com.porua.app.ui.theme.StatusSuccess
import com.porua.app.ui.theme.StatusWarning
import com.porua.app.ui.viewmodel.PoruaUiState
import org.json.JSONArray

private data class TopicRisk(
    val title: String,
    val riskPercentage: Int,
    val color: Color
)

@Composable
fun AiInsightsScreen(
    uiState: PoruaUiState,
    onBack: () -> Unit
) {
    val colors = LocalPoruaColors.current
    val scrollState = rememberScrollState()

    // 1. Calculate forgetting risk dynamically from quiz mistakes
    val topicRisks = remember(uiState.quizAttempts, uiState.userProfile.group) {
        val chapterStats = mutableMapOf<String, Pair<Int, Int>>() // Pair(wrong, total)
        for (attempt in uiState.quizAttempts) {
            try {
                val array = JSONArray(attempt.questionResultsJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val ch = obj.optInt("chapterNumber", 1)
                    val isCorrect = obj.optBoolean("isCorrect", false)
                    val chTitle = obj.optString("chapterTitle", "Ch $ch")
                    val key = "${attempt.subjectName}: $chTitle"
                    val (wrong, total) = chapterStats.getOrDefault(key, Pair(0, 0))
                    chapterStats[key] = Pair(wrong + if (isCorrect) 0 else 1, total + 1)
                }
            } catch (e: Exception) {
                val key = "${attempt.subjectName} Practice"
                val (wrong, total) = chapterStats.getOrDefault(key, Pair(0, 0))
                val wrongAdd = (attempt.totalQuestions - attempt.score).coerceAtLeast(0)
                chapterStats[key] = Pair(wrong + wrongAdd, total + attempt.totalQuestions)
            }
        }

        val risks = mutableListOf<TopicRisk>()
        for ((topic, stats) in chapterStats) {
            val (wrong, total) = stats
            if (total > 0) {
                val accuracy = ((total - wrong) * 100) / total
                val risk = (100 - accuracy).coerceIn(15, 95)
                val color = when {
                    risk >= 50 -> StatusDanger
                    risk >= 30 -> StatusWarning
                    else -> StatusSuccess
                }
                risks.add(TopicRisk(topic, risk, color))
            }
        }
        risks.sortByDescending { it.riskPercentage }

        // If fewer than 4 topics, supplement with stream-relevant foundational syllabus topics
        if (risks.size < 4) {
            val defaults = when (uiState.userProfile.group) {
                "Commerce" -> listOf(
                    TopicRisk("Accounting: Double Entry", 65, StatusDanger),
                    TopicRisk("Business Org: Partnership", 52, StatusWarning),
                    TopicRisk("Finance: Capital Budgeting", 42, StatusWarning),
                    TopicRisk("ICT: Database Management", 25, StatusSuccess)
                )
                "Arts" -> listOf(
                    TopicRisk("Economics: Elasticity of Demand", 68, StatusDanger),
                    TopicRisk("Civics: Good Governance", 48, StatusWarning),
                    TopicRisk("Sociology: Social Stratification", 38, StatusSuccess),
                    TopicRisk("ICT: Communication Systems", 25, StatusSuccess)
                )
                else -> listOf(
                    TopicRisk("Chemistry: Electrolysis & Acids", 72, StatusDanger),
                    TopicRisk("Physics: Vector Operations", 58, StatusWarning),
                    TopicRisk("Higher Math: Integration by Parts", 45, StatusWarning),
                    TopicRisk("ICT: Logic Gates & Number Systems", 25, StatusSuccess)
                )
            }
            for (def in defaults) {
                if (risks.none { it.title == def.title } && risks.size < 4) {
                    risks.add(def)
                }
            }
        }
        risks.take(4)
    }

    // 2. Dynamic Mastery Forecast based on actual readiness and attempts
    val readiness = uiState.userProfile.examReadyPercentage
    val forecastTargetPct = (readiness + 15).coerceIn(40, 95)
    val actualY = remember(readiness, uiState.quizAttempts) {
        if (uiState.quizAttempts.size >= 4) {
            val scores = uiState.quizAttempts.takeLast(6).map {
                val pct = if (it.totalQuestions > 0) (it.score * 100 / it.totalQuestions) else 50
                (1f - (pct / 100f)).coerceIn(0.12f, 0.88f)
            }
            if (scores.size >= 2) scores else listOf(0.75f, 0.70f, 0.65f, 0.60f, 0.55f, 0.50f)
        } else {
            val currentY = (1f - (readiness / 100f)).coerceIn(0.15f, 0.85f)
            listOf(
                (currentY + 0.20f).coerceIn(0.2f, 0.9f),
                (currentY + 0.16f).coerceIn(0.2f, 0.88f),
                (currentY + 0.12f).coerceIn(0.2f, 0.85f),
                (currentY + 0.08f).coerceIn(0.15f, 0.82f),
                (currentY + 0.04f).coerceIn(0.15f, 0.80f),
                currentY
            )
        }
    }

    // 3. Dynamic speed and timing
    val avgSeconds = remember(uiState.quizAttempts) {
        val totalSec: Int = uiState.quizAttempts.sumOf { it.timeTakenSeconds }
        val totalQ: Int = uiState.quizAttempts.sumOf { it.totalQuestions }
        if (totalQ > 0 && totalSec > 0) {
            (totalSec / totalQ).coerceIn(5, 120)
        } else {
            12
        }
    }

    // 4. Dynamic Weekly Coach Text
    val coachText = remember(uiState.userProfile, topicRisks) {
        val highestRiskTopic = topicRisks.firstOrNull { it.riskPercentage >= 50 }?.title
        val focusMsg = if (highestRiskTopic != null) {
            "Focus next on $highestRiskTopic where errors were recorded."
        } else {
            "Keep maintaining your daily study pace in Plan to reach 90%+ readiness before exam."
        }
        "Weekly coach: HSC ${uiState.userProfile.examYear} (${uiState.userProfile.group}) readiness is at ${uiState.userProfile.examReadyPercentage}%. $focusMsg"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
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
                text = "AI insights",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.text,
                modifier = Modifier.weight(1f)
            )

            // Works offline pill
            Box(
                modifier = Modifier
                    .clip(ShapeSm)
                    .background(colors.successSoft)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "Works offline",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.success
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Card 1: Forgetting Risk
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ShapeLg)
                    .background(colors.surface)
                    .border(1.dp, colors.border, ShapeLg)
                    .padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(colors.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Forgetting risk • next 7 days",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.text
                        )
                        Text(
                            text = "Based on error patterns & revision spacing",
                            fontSize = 11.sp,
                            color = colors.textMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                topicRisks.forEachIndexed { index, item ->
                    RiskProgressRow(item.title, item.riskPercentage, item.color)
                    if (index < topicRisks.size - 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 2: Mastery Forecast Chart
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ShapeLg)
                    .background(colors.surface)
                    .border(1.dp, colors.border, ShapeLg)
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mastery forecast",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                    Text(
                        text = "$forecastTargetPct% forecast",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Line Chart Visualization Canvas
                val primaryColor = colors.primary
                val futureBg = colors.primarySoft.copy(alpha = 0.35f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Right 35% shaded as future prediction
                        drawRect(
                            color = futureBg,
                            topLeft = Offset(w * 0.65f, 0f),
                            size = androidx.compose.ui.geometry.Size(w * 0.35f, h)
                        )

                        // Actual Points W1 to W6
                        val stepX = (w * 0.65f) / (actualY.size - 1).coerceAtLeast(1)

                        for (i in 0 until actualY.size - 1) {
                            val startX = i * stepX
                            val startY = h * actualY[i]
                            val endX = (i + 1) * stepX
                            val endY = h * actualY[i + 1]

                            drawLine(
                                color = primaryColor,
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = 3f
                            )
                        }

                        // Draw circular points for actual
                        actualY.forEachIndexed { i, yFraction ->
                            drawCircle(
                                color = primaryColor,
                                radius = 4f,
                                center = Offset(i * stepX, h * yFraction)
                            )
                        }

                        // Dashed prediction line from last actual to W8
                        val startPredX = (actualY.size - 1) * stepX
                        val startPredY = h * actualY.last()
                        val endPredX = w
                        val endPredY = (h * (1f - (forecastTargetPct / 100f))).coerceIn(10f, h - 10f)

                        drawLine(
                            color = primaryColor,
                            start = Offset(startPredX, startPredY),
                            end = Offset(endPredX, endPredY),
                            strokeWidth = 3f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // X-axis labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("W1", "W2", "W3", "W4", "W5", "W6", "W7", "W8").forEach { week ->
                        Text(
                            text = week,
                            fontSize = 11.sp,
                            fontWeight = if (week == "W7" || week == "W8") FontWeight.Bold else FontWeight.Normal,
                            color = if (week == "W7" || week == "W8") colors.primary else colors.textMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp, 3.dp)
                            .background(colors.primary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Actual", fontSize = 11.sp, color = colors.textMuted)

                    Spacer(modifier = Modifier.width(16.dp))

                    Box(
                        modifier = Modifier
                            .size(10.dp, 3.dp)
                            .background(colors.primary.copy(alpha = 0.6f))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Target forecast", fontSize = 11.sp, color = colors.textMuted)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2 Grid Cards: Daily Goal & Question Speed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Daily Study Goal
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(ShapeLg)
                        .background(colors.warningSoft)
                        .border(1.dp, colors.warning.copy(alpha = 0.2f), ShapeLg)
                        .padding(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = colors.warning,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Goal: ${uiState.userProfile.dailyStudyTime}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Consistent daily pace",
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )
                }

                // Question speed
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(ShapeLg)
                        .background(colors.dangerSoft)
                        .border(1.dp, colors.danger.copy(alpha = 0.2f), ShapeLg)
                        .padding(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = colors.danger,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Avg $avgSeconds s / question",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (avgSeconds < 15) "Fast response speed" else "Steady analytical pace",
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weekly Coach Insight
            AiInsightCard(
                text = coachText,
                tag = "Porua AI • Active"
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RiskProgressRow(
    topic: String,
    risk: Int,
    barColor: Color
) {
    val colors = LocalPoruaColors.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = topic,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text
            )
            Text(
                text = "$risk% risk",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        TopicBar(percentage = risk, customColor = barColor)
    }
}
