package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.WildWatermelon
import kotlin.math.roundToInt

@Composable
fun CalorieRing(
    consumedCalories: Double,
    targetCalories: Int,
    appLanguage: String = "ru",
    modifier: Modifier = Modifier,
    ringSize: Dp = 145.dp
) {
    val target = targetCalories.coerceAtLeast(1)
    val consumed = consumedCalories.roundToInt()
    val remaining = target - consumed
    val rawProgress = (consumed.toFloat() / target.toFloat()).coerceAtLeast(0f)
    val sweepProgress = rawProgress.coerceAtMost(1f)

    val animatedSweep by animateFloatAsState(
        targetValue = sweepProgress,
        animationSpec = tween(durationMillis = 900),
        label = "calorie_sweep"
    )

    val ringColor = when {
        consumed > target -> Color(0xFFF87171)
        consumed in (target * 0.95).toInt()..target -> Color(0xFF4ADE80)
        else -> WildWatermelon
    }

    val isCompact = ringSize <= 160.dp

    val desc = "Кольцо калорий: употреблено $consumed из $target ккал. ${if (remaining >= 0) "$remaining ккал осталось" else "${-remaining} ккал сверх нормы"}"

    val trackColor = DarkBorder

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(ringSize)
            .semantics { contentDescription = desc }
    ) {
        Canvas(modifier = Modifier.size(ringSize - 10.dp)) {
            val strokeWidth = (if (isCompact) 11.dp else 16.dp).toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
            val arcSize = Size(diameter, diameter)

            // Background circle track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active progress arc
            if (animatedSweep > 0f) {
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = animatedSweep * 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // Inner stats display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (remaining >= 0) "$remaining" else "+${-remaining}",
                fontSize = if (isCompact) 26.sp else 36.sp,
                fontWeight = FontWeight.Bold,
                color = if (remaining >= 0) TextPrimary else ringColor,
                lineHeight = if (isCompact) 28.sp else 38.sp
            )
            Text(
                text = if (remaining >= 0) {
                    if (appLanguage == "en") "kcal left" else "ккал осталось"
                } else {
                    if (appLanguage == "en") "kcal over goal" else "ккал сверх нормы"
                },
                fontSize = if (isCompact) 10.sp else 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(if (isCompact) 2.dp else 6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$consumed",
                    fontSize = if (isCompact) 11.sp else 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ringColor
                )
                Text(
                    text = " / $target",
                    fontSize = if (isCompact) 11.sp else 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextTertiary
                )
            }
        }
    }
}
