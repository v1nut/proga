package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.WaterAqua
import com.example.ui.theme.WaterAquaContainer
import kotlin.math.roundToInt

@Composable
fun WaterTrackerCard(
    currentWaterMl: Int,
    targetWaterMl: Int,
    onAddWater: (amountMl: Int) -> Unit,
    onRemoveLastWater: () -> Unit,
    appLanguage: String = "ru",
    modifier: Modifier = Modifier
) {
    val progress = if (targetWaterMl > 0) {
        (currentWaterMl.toFloat() / targetWaterMl.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600),
        label = "water_progress_anim"
    )

    val percent = (progress * 100).roundToInt()
    val remainingMl = (targetWaterMl - currentWaterMl).coerceAtLeast(0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(DarkCard)
            .border(1.dp, DarkBorder, RoundedCornerShape(24.dp))
            .padding(18.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(WaterAquaContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Opacity,
                        contentDescription = "Вода",
                        tint = WaterAqua,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (appLanguage == "en") "Water Tracker" else "Водный баланс",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = if (remainingMl == 0) {
                            if (appLanguage == "en") "Goal reached! 🎉" else "Норма выполнена! 🎉"
                        } else {
                            if (appLanguage == "en") "$remainingMl ml remaining" else "Осталось: $remainingMl мл"
                        },
                        fontSize = 11.sp,
                        color = if (remainingMl == 0) WaterAqua else TextSecondary,
                        maxLines = 1
                    )
                }
            }

            // Value badge
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(start = 6.dp)
            ) {
                Text(
                    text = "$currentWaterMl",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = WaterAqua
                )
                Text(
                    text = if (appLanguage == "en") " / $targetWaterMl ml" else " / $targetWaterMl мл",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 1.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(DarkCardElevated)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(WaterAqua, Color(0xFF38BDF8))
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (appLanguage == "en") "$percent% of goal" else "$percent% от нормы",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextTertiary
            )
            Text(
                text = if (appLanguage == "en") "30 ml / kg body weight" else "30 мл / кг веса",
                fontSize = 11.sp,
                color = TextTertiary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Logging Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // +250 ml (Glass)
            WaterActionButton(
                label = if (appLanguage == "en") "+250 ml" else "+250 мл",
                subtitle = if (appLanguage == "en") "Glass" else "Стакан",
                onClick = { onAddWater(250) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("water_add_250")
            )

            // +500 ml (Bottle)
            WaterActionButton(
                label = if (appLanguage == "en") "+500 ml" else "+500 мл",
                subtitle = if (appLanguage == "en") "Bottle" else "Бутылка",
                onClick = { onAddWater(500) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("water_add_500")
            )

            // Undo button (if water logged)
            if (currentWaterMl > 0) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkCardElevated)
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .clickable { onRemoveLastWater() }
                        .testTag("water_undo"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Отменить последнюю воду",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WaterActionButton(
    label: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = androidx.compose.ui.platform.LocalView.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(WaterAquaContainer)
            .border(1.dp, WaterAqua.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .clickable {
                com.example.ui.util.HapticUtil.performClick(view)
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = WaterAqua,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = WaterAqua
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = TextSecondary
            )
        }
    }
}
