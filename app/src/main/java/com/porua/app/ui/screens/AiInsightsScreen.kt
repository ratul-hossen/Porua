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

@Composable
fun AiInsightsScreen(
    onBack: () -> Unit
) {
    val colors = LocalPoruaColors.current
    val scrollState = rememberScrollState()

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
                            text = "On-device ML • updated 2 h ago",
                            fontSize = 11.sp,
                            color = colors.textMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                RiskProgressRow("Electrolysis", 82, StatusDanger)
                Spacer(modifier = Modifier.height(12.dp))
                RiskProgressRow("Alcohols", 74, StatusDanger)
                Spacer(modifier = Modifier.height(12.dp))
                RiskProgressRow("Integration by parts", 61, StatusWarning)
                Spacer(modifier = Modifier.height(12.dp))
                RiskProgressRow("Vectors", 38, StatusSuccess)
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
                        text = "63% by W7",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Line Chart Visualization Canvas
                val primaryColor = colors.primary
                val textColor = colors.textMuted
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
                        val actualY = listOf(0.75f, 0.70f, 0.65f, 0.62f, 0.58f, 0.52f)
                        val stepX = w * 0.65f / (actualY.size - 1)

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

                        // Dashed prediction line from W6 to W8
                        val startPredX = w * 0.65f
                        val startPredY = h * actualY.last()
                        val endPredX = w
                        val endPredY = h * 0.40f

                        drawLine(
                            color = Color.Black,
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
                            .background(Color.Black)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "ML prediction", fontSize = 11.sp, color = colors.textMuted)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2 Grid Cards: Best time & Rushing numericals
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Best time
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
                        text = "Best time: 6–8 AM",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "+18% accuracy vs evenings",
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )
                }

                // Rushing numericals
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
                        text = "Rushing numericals",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Avg 9 s per question",
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weekly Coach Insight
            AiInsightCard(
                text = "Weekly coach: you improved Chemistry by 9%. Next week focus on Electrolysis and Carbonyl compounds.",
                tag = "Gemini • online"
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
