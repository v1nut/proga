package com.example.data.manager

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class HealthStepData(
    val dateString: String = LocalDate.now().toString(),
    val isAvailable: Boolean = true,
    val hasPermissions: Boolean = false,
    val stepsCount: Int = 0,
    val burnedCalories: Int = 0,
    val activeMinutes: Int = 0,
    val distanceKm: Double = 0.0,
    val dataSource: String = "Аппаратный шагомер",
    val lastSyncTime: String = "Не синхронизировано"
)

class HealthConnectManager(private val context: Context) : SensorEventListener {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val managerJob = Job()
    private val scope = CoroutineScope(Dispatchers.IO + managerJob)

    fun getSdkStatus(): Int {
        return try {
            HealthConnectClient.getSdkStatus(context)
        } catch (e: Exception) {
            HealthConnectClient.SDK_UNAVAILABLE
        }
    }

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class)
    )

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val stepCounterSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private val _stepData = MutableStateFlow(loadStepData())
    val stepData: StateFlow<HealthStepData> = _stepData.asStateFlow()

    init {
        registerStepSensor()
        checkPermissionsAndSync()
    }

    fun cleanup() {
        unregisterStepSensor()
        scope.cancel()
    }

    fun registerStepSensor() {
        // Safe Activity Recognition check for Android 10+ (API 29+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }
        stepCounterSensor?.let { sensor ->
            try {
                sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun unregisterStepSensor() {
        try {
            sensorManager?.unregisterListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val rawSteps = event?.values?.firstOrNull()?.toInt() ?: return
        val today = LocalDate.now().toString()
        checkAndResetNewDay(today)

        val lastSensorSteps = prefs.getInt(KEY_BASE_SENSOR_STEPS, -1)
        val stepDelta = if (lastSensorSteps < 0 || rawSteps < lastSensorSteps) {
            0
        } else {
            rawSteps - lastSensorSteps
        }
        prefs.edit().putInt(KEY_BASE_SENSOR_STEPS, rawSteps).apply()

        val currentSensorSteps = prefs.getInt(KEY_SENSOR_STEPS, 0)
        val newSensorTotal = currentSensorSteps + stepDelta
        prefs.edit().putInt(KEY_SENSOR_STEPS, newSensorTotal).apply()

        // Only update display data if Health Connect is NOT the primary active source
        if (_stepData.value.dataSource != "Health Connect") {
            updateStepDisplay(newSensorTotal, "Аппаратный шагомер")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun checkPermissionsAndSync() {
        val status = getSdkStatus()
        if (status != HealthConnectClient.SDK_AVAILABLE) {
            _stepData.value = _stepData.value.copy(isAvailable = false, hasPermissions = false)
            return
        }

        scope.launch {
            try {
                val client = HealthConnectClient.getOrCreate(context)
                val granted = client.permissionController.getGrantedPermissions()
                val hasPerm = granted.containsAll(permissions)
                withContext(Dispatchers.Main) {
                    _stepData.value = _stepData.value.copy(
                        isAvailable = true,
                        hasPermissions = hasPerm
                    )
                }
                if (hasPerm) {
                    readHealthConnectSteps(client)
                } else {
                    fallbackToHardwareData()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _stepData.value = _stepData.value.copy(isAvailable = false)
                    fallbackToHardwareData()
                }
            }
        }
    }

    suspend fun readHealthConnectSteps(client: HealthConnectClient? = null) {
        val targetClient = client ?: try {
            if (getSdkStatus() == HealthConnectClient.SDK_AVAILABLE) HealthConnectClient.getOrCreate(context) else null
        } catch (e: Exception) {
            null
        } ?: run {
            fallbackToHardwareData()
            return
        }

        try {
            val startTime = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
            val endTime = Instant.now()
            val response = targetClient.aggregate(
                androidx.health.connect.client.request.AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            val totalSteps = response[StepsRecord.COUNT_TOTAL]?.toInt() ?: 0
            withContext(Dispatchers.Main) {
                if (totalSteps > 0) {
                    prefs.edit().putInt(KEY_HEALTH_CONNECT_STEPS, totalSteps).apply()
                    updateStepDisplay(totalSteps, "Health Connect")
                } else {
                    val sensorSteps = prefs.getInt(KEY_SENSOR_STEPS, 0)
                    if (sensorSteps > 0) {
                        updateStepDisplay(sensorSteps, "Аппаратный шагомер")
                    } else {
                        updateStepDisplay(0, "Health Connect (0 шагов)")
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                fallbackToHardwareData()
            }
        }
    }

    fun syncNow(onResult: (String) -> Unit = {}) {
        val status = getSdkStatus()
        if (status == HealthConnectClient.SDK_UNAVAILABLE) {
            onResult("Health Connect недоступен на этом устройстве")
            _stepData.value = _stepData.value.copy(isAvailable = false)
            fallbackToHardwareData()
            return
        }
        if (status == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
            onResult("Требуется обновление приложения Health Connect")
            _stepData.value = _stepData.value.copy(isAvailable = false)
            fallbackToHardwareData()
            return
        }

        scope.launch {
            try {
                val client = HealthConnectClient.getOrCreate(context)
                val granted = client.permissionController.getGrantedPermissions()
                if (granted.containsAll(permissions)) {
                    readHealthConnectSteps(client)
                    withContext(Dispatchers.Main) {
                        onResult("Синхронизация с Health Connect выполнена")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        checkPermissionsAndSync()
                        onResult("Предоставьте разрешение Health Connect")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    fallbackToHardwareData()
                    onResult("Ошибка обращения к Health Connect")
                }
            }
        }
    }

    private fun fallbackToHardwareData() {
        val today = LocalDate.now().toString()
        checkAndResetNewDay(today)

        val sensorSteps = prefs.getInt(KEY_SENSOR_STEPS, 0)
        updateStepDisplay(sensorSteps, "Аппаратный шагомер")
    }

    private fun checkAndResetNewDay(today: String) {
        val savedDate = prefs.getString(KEY_STEPS_DATE, "")
        if (savedDate != today) {
            prefs.edit()
                .putString(KEY_STEPS_DATE, today)
                .putInt(KEY_BASE_SENSOR_STEPS, -1)
                .putInt(KEY_SENSOR_STEPS, 0)
                .putInt(KEY_HEALTH_CONNECT_STEPS, 0)
                .putInt(KEY_DISPLAY_STEPS, 0)
                .putInt(KEY_BURNED_CAL, 0)
                .putString(KEY_LAST_SYNC, "Не синхронизировано")
                .apply()
        }
    }

    private fun updateStepDisplay(steps: Int, source: String): HealthStepData {
        val today = LocalDate.now().toString()
        val timeNow = "Сегодня, ${DateTimeFormatter.ofPattern("HH:mm").format(java.time.LocalTime.now())}"
        val burnedCal = (steps * 0.045).toInt()
        val activeMin = steps / 160
        val distKm = ((steps * 0.00075) * 10).toInt() / 10.0

        val data = _stepData.value.copy(
            dateString = today,
            stepsCount = steps,
            burnedCalories = burnedCal,
            activeMinutes = activeMin,
            distanceKm = distKm,
            dataSource = source,
            lastSyncTime = timeNow
        )

        prefs.edit()
            .putString(KEY_STEPS_DATE, today)
            .putInt(KEY_DISPLAY_STEPS, steps)
            .putInt(KEY_BURNED_CAL, burnedCal)
            .putString(KEY_LAST_SYNC, timeNow)
            .apply()

        _stepData.value = data
        return data
    }

    private fun loadStepData(): HealthStepData {
        val today = LocalDate.now().toString()
        checkAndResetNewDay(today)

        val steps = prefs.getInt(KEY_DISPLAY_STEPS, 0)
        val lastSync = prefs.getString(KEY_LAST_SYNC, "Не синхронизировано") ?: "Не синхронизировано"
        val burnedCal = (steps * 0.045).toInt()
        val activeMin = steps / 160
        val distKm = ((steps * 0.00075) * 10).toInt() / 10.0

        return HealthStepData(
            dateString = today,
            stepsCount = steps,
            burnedCalories = burnedCal,
            activeMinutes = activeMin,
            distanceKm = distKm,
            dataSource = "Аппаратный шагомер",
            lastSyncTime = lastSync
        )
    }

    companion object {
        private const val PREFS_NAME = "health_connect_prefs"
        private const val KEY_STEPS_DATE = "key_steps_date"
        private const val KEY_BASE_SENSOR_STEPS = "key_base_sensor_steps"
        private const val KEY_SENSOR_STEPS = "key_sensor_steps"
        private const val KEY_HEALTH_CONNECT_STEPS = "key_health_connect_steps"
        private const val KEY_DISPLAY_STEPS = "health_display_steps"
        private const val KEY_BURNED_CAL = "health_burned_cal"
        private const val KEY_LAST_SYNC = "health_last_sync"
    }
}
