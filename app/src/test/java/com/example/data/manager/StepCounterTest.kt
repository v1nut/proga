package com.example.data.manager

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StepCounterTest {

    private lateinit var context: Context
    private lateinit var manager: HealthConnectManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        manager = HealthConnectManager(context)
    }

    @Test
    fun testInitialStepDataIsZero() {
        val data = manager.stepData.value
        assertEquals("Аппаратный шагомер", data.dataSource)
        assertEquals(0, data.stepsCount)
        assertEquals(0, data.burnedCalories)
    }
}
