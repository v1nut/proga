package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WildWatermelon
import com.example.ui.theme.WildWatermelonContainer

@Composable
fun GoogleAccountSelectDialog(
    defaultEmail: String = "chistochilling@gmail.com",
    onDismiss: () -> Unit,
    onAccountSelected: (email: String, displayName: String) -> Unit
) {
    var selectedEmail by remember { mutableStateOf(if (defaultEmail.isBlank()) "chistochilling@gmail.com" else defaultEmail) }
    var isCustomInput by remember { mutableStateOf(false) }
    var customEmailInput by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(24.dp))
                .testTag("google_account_select_dialog"),
            color = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(WildWatermelonContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("G", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WildWatermelon)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Выберите аккаунт Google",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Синхронизация Google Fit и данных",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Выберите из найденных аккаунтов Google на устройстве:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Account Option 1: Primary Google Account
                val primaryAccountEmail = if (defaultEmail.isNotBlank()) defaultEmail else "chistochilling@gmail.com"
                val isPrimarySelected = !isCustomInput && selectedEmail == primaryAccountEmail

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isPrimarySelected) WildWatermelonContainer.copy(alpha = 0.25f) else DarkCardElevated)
                        .border(1.dp, if (isPrimarySelected) WildWatermelon else DarkBorder, RoundedCornerShape(14.dp))
                        .clickable {
                            isCustomInput = false
                            selectedEmail = primaryAccountEmail
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(WildWatermelon),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = primaryAccountEmail.take(1).uppercase(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = primaryAccountEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = primaryAccountEmail,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    if (isPrimarySelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Выбрано",
                            tint = WildWatermelon,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Account Option 2: Enter custom email
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isCustomInput) WildWatermelonContainer.copy(alpha = 0.25f) else DarkCardElevated)
                        .border(1.dp, if (isCustomInput) WildWatermelon else DarkBorder, RoundedCornerShape(14.dp))
                        .clickable { isCustomInput = true }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = if (isCustomInput) WildWatermelon else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Войти под другим e-mail Google",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }

                if (isCustomInput) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customEmailInput,
                        onValueChange = { customEmailInput = it },
                        label = { Text("Введите e-mail Google") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WildWatermelon,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Button
                Button(
                    onClick = {
                        val emailToUse = if (isCustomInput && customEmailInput.contains("@")) {
                            customEmailInput.trim()
                        } else if (isCustomInput) {
                            primaryAccountEmail
                        } else {
                            selectedEmail
                        }
                        val nameToUse = emailToUse.substringBefore("@").replaceFirstChar { it.uppercase() }
                        onAccountSelected(emailToUse, nameToUse)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WildWatermelon,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Подтвердить вход в Google",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
