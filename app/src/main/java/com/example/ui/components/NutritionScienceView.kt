package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.Product
import com.example.data.service.GeminiFoodService
import com.example.data.service.ScienceNutritionAdvice
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.FatColor
import com.example.ui.theme.ProteinColor
import com.example.ui.theme.SuccessColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WildWatermelon
import com.example.ui.theme.WildWatermelonContainer

@Composable
fun NutritionScienceView(
    product: Product,
    modifier: Modifier = Modifier
) {
    val geminiService = remember { GeminiFoodService() }
    var advice by remember { mutableStateOf<ScienceNutritionAdvice?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(product.id) {
        isLoading = true
        val result = geminiService.getNutritionalScienceAdvice(product)
        result.onSuccess {
            advice = it
            isLoading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(DarkCard)
            .border(1.dp, WildWatermelon.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(WildWatermelonContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MedicalServices,
                    contentDescription = null,
                    tint = WildWatermelon,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Научный анализ доказательной медицины",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = WildWatermelon
                )
                Text(
                    text = "Данные исследований ВОЗ, PubMed и EFSA",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = WildWatermelon,
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ИИ сопоставляет медицинские мета-анализы...",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        } else if (advice != null) {
            AnimatedVisibility(visible = true, enter = fadeIn()) {
                val data = advice!!
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Summary Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkCardElevated)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = data.summary,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            lineHeight = 16.sp
                        )
                    }

                    // Key Health Benefits
                    Text(
                        text = "Ключевая польза для организма:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WildWatermelon
                    )

                    data.healthBenefits.forEach { benefit ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessColor,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 1.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = benefit,
                                fontSize = 11.sp,
                                color = TextPrimary,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    // Clinical Evidence / Studies
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ProteinColor.copy(alpha = 0.1f))
                            .border(0.5.dp, ProteinColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = ProteinColor,
                                modifier = Modifier.size(16.dp).padding(top = 1.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Клинические данные:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ProteinColor
                                )
                                Text(
                                    text = data.scientificEvidence,
                                    fontSize = 11.sp,
                                    color = TextPrimary,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    // Norm & Glycemic impact row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkCardElevated)
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("Суточная норма:", fontSize = 10.sp, color = TextSecondary)
                                Text(data.recommendedIntake, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkCardElevated)
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("Гликемический ответ:", fontSize = 10.sp, color = TextSecondary)
                                Text(data.glycemicImpact, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = FatColor)
                            }
                        }
                    }

                    // Cautions / Warnings
                    if (data.cautions.isNotBlank() && data.cautions != "Без выраженных противопоказаний") {
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(FatColor.copy(alpha = 0.1f))
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = FatColor,
                                modifier = Modifier.size(14.dp).padding(top = 1.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Предостережения: ${data.cautions}",
                                fontSize = 10.sp,
                                color = TextPrimary,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
