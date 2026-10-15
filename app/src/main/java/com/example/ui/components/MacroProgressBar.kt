package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CarbsColor
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.FatColor
import com.example.ui.theme.ProteinColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun MacroProgressBar(
    title: String,
    consumedGrams: Double,
    targetGrams: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val target = targetGrams.coerceAtLeast(1)
    val consumed = consumedGrams
    val remaining = (target - consumed).coerceAtLeast(0.0)
    val progressRatio = (consumed / target).toFloat().coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progressRatio,
        animationSpec = tween(durationMillis = 800),
        label = "macro_progress"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkCard)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            Text(
                text = String.format(Locale.US, "%.1f / %d г", consumed, target),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(DarkBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(RoundedCornerShape(4.dp))
                    .background(accentColor)
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = String.format(Locale.US, "осталось: %.1f г", remaining),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = TextTertiary
            )
        }
    }
}

@Composable
fun MacroOverviewSection(
    consumedProtein: Double,
    targetProtein: Int,
    consumedFat: Double,
    targetFat: Int,
    consumedCarbs: Double,
    targetCarbs: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MacroProgressBar(
            title = "Белки (Б)",
            consumedGrams = consumedProtein,
            targetGrams = targetProtein,
            accentColor = ProteinColor
        )
        MacroProgressBar(
            title = "Жиры (Ж)",
            consumedGrams = consumedFat,
            targetGrams = targetFat,
            accentColor = FatColor
        )
        MacroProgressBar(
            title = "Углеводы (У)",
            consumedGrams = consumedCarbs,
            targetGrams = targetCarbs,
            accentColor = CarbsColor
        )
    }
}
