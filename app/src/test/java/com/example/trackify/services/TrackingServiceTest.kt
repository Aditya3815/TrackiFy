package com.example.trackify.services

import android.content.Intent
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import com.example.trackify.util.Constants
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSystemClock
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P]) // Example SDK, adjust if necessary
class TrackingServiceTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var service: TrackingService
    private val testScheduler = TestCoroutineScheduler()
    private val mainTestDispatcher = StandardTestDispatcher(testScheduler)

    private lateinit var mockFusedLocationClient: FusedLocationProviderClient
    private lateinit var mockBaseNotificationBuilder: NotificationCompat.Builder

    @Before
    fun setup() {
        Dispatchers.setMain(mainTestDispatcher)

        mockFusedLocationClient = mock()
        mockBaseNotificationBuilder = mock()

        // Mock chained calls for the notification builder
        whenever(mockBaseNotificationBuilder.addAction(any(), anyOrNull(), anyOrNull())).thenReturn(mockBaseNotificationBuilder)
        whenever(mockBaseNotificationBuilder.setContentText(anyOrNull())).thenReturn(mockBaseNotificationBuilder)

        service = TrackingService()
        service.fusedLocationProviderClient = mockFusedLocationClient
        service.baseNotificationBuilder = mockBaseNotificationBuilder

        service.onCreate()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialStart_Run5s_Pause_viaOnStartCommand() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        val startIntent = Intent(context, TrackingService::class.java).apply {
            action = Constants.ACTION_START_OR_RESUME_SERVICE
        }
        service.onStartCommand(startIntent, 0, 1)
        testScheduler.advanceUntilIdle()

        ShadowSystemClock.advanceBy(5000L, TimeUnit.MILLISECONDS)
        testScheduler.advanceUntilIdle()

        val pauseIntent = Intent(context, TrackingService::class.java).apply {
            action = Constants.ACTION_PAUSE_SERVICE
        }
        service.onStartCommand(pauseIntent, 0, 2)
        testScheduler.advanceUntilIdle()

        assertThat(TrackingService.timeRunInMilis.value).isEqualTo(5000L)
        assertThat(service.timeRun).isEqualTo(5000L)
        assertThat(TrackingService.isTracking.value).isFalse()
    }

    @Test
    fun testResume_Run3s_AfterInitial5s_Pause_viaOnStartCommand() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        service.isFirstRun = false
        service.timeRun = 5000L // Simulate previous run
        TrackingService.timeRunInMilis.postValue(5000L) // Ensure LiveData reflects this
        TrackingService.isTracking.postValue(false) // Ensure service is paused initially

        val resumeIntent = Intent(context, TrackingService::class.java).apply {
            action = Constants.ACTION_START_OR_RESUME_SERVICE
        }
        service.onStartCommand(resumeIntent, 0, 3)
        testScheduler.advanceUntilIdle()

        ShadowSystemClock.advanceBy(3000L, TimeUnit.MILLISECONDS)
        testScheduler.advanceUntilIdle()

        val pauseIntent = Intent(context, TrackingService::class.java).apply {
            action = Constants.ACTION_PAUSE_SERVICE
        }
        service.onStartCommand(pauseIntent, 0, 4)
        testScheduler.advanceUntilIdle()

        assertThat(TrackingService.timeRunInMilis.value).isEqualTo(8000L)
        assertThat(service.timeRun).isEqualTo(8000L)
        assertThat(TrackingService.isTracking.value).isFalse()
    }
}
