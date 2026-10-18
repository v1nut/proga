package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.Product
import com.example.data.service.ProductScienceInfo
import com.example.ui.theme.CarbsColor
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.FatColor
import com.example.ui.theme.ProteinColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WildWatermelon
import com.example.ui.theme.WildWatermelonContainer
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductInfoBottomSheet(
    product: Product,
    onFetchInfo: (Product, (Result<ProductScienceInfo>) -> Unit) -> Unit,
    onDismiss: () -> Unit,
    appLanguage: String = "ru"
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var info by remember { mutableStateOf<ProductScienceInfo?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun loadData() {
        isLoading = true
        errorMessage = null
        onFetchInfo(product) { result ->
            result.onSuccess {
                info = it
                isLoading = false
            }.onFailure {
                errorMessage = if (appLanguage == "en") "Info unavailable, try again later" else "Справка недоступна, попробуйте позже"
                isLoading = false
            }
        }
    }

    LaunchedEffect(product.id) {
        loadData()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(WildWatermelonContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = WildWatermelon,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (appLanguage == "en") "Product Science Info (AI)" else "Справка о продукте (ИИ)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (appLanguage == "en") "AI generated reference info. Not a medical advice" else "Сгенерировано ИИ, может содержать ошибки. Не является медицинской рекомендацией",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Закрыть", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Loading state
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkCard),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = WildWatermelon, strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (appLanguage == "en") "Loading science reference..." else "Генерация справочной информации...",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            } else if (errorMessage != null) {
                // Error state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF2A1517))
                        .border(1.dp, Color(0xFFF87171), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFF87171),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage ?: if (appLanguage == "en") "Info unavailable" else "Справка недоступна, попробуйте позже",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF87171)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { loadData() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WildWatermelon,
                            contentColor = Color(0xFF101014)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (appLanguage == "en") "Retry" else "Повторить", fontWeight = FontWeight.Bold)
                    }
                }
            } else if (info != null) {
                val data = info!!
                // Content Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkCard)
                        .border(1.dp, DarkBorder, RoundedCornerShape(18.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = data.productName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = WildWatermelon
                    )

                    Text(
                        text = data.summary,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 18.sp
                    )

                    if (data.healthBenefits.isNotEmpty()) {
                        Column {
                            Text(if (appLanguage == "en") "Nutritional Benefits:" else "Питательные свойства:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            data.healthBenefits.forEach { item ->
                                Row(
                                    modifier = Modifier.padding(bottom = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = ProteinColor,
                                        modifier = Modifier.size(15.dp).padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = item, fontSize = 12.sp, color = TextPrimary, lineHeight = 16.sp)
                                }
                            }
                        }
                    }

                    if (data.generalInfo.isNotBlank()) {
                        Column {
                            Text(if (appLanguage == "en") "General Info:" else "Общие сведения:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = data.generalInfo, fontSize = 12.sp, color = TextPrimary, lineHeight = 17.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkCardElevated)
                                .padding(10.dp)
                        ) {
                            Text(if (appLanguage == "en") "Glycemic Impact" else "Гликемический ответ", fontSize = 10.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = data.glycemicImpact, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FatColor)
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkCardElevated)
                                .padding(10.dp)
                        ) {
                            Text(if (appLanguage == "en") "Recommended Intake" else "Ориентир порции", fontSize = 10.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = data.recommendedIntake, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CarbsColor)
                        }
                    }

                    if (data.cautions.isNotBlank()) {
                        Column {
                            Text(if (appLanguage == "en") "Cautions & Notes:" else "Особенности применения:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = data.cautions, fontSize = 12.sp, color = TextSecondary, lineHeight = 16.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
