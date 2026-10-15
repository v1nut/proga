package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.manager.AuthType
import com.example.data.manager.GoogleFitActivityData
import com.example.data.manager.UserAuthState
import com.example.data.model.UserProfile
import com.example.notification.InactivityReminderManager
import com.example.ui.components.GoogleAccountSelectDialog
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.WaterAqua
import com.example.ui.theme.WildWatermelon
import com.example.ui.theme.WildWatermelonContainer

@Composable
fun SettingsScreen(
    userProfile: UserProfile,
    googleFitData: GoogleFitActivityData = GoogleFitActivityData(),
    userAuthState: UserAuthState = UserAuthState(),
    onSyncGoogleFit: () -> Unit = {},
    onToggleGoogleFitAutoSync: (Boolean) -> Unit = {},
    onLoginWithGoogle: (email: String) -> Unit = {},
    onLoginWithEmail: (email: String) -> Unit = {},
    onLogoutUser: () -> Unit = {},
    onNavigateToProfile: () -> Unit,
    onNavigateToCatalog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences(InactivityReminderManager.PREFS_NAME, Context.MODE_PRIVATE)
    }

    var notificationsEnabled by remember {
        mutableStateOf(prefs.getBoolean(InactivityReminderManager.KEY_NOTIFICATIONS_ENABLED, true))
    }
    var reminderIntervalHours by remember {
        mutableIntStateOf(prefs.getInt(InactivityReminderManager.KEY_INTERVAL_HOURS, 4))
    }
    var autoWaterCalculation by remember {
        mutableStateOf(prefs.getBoolean("auto_water_calc", true))
    }
    var autoSaveAiProducts by remember {
        mutableStateOf(prefs.getBoolean("auto_save_ai_products", true))
    }
    var testNotificationSent by remember { mutableStateOf(false) }

    // Google Account picker & Email login form state
    var showGoogleAccountDialog by remember { mutableStateOf(false) }
    var showEmailForm by remember { mutableStateOf(false) }
    var inputEmail by remember { mutableStateOf("") }
    var inputPassword by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
            .testTag("settings_screen")
    ) {
        // Screen Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(WildWatermelonContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = WildWatermelon,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Настройки",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Аккаунт, Google Fit, ИИ и параметры",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 1: Account / Auth (Google & Email)
        SettingsSectionCard(title = "Учетная запись (Google & Email)", icon = Icons.Default.AccountCircle) {
            if (userAuthState.isLoggedIn) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkCardElevated)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(WildWatermelon),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userAuthState.displayName.take(1).uppercase(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userAuthState.displayName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(WildWatermelonContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (userAuthState.authType == AuthType.GOOGLE) "Google" else "Email",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WildWatermelon
                                )
                            }
                        }
                        Text(
                            text = userAuthState.email,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    TextButton(
                        onClick = {
                            onLogoutUser()
                            Toast.makeText(context, "Вышли из аккаунта", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Выйти", fontSize = 12.sp, color = WildWatermelon, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Text(
                    text = "Войдите через Google или по e-mail для синхронизации всех данных между устройствами.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            showGoogleAccountDialog = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WildWatermelon,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Вход Google", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showEmailForm = !showEmailForm },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkCardElevated,
                            contentColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Вход по Email", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                AnimatedVisibility(visible = showEmailForm) {
                    Column(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkCardElevated)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = inputEmail,
                            onValueChange = { inputEmail = it },
                            label = { Text("Электронная почта") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextSecondary) },
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

                        OutlinedTextField(
                            value = inputPassword,
                            onValueChange = { inputPassword = it },
                            label = { Text("Пароль") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondary) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = WildWatermelon,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (inputEmail.contains("@")) {
                                    onLoginWithEmail(inputEmail)
                                    showEmailForm = false
                                    Toast.makeText(context, "Вход с $inputEmail выполнен!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Введите корректный email", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WildWatermelon),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Войти / Зарегистрироваться", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 2: Google Fit & Health Connect Integration
        SettingsSectionCard(title = "Google Fit & Health Connect", icon = Icons.Default.DirectionsRun) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Автосинхронизация активности",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1E3A2B))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Подключено", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4EFA8A))
                        }
                    }
                    Text(
                        text = "Автоматический импорт сожженных калорий и шагов из Google Fit",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = googleFitData.isAutoSyncEnabled,
                    onCheckedChange = { isChecked ->
                        onToggleGoogleFitAutoSync(isChecked)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF101014),
                        checkedTrackColor = WildWatermelon,
                        uncheckedThumbColor = TextTertiary,
                        uncheckedTrackColor = DarkCardElevated
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Google Fit Live Sync Metrics Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkCardElevated)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FitStatPill("Шаги", "${googleFitData.stepsCount}")
                FitStatPill("Сожжено", "${googleFitData.burnedCalories} ккал")
                FitStatPill("Активность", "${googleFitData.activeMinutes} мин")
                FitStatPill("Дистанция", "${googleFitData.distanceKm} км")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    onSyncGoogleFit()
                    Toast.makeText(context, "Google Fit: Синхронизировано ${googleFitData.stepsCount} шагов (${googleFitData.burnedCalories} ккал)!", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WildWatermelonContainer,
                    contentColor = WildWatermelon
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Синхронизировать данные прямо сейчас", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 3: Custom Products & AI Auto-Save
        SettingsSectionCard(title = "Каталог и свои продукты", icon = Icons.Default.AutoAwesome) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Сохранять ИИ-продукты",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Блюда из ИИ-поиска автоматически добавляются в «Свои продукты» на будущее",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = autoSaveAiProducts,
                    onCheckedChange = { isChecked ->
                        autoSaveAiProducts = isChecked
                        prefs.edit().putBoolean("auto_save_ai_products", isChecked).apply()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF101014),
                        checkedTrackColor = WildWatermelon,
                        uncheckedThumbColor = TextTertiary,
                        uncheckedTrackColor = DarkCardElevated
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onNavigateToCatalog,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkCardElevated,
                    contentColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Перейти в каталог и управление своими продуктами", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 4: Push Notifications & Reminders
        SettingsSectionCard(title = "Push-уведомления и напоминания", icon = Icons.Default.Notifications) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Напоминать о питании и воде",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Push-сообщение, если данные не обновлялись более $reminderIntervalHours часов",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { isChecked ->
                        notificationsEnabled = isChecked
                        prefs.edit().putBoolean(InactivityReminderManager.KEY_NOTIFICATIONS_ENABLED, isChecked).apply()
                        if (isChecked) {
                            InactivityReminderManager.scheduleReminder(context)
                        } else {
                            InactivityReminderManager.cancelReminder(context)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF101014),
                        checkedTrackColor = WildWatermelon,
                        uncheckedThumbColor = TextTertiary,
                        uncheckedTrackColor = DarkCardElevated
                    ),
                    modifier = Modifier.testTag("notification_switch")
                )
            }

            if (notificationsEnabled) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Интервал неактивности:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(2, 3, 4, 5).forEach { hours ->
                        val isSelected = reminderIntervalHours == hours
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) WildWatermelon else DarkCardElevated)
                                .border(0.5.dp, if (isSelected) WildWatermelon else DarkBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    reminderIntervalHours = hours
                                    prefs.edit().putInt(InactivityReminderManager.KEY_INTERVAL_HOURS, hours).apply()
                                    InactivityReminderManager.scheduleReminder(context)
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$hours часа",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF101014) else TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        InactivityReminderManager.triggerTestNotification(context)
                        testNotificationSent = true
                        Toast.makeText(context, "Тестовое push-уведомление отправлено!", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkCardElevated,
                        contentColor = WildWatermelon
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("test_notification_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (testNotificationSent) "Уведомление отправлено ✓" else "Проверить Push-уведомление",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 5: Water & Profile
        SettingsSectionCard(title = "Водный баланс и профиль", icon = Icons.Default.WaterDrop) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Авторасчёт суточной нормы воды",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "30 мл на 1 кг веса (${(userProfile.weightKg * 30).toInt()} мл для ${userProfile.weightKg.toInt()} кг)",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = autoWaterCalculation,
                    onCheckedChange = { isChecked ->
                        autoWaterCalculation = isChecked
                        prefs.edit().putBoolean("auto_water_calc", isChecked).apply()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF101014),
                        checkedTrackColor = WaterAqua,
                        uncheckedThumbColor = TextTertiary,
                        uncheckedTrackColor = DarkCardElevated
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onNavigateToProfile,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkCardElevated,
                    contentColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Изменить вес, рост и норму КБЖУ в профиле", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 6: Theme & App Info
        SettingsSectionCard(title = "Тема и версия", icon = Icons.Default.ColorLens) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(WildWatermelon)
                        .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Дикий арбуз Крайола (#FC6C85)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Фирменная цветовая палитра и тёмная премиальная тема",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Версия приложения", fontSize = 12.sp, color = TextSecondary)
                Text(text = "1.3.0 (Google Fit + AI Sync)", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showGoogleAccountDialog) {
        GoogleAccountSelectDialog(
            defaultEmail = if (userAuthState.email.isNotBlank()) userAuthState.email else "chistochilling@gmail.com",
            onDismiss = { showGoogleAccountDialog = false },
            onAccountSelected = { email, name ->
                onLoginWithGoogle(email)
                onSyncGoogleFit()
                showGoogleAccountDialog = false
                Toast.makeText(context, "Успешный вход! Аккаунт $email привязан к Google Fit", Toast.LENGTH_LONG).show()
            }
        )
    }
}

@Composable
private fun FitStatPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = TextSecondary)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WildWatermelon)
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkCard)
            .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = WildWatermelon,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        content()
    }
}
