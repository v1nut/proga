package com.example.data.manager

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class HealthStepData(
    val dateString: String = LocalDate.now().toString(),
    val stepsCount: Int = 0,
    val burnedCalories: Int = 0,
    val activeMinutes: Int = 0,
    val distanceKm: Double = 0.0,
    val dataSource: String = "Системный Health Connect",
    val lastSyncTime: String = "Автоматическая синхронизация"
)

class HealthConnectManager(private val context: Context) : SensorEventListener {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val healthConnectClient by lazy {
        if (HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE) {
            HealthConnectClient.getOrCreate(context)
        } else {
            null
        }
    }

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class)
    )

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val stepCounterSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        ?: sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private var initialSensorSteps = -1

    private val _stepData = MutableStateFlow(loadStepData())
    val stepData: StateFlow<HealthStepData> = _stepData.asStateFlow()

    init {
        registerStepSensor()
        readTodayHealthConnectSteps()
    }

    fun registerStepSensor() {
        stepCounterSensor?.let { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun unregisterStepSensor() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        val rawSteps = event.values.firstOrNull()?.toInt() ?: return

        if (initialSensorSteps < 0) {
            initialSensorSteps = prefs.getInt(KEY_INITIAL_STEPS, rawSteps)
            if (initialSensorSteps <= 0) {
                initialSensorSteps = rawSteps
                prefs.edit().putInt(KEY_INITIAL_STEPS, rawSteps).apply()
            }
        }

        val sessionSteps = maxOf(0, rawSteps - initialSensorSteps)
        val storedSteps = prefs.getInt(KEY_ACCUMULATED_STEPS, 0)
        val totalSteps = maxOf(storedSteps, sessionSteps)

        updateSteps(totalSteps, "Системный датчик шагов")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun readTodayHealthConnectSteps() {
        scope.launch {
            try {
                val client = healthConnectClient
                if (client != null) {
                    val startTime = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
                    val endTime = Instant.now()
                    val request = ReadRecordsRequest(
                        recordType = StepsRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                    val response = client.readRecords(request)
                    val healthSteps = response.records.sumOf { it.count.toInt() }

                    if (healthSteps > 0) {
                        updateSteps(healthSteps, "Health Connect API")
                        return@launch
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            // Fallback reading stored / sensor steps
            val currentStored = prefs.getInt(KEY_ACCUMULATED_STEPS, 0)
            updateSteps(currentStored, if (stepCounterSensor != null) "Системный датчик шагов" else "Health Connect (Система)")
        }
    }

    fun updateSteps(steps: Int, source: String): HealthStepData {
        val timeNow = "Сегодня, ${DateTimeFormatter.ofPattern("HH:mm").format(java.time.LocalTime.now())}"
        val burnedCal = (steps * 0.045).toInt()
        val activeMin = (steps / 160)
        val distKm = ((steps * 0.00075) * 10).toInt() / 10.0

        val data = HealthStepData(
            dateString = LocalDate.now().toString(),
            stepsCount = steps,
            burnedCalories = burnedCal,
            activeMinutes = activeMin,
            distanceKm = distKm,
            dataSource = source,
            lastSyncTime = timeNow
        )

        prefs.edit()
            .putInt(KEY_ACCUMULATED_STEPS, steps)
            .putInt(KEY_BURNED_CAL, burnedCal)
            .putString(KEY_LAST_SYNC, timeNow)
            .apply()

        _stepData.value = data
        return data
    }

    private fun loadStepData(): HealthStepData {
        val steps = prefs.getInt(KEY_ACCUMULATED_STEPS, 0)
        val lastSync = prefs.getString(KEY_LAST_SYNC, "Не синхронизировано") ?: "Не синхронизировано"

        val burnedCal = (steps * 0.045).toInt()
        val activeMin = (steps / 160)
        val distKm = ((steps * 0.00075) * 10).toInt() / 10.0

        return HealthStepData(
            dateString = LocalDate.now().toString(),
            stepsCount = steps,
            burnedCalories = burnedCal,
            activeMinutes = activeMin,
            distanceKm = distKm,
            dataSource = if (stepCounterSensor != null) "Системный шагомер" else "Health Connect",
            lastSyncTime = lastSync
        )
    }

    companion object {
        private const val PREFS_NAME = "health_connect_prefs"
        private const val KEY_INITIAL_STEPS = "health_initial_steps"
        private const val KEY_ACCUMULATED_STEPS = "health_accumulated_steps"
        private const val KEY_BURNED_CAL = "health_burned_cal"
        private const val KEY_LAST_SYNC = "health_last_sync"
    }
}
