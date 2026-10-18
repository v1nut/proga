package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.onFocusChanged
import androidx.core.content.ContextCompat
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Warning
import kotlinx.coroutines.launch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.calculator.KbzhuCalculator
import com.example.data.manager.HealthStepData
import com.example.data.model.KbzhuBackupData
import com.example.data.model.UserProfile
import com.example.notification.InactivityReminderManager
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.OutlineStrong
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.WildWatermelon
import com.example.ui.theme.WildWatermelonContainer

@Composable
fun SettingsScreen(
    userProfile: UserProfile,
    healthStepData: HealthStepData = HealthStepData(),
    onRequestHealthPermissions: () -> Unit = {},
    onSyncHealthConnect: ((String) -> Unit) -> Unit = { _ -> },
    onSaveWaterGoal: (isManual: Boolean, customTargetMl: Int) -> Unit = { _, _ -> },
    onExportBackup: (Uri, (Boolean, String) -> Unit) -> Unit = { _, _ -> },
    onImportBackup: suspend (Uri) -> Pair<KbzhuBackupData?, String?> = { _ -> Pair(null, null) },
    onConfirmRestoreBackup: (KbzhuBackupData, (Boolean, String) -> Unit) -> Unit = { _, _ -> },
    onSetLanguage: (String) -> Unit = {},
    onSetThemeMode: (String) -> Unit = {},
    tierMode: String = "auto",
    onSetTierMode: (String) -> Unit = {},
    onOpenChangelog: () -> Unit = {},
    onNavigateToProfile: () -> Unit,
    onNavigateToCatalog: () -> Unit = {},
    appLanguage: String = "ru",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember {
        context.getSharedPreferences(InactivityReminderManager.PREFS_NAME, Context.MODE_PRIVATE)
    }

    var notificationsEnabled by remember {
        mutableStateOf(prefs.getBoolean(InactivityReminderManager.KEY_NOTIFICATIONS_ENABLED, true))
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            notificationsEnabled = true
            prefs.edit().putBoolean(InactivityReminderManager.KEY_NOTIFICATIONS_ENABLED, true).apply()
            InactivityReminderManager.scheduleReminder(context)
        } else {
            notificationsEnabled = false
            prefs.edit().putBoolean(InactivityReminderManager.KEY_NOTIFICATIONS_ENABLED, false).apply()
            InactivityReminderManager.cancelReminder(context)
            Toast.makeText(
                context,
                if (appLanguage == "en") "Notification permission is required to enable reminders" else "Предоставьте разрешение на уведомления в настройках устройства",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    var reminderIntervalHours by remember {
        mutableIntStateOf(prefs.getInt(InactivityReminderManager.KEY_INTERVAL_HOURS, 4))
    }
    var autoSaveAiProducts by remember {
        mutableStateOf(prefs.getBoolean("auto_save_ai_products", false))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .testTag("settings_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(WildWatermelonContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = WildWatermelon,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = if (appLanguage == "en") "App Settings" else "Настройки приложения",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = if (appLanguage == "en") "Normalization parameters, Health Connect & Backup" else "Параметры нормализации, Health Connect и бэкап",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 1: Health Connect Synchronization
        SettingsSectionCard(title = if (appLanguage == "en") "Sync (Health Connect)" else "Синхронизация (Health Connect)", icon = Icons.Default.DirectionsRun) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (appLanguage == "en") "Source: ${healthStepData.dataSource}" else "Источник: ${healthStepData.dataSource}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (appLanguage == "en") "Last Sync: ${healthStepData.lastSyncTime}" else "Последняя синхронизация: ${healthStepData.lastSyncTime}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkCardElevated)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FitStatPill(if (appLanguage == "en") "Steps" else "Шаги", "${healthStepData.stepsCount}")
                FitStatPill(if (appLanguage == "en") "Burned" else "Сожжено", if (appLanguage == "en") "${healthStepData.burnedCalories} kcal" else "${healthStepData.burnedCalories} ккал")
                FitStatPill(if (appLanguage == "en") "Distance" else "Расстояние", if (appLanguage == "en") "${healthStepData.distanceKm} km" else "${healthStepData.distanceKm} км")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (healthStepData.isAvailable && !healthStepData.hasPermissions) {
                        onRequestHealthPermissions()
                    } else {
                        onSyncHealthConnect { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WildWatermelonContainer,
                    contentColor = WildWatermelon
                ),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (healthStepData.isAvailable && !healthStepData.hasPermissions) {
                        if (appLanguage == "en") "Grant Permissions" else "Предоставить разрешения"
                    } else {
                        if (appLanguage == "en") "Sync Now" else "Синхронизировать сейчас"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 2: Water Target Calculation (Auto vs Manual)
        var isManualWater by remember {
            mutableStateOf(prefs.getBoolean("is_manual_water_target", false))
        }
        var customWaterText by remember {
            mutableStateOf(prefs.getInt("custom_target_water_ml", 2000).toString())
        }

        val calculatedAutoWater = KbzhuCalculator.calculateTargetWaterMl(userProfile.weightKg, false, 2000)
        val customWaterVal = customWaterText.toIntOrNull() ?: 2000
        val isCustomWaterValid = customWaterVal in 500..6000

        SettingsSectionCard(title = if (appLanguage == "en") "Water Goal" else "Норма воды", icon = Icons.Default.WaterDrop) {
            Text(
                text = if (appLanguage == "en") "Daily water intake target calculation method:" else "Способ расчёта суточной цели потребления воды:",
                fontSize = 11.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    false to (if (appLanguage == "en") "Auto (${calculatedAutoWater} ml)" else "Авто (${calculatedAutoWater} мл)"),
                    true to (if (appLanguage == "en") "Manual" else "Вручную")
                ).forEach { (isManual, label) ->
                    val isSelected = isManualWater == isManual
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) WildWatermelon else DarkCardElevated)
                            .border(1.dp, if (isSelected) WildWatermelon else DarkBorder, RoundedCornerShape(12.dp))
                            .selectable(
                                selected = isSelected,
                                role = Role.RadioButton,
                                onClick = {
                                    isManualWater = isManual
                                    onSaveWaterGoal(isManual, customWaterVal)
                                }
                            )
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF101014) else TextPrimary
                        )
                    }
                }
            }

            if (isManualWater) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = customWaterText,
                    onValueChange = { input ->
                        val clean = input.filter { it.isDigit() }
                        customWaterText = clean
                        val parsed = clean.toIntOrNull() ?: 0
                        if (parsed in 500..6000) {
                            onSaveWaterGoal(true, parsed)
                        }
                    },
                    label = { Text(if (appLanguage == "en") "Manual Water Goal (ml)" else "Ручная норма воды (мл)") },
                    singleLine = true,
                    isError = !isCustomWaterValid,
                    supportingText = {
                        if (!isCustomWaterValid) {
                            Text(if (appLanguage == "en") "Enter value from 500 to 6000 ml" else "Введите норму от 500 до 6000 мл", color = Color(0xFFF87171), fontSize = 11.sp)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WildWatermelon,
                        unfocusedBorderColor = OutlineStrong,
                        errorBorderColor = Color(0xFFF87171),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 3: Custom Products & AI Auto-Save
        SettingsSectionCard(title = if (appLanguage == "en") "Catalog & Custom Foods" else "Каталог и свои продукты", icon = Icons.Default.AutoAwesome) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = if (appLanguage == "en") "Save AI Foods" else "Сохранять ИИ-продукты",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (appLanguage == "en") "Automatically add recognized dishes to My Foods" else "Автоматически добавлять распознанные блюда в «Мои продукты»",
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

            Spacer(modifier = Modifier.height(12.dp))

            var customGeminiApiKey by remember {
                mutableStateOf(com.example.data.security.EncryptedPrefs.getGeminiApiKey(context))
            }

            OutlinedTextField(
                value = customGeminiApiKey,
                onValueChange = { input ->
                    customGeminiApiKey = input
                },
                keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    com.example.data.security.EncryptedPrefs.saveGeminiApiKey(context, customGeminiApiKey)
                }),
                label = { Text(if (appLanguage == "en") "Custom Gemini API Key (optional)" else "Ключ Gemini API (опционально)") },
                placeholder = { Text("AIzaSy...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            com.example.data.security.EncryptedPrefs.saveGeminiApiKey(context, customGeminiApiKey)
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WildWatermelon,
                    unfocusedBorderColor = OutlineStrong,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onNavigateToCatalog,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, WildWatermelon.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = WildWatermelon),
                modifier = Modifier.fillMaxWidth().height(42.dp)
            ) {
                Text(if (appLanguage == "en") "Manage Catalog" else "Перейти в управление каталогом", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 4: Notifications & Inactivity Reminders
        var showNotificationSettingsDialog by remember { mutableStateOf(false) }

        SettingsSectionCard(title = if (appLanguage == "en") "Reminders" else "Напоминания о питании и воде", icon = Icons.Default.NotificationsActive) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = if (appLanguage == "en") "Meal & Water Reminders" else "Напоминать о питании и воде",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (appLanguage == "en") "Reminder if inactive for more than $reminderIntervalHours hours" else "Напоминание, если данные не обновлялись более $reminderIntervalHours ${com.example.notification.InactivityReminderWorker.formatHoursWord(reminderIntervalHours)}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            val areEnabledInSystem = androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
                            if (!areEnabledInSystem || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    notificationsEnabled = false
                                    Toast.makeText(
                                        context,
                                        if (appLanguage == "en") "Notifications are disabled in system settings" else "Уведомления отключены в системных настройках устройства",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                notificationsEnabled = true
                                prefs.edit().putBoolean(InactivityReminderManager.KEY_NOTIFICATIONS_ENABLED, true).apply()
                                InactivityReminderManager.scheduleReminder(context)
                            }
                        } else {
                            notificationsEnabled = false
                            prefs.edit().putBoolean(InactivityReminderManager.KEY_NOTIFICATIONS_ENABLED, false).apply()
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
                    text = if (appLanguage == "en") "Inactivity Interval:" else "Интервал неактивности:",
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
                        val labelText = if (appLanguage == "en") "$hours hours" else (if (hours == 5) "5 часов" else "$hours часа")
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) WildWatermelon else DarkCardElevated)
                                .border(0.5.dp, if (isSelected) WildWatermelon else DarkBorder, RoundedCornerShape(10.dp))
                                .selectable(
                                    selected = isSelected,
                                    role = Role.RadioButton,
                                    onClick = {
                                        reminderIntervalHours = hours
                                        prefs.edit().putInt(InactivityReminderManager.KEY_INTERVAL_HOURS, hours).apply()
                                        InactivityReminderManager.scheduleReminder(context)
                                    }
                                )
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = labelText,
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
                        val isSent = InactivityReminderManager.triggerTestNotification(context)
                        if (isSent) {
                            Toast.makeText(context, if (appLanguage == "en") "Test reminder sent!" else "Тестовое напоминание отправлено!", Toast.LENGTH_SHORT).show()
                        } else {
                            showNotificationSettingsDialog = true
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WildWatermelonContainer,
                        contentColor = WildWatermelon
                    ),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Text(if (appLanguage == "en") "Test Reminder" else "Проверить напоминание", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                if (showNotificationSettingsDialog) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showNotificationSettingsDialog = false },
                        containerColor = DarkSurface,
                        title = { Text(if (appLanguage == "en") "Notifications Disabled" else "Уведомления отключены", fontWeight = FontWeight.Bold, color = TextPrimary) },
                        text = { Text(if (appLanguage == "en") "Notifications are disabled in device settings. Open settings?" else "В настройках устройства отключены уведомления для приложения. Открыть настройки?", color = TextSecondary, fontSize = 13.sp) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showNotificationSettingsDialog = false
                                    val intent = android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                        putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    }
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WildWatermelon, contentColor = Color(0xFF101014))
                            ) {
                                Text(if (appLanguage == "en") "Open Settings" else "Открыть настройки", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showNotificationSettingsDialog = false }) {
                                Text(if (appLanguage == "en") "Cancel" else "Отмена", color = TextTertiary)
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 5: Backup & Restore
        var pendingImportData by remember { mutableStateOf<KbzhuBackupData?>(null) }

        val exportLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("application/json")
        ) { uri: Uri? ->
            if (uri != null) {
                onExportBackup(uri) { success, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }

        val importLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->
            if (uri != null) {
                scope.launch {
                    val (parsed, error) = onImportBackup(uri)
                    if (parsed != null) {
                        pendingImportData = parsed
                    } else {
                        Toast.makeText(context, error ?: if (appLanguage == "en") "Invalid backup file" else "Некорректный файл резервной копии", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        SettingsSectionCard(title = if (appLanguage == "en") "Backup & Restore" else "Резервная копия", icon = Icons.Default.Save) {
            Text(
                text = if (appLanguage == "en") "Save all diary entries, water, weight, and profile data to a JSON file or restore on a new device." else "Сохраните всю историю дневника, воды, веса и профиля в JSON-файл или восстановите данные на новом устройстве.",
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val date = java.time.LocalDate.now().toString()
                        exportLauncher.launch("kbzhu-backup-$date.json")
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WildWatermelon,
                        contentColor = Color(0xFF101014)
                    ),
                    modifier = Modifier.weight(1f).height(44.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = if (appLanguage == "en") "Export" else "Экспорт",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = {
                        importLauncher.launch(arrayOf("application/json", "*/*"))
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkCardElevated,
                        contentColor = TextPrimary
                    ),
                    modifier = Modifier.weight(1f).height(44.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = if (appLanguage == "en") "Import" else "Импорт",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Confirmation Dialog
        if (pendingImportData != null) {
            val data = pendingImportData!!
            val diaryCount = data.safeMealEntries.size
            val productCount = data.safeProducts.size
            val savedMealCount = data.safeSavedMeals.size
            val waterCount = data.safeWaterEntries.size
            val weightCount = data.safeWeightEntries.size

            androidx.compose.material3.AlertDialog(
                onDismissRequest = { pendingImportData = null },
                title = {
                    Text(
                        text = if (appLanguage == "en") "Confirm Data Import" else "Подтвердите импорт данных",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2A1517))
                                .border(1.dp, Color(0xFFF87171), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFF87171),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (appLanguage == "en")
                                        "WARNING: All current diary, products, and settings WILL BE ERASED and replaced!"
                                    else
                                        "ВНИМАНИЕ: Все текущие данные приложения БУДУТ УДАЛЕНЫ и заменены данными из файла!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF87171),
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (appLanguage == "en") "Backup details (${data.exportDate}):" else "Содержимое резервной копии (${data.exportDate}):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkCardElevated)
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = if (appLanguage == "en") "• Diary records: $diaryCount" else "• Записей в дневнике: $diaryCount", fontSize = 12.sp, color = TextSecondary)
                            Text(text = if (appLanguage == "en") "• Products in catalog: $productCount" else "• Продуктов в каталоге: $productCount", fontSize = 12.sp, color = TextSecondary)
                            Text(text = if (appLanguage == "en") "• Saved recipes: $savedMealCount" else "• Моих блюд (рецептов): $savedMealCount", fontSize = 12.sp, color = TextSecondary)
                            Text(text = if (appLanguage == "en") "• Water records: $waterCount" else "• Записей воды: $waterCount", fontSize = 12.sp, color = TextSecondary)
                            Text(text = if (appLanguage == "en") "• Weight records: $weightCount" else "• Записей веса: $weightCount", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onConfirmRestoreBackup(data) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                            pendingImportData = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF87171),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (appLanguage == "en") "Replace Data" else "Удалить и заменить", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingImportData = null }) {
                        Text(if (appLanguage == "en") "Cancel" else "Отмена", color = TextTertiary)
                    }
                },
                containerColor = DarkSurface
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 5.5: Food Tier List Mode (S-D)
        var currentTierMode by remember(tierMode) { mutableStateOf(tierMode) }

        SettingsSectionCard(title = if (appLanguage == "en") "Food Tier System (S–D)" else "Режим Тир-листа продуктов (S–D)", icon = Icons.Default.AutoAwesome) {
            Text(
                text = if (appLanguage == "en") "Adapt food nutrition tiers (S, A, B, C, D) to your current fitness goal:" else "Адаптировать ранжирование пользы продуктов под текущую цель:",
                fontSize = 11.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    com.example.ui.util.TierMode.AUTO_BY_PROFILE,
                    com.example.ui.util.TierMode.WEIGHT_LOSS,
                    com.example.ui.util.TierMode.WEIGHT_GAIN
                ).forEach { mode ->
                    val isSelected = currentTierMode == mode.key
                    val labelText = if (appLanguage == "en") mode.titleEn else mode.titleRu

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) WildWatermelon else DarkCardElevated)
                            .border(1.dp, if (isSelected) WildWatermelon else DarkBorder, RoundedCornerShape(12.dp))
                            .selectable(
                                selected = isSelected,
                                role = Role.RadioButton,
                                onClick = {
                                    currentTierMode = mode.key
                                    onSetTierMode(mode.key)
                                }
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = labelText,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF101014) else TextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 6: App Language
        var currentLang by remember {
            mutableStateOf(prefs.getString("app_language", "ru") ?: "ru")
        }

        SettingsSectionCard(title = if (currentLang == "en") "Language" else "Язык интерфейса", icon = Icons.Default.Language) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("ru" to "Русский (RU)", "en" to "English (EN)").forEach { (code, label) ->
                    val isSelected = currentLang == code
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) WildWatermelon else DarkCardElevated)
                            .border(1.dp, if (isSelected) WildWatermelon else DarkBorder, RoundedCornerShape(12.dp))
                            .selectable(
                                selected = isSelected,
                                role = Role.RadioButton,
                                onClick = {
                                    currentLang = code
                                    onSetLanguage(code)
                                    Toast.makeText(
                                        context,
                                        if (code == "ru") "Язык изменён на Русский" else "Language set to English",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF101014) else TextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 7: App Theme Selection
        var currentThemeMode by remember {
            mutableStateOf(prefs.getString("app_theme_mode", "dark") ?: "dark")
        }

        SettingsSectionCard(title = if (currentLang == "en") "App Theme" else "Тема оформления", icon = Icons.Default.ColorLens) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "dark" to if (currentLang == "en") "Dark" else "Тёмная",
                    "light" to if (currentLang == "en") "Light" else "Светлая",
                    "system" to if (currentLang == "en") "System" else "Системная"
                ).forEach { (mode, label) ->
                    val isSelected = currentThemeMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) WildWatermelon else DarkCardElevated)
                            .border(1.dp, if (isSelected) WildWatermelon else DarkBorder, RoundedCornerShape(12.dp))
                            .selectable(
                                selected = isSelected,
                                role = Role.RadioButton,
                                onClick = {
                                    currentThemeMode = mode
                                    prefs.edit().putString("app_theme_mode", mode).apply()
                                    onSetThemeMode(mode)
                                }
                            )
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFF101014) else TextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 8: About & App Version
        val appVersion = remember {
            try {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                pInfo.versionName ?: com.example.BuildConfig.VERSION_NAME
            } catch (e: Exception) {
                com.example.BuildConfig.VERSION_NAME
            }
        }

        SettingsSectionCard(title = if (currentLang == "en") "About App" else "О приложении", icon = Icons.Default.Info) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (currentLang == "en") "App Version" else "Версия приложения",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = "v$appVersion",
                    fontSize = 12.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onOpenChangelog,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, WildWatermelon.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().height(44.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = WildWatermelon,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (currentLang == "en") "What's new in v$appVersion" else "Что нового в v$appVersion",
                    fontSize = 12.sp,
                    color = WildWatermelon,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
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
