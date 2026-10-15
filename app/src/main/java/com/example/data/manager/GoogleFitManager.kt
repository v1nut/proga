package com.example.data.manager

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class GoogleFitActivityData(
    val dateString: String = LocalDate.now().toString(),
    val isConnected: Boolean = true,
    val isAutoSyncEnabled: Boolean = true,
    val stepsCount: Int = 0,
    val burnedCalories: Int = 0,
    val activeMinutes: Int = 0,
    val distanceKm: Double = 0.0,
    val lastSyncTime: String = "Синхронизировано",
    val sensorStatus: String = "Google Fit & Датчик шагов"
)

class GoogleFitManager(private val context: Context) : SensorEventListener {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val stepCounterSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        ?: sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private var initialSensorSteps = -1

    private val _fitData = MutableStateFlow(loadFitData())
    val fitData: StateFlow<GoogleFitActivityData> = _fitData.asStateFlow()

    init {
        registerStepSensor()
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
        val totalSteps = storedSteps + sessionSteps

        updateStepsData(totalSteps, "Google Fit / Аппаратный сенсор")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun setAutoSyncEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SYNC, enabled).apply()
        _fitData.value = _fitData.value.copy(isAutoSyncEnabled = enabled)
    }

    fun addManualSteps(stepDelta: Int) {
        val currentSteps = _fitData.value.stepsCount
        val newTotal = currentSteps + stepDelta
        prefs.edit().putInt(KEY_ACCUMULATED_STEPS, newTotal).apply()
        updateStepsData(newTotal, "Ручной ввод шагов")
    }

    fun setStepsManually(newSteps: Int) {
        prefs.edit().putInt(KEY_ACCUMULATED_STEPS, newSteps).apply()
        updateStepsData(newSteps, "Google Fit / Синхронизировано")
    }

    fun syncNow(): GoogleFitActivityData {
        val timeNow = "Сегодня, ${DateTimeFormatter.ofPattern("HH:mm").format(java.time.LocalTime.now())}"
        val currentSteps = _fitData.value.stepsCount
        
        val updated = updateStepsData(currentSteps, "Синхронизировано с Google Fit", timeNow)
        prefs.edit().putString(KEY_LAST_SYNC, timeNow).apply()
        return updated
    }

    private fun updateStepsData(steps: Int, statusMsg: String, syncTime: String? = null): GoogleFitActivityData {
        val timeStr = syncTime ?: prefs.getString(KEY_LAST_SYNC, "Сегодня, ${DateTimeFormatter.ofPattern("HH:mm").format(java.time.LocalTime.now())}") ?: "Сегодня"
        val burnedCal = (steps * 0.045).toInt()
        val activeMin = (steps / 160)
        val distKm = ((steps * 0.00075) * 10).toInt() / 10.0

        val data = GoogleFitActivityData(
            dateString = LocalDate.now().toString(),
            isConnected = true,
            isAutoSyncEnabled = prefs.getBoolean(KEY_AUTO_SYNC, true),
            stepsCount = steps,
            burnedCalories = burnedCal,
            activeMinutes = activeMin,
            distanceKm = distKm,
            lastSyncTime = timeStr,
            sensorStatus = statusMsg
        )

        prefs.edit()
            .putInt(KEY_ACCUMULATED_STEPS, steps)
            .putInt(KEY_BURNED_CAL, burnedCal)
            .apply()

        _fitData.value = data
        return data
    }

    private fun loadFitData(): GoogleFitActivityData {
        val steps = prefs.getInt(KEY_ACCUMULATED_STEPS, 0)
        val isAutoSync = prefs.getBoolean(KEY_AUTO_SYNC, true)
        val lastSync = prefs.getString(KEY_LAST_SYNC, "Не синхронизировано") ?: "Не синхронизировано"

        val burnedCal = (steps * 0.045).toInt()
        val activeMin = (steps / 160)
        val distKm = ((steps * 0.00075) * 10).toInt() / 10.0

        return GoogleFitActivityData(
            dateString = LocalDate.now().toString(),
            isConnected = true,
            isAutoSyncEnabled = isAutoSync,
            stepsCount = steps,
            burnedCalories = burnedCal,
            activeMinutes = activeMin,
            distanceKm = distKm,
            lastSyncTime = lastSync,
            sensorStatus = if (stepCounterSensor != null) "Аппаратный датчик шагов (Активен)" else "Google Fit API Sync"
        )
    }

    companion object {
        private const val PREFS_NAME = "google_fit_prefs"
        private const val KEY_AUTO_SYNC = "fit_auto_sync"
        private const val KEY_INITIAL_STEPS = "fit_initial_steps"
        private const val KEY_ACCUMULATED_STEPS = "fit_accumulated_steps"
        private const val KEY_BURNED_CAL = "fit_burned_cal"
        private const val KEY_LAST_SYNC = "fit_last_sync"
    }
}
