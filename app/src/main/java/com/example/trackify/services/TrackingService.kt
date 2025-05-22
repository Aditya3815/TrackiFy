package com.example.trackify.services

import com.example.trackify.R
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.trackify.util.Constants
import com.example.trackify.util.Constants.ACTION_PAUSE_SERVICE
import com.example.trackify.util.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.trackify.util.Constants.ACTION_STOP_SERVICE
import com.example.trackify.util.Constants.NOTIFICATION_CHANNEL_ID
import com.example.trackify.util.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.trackify.util.Constants.NOTIFICATION_ID
import com.example.trackify.util.Constants.TIMER_UPDATE_INTERVAL
import com.example.trackify.util.TrackingUtility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.jvm.java

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@Suppress("DEPRECATION")
@AndroidEntryPoint
class TrackingService : LifecycleService() {

    var isFirstRun = true
    var serviceKilled = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val timeRunInseconds = MutableLiveData<Long>()

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    lateinit var curNotificationBuilder: NotificationCompat.Builder

    companion object {
        val timeRunInMilis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
    }


    /**
     * Initializes default values for LiveData objects.
     */
    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInMilis.postValue(0L)
        timeRunInseconds.postValue(0L)

    }


    /**
     * Called when the service is created. Sets up notification builder,
     * initializes LiveData, and observes tracking state.
     */
    override fun onCreate() {
        super.onCreate()
        curNotificationBuilder = baseNotificationBuilder
        postInitialValues()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        isTracking.observe(this, Observer {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        })
    }

    private fun killService(){
        serviceKilled = true
        isFirstRun = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Resuming service...")
                        isTracking.postValue(true) // Set tracking true BEFORE starting timer logic
                        startTimerLogic() // Call the refactored timer logic
                    }
                }

                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                    pauseService()
                }

                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                    killService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // private var isTimerEnabled = false // Removed
    internal var timeRun = 0L // Accumulated run time in milliseconds
    // private var lapTime = 0L // Removed
    private var timeStarted = 0L // Timestamp when the current segment started


    // Renamed from startTimer and refactored
    private fun startTimerLogic() {
        addEmptyPolyline() // Start a new line segment
        timeStarted = System.currentTimeMillis() // Record start time for this segment

        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value == true) { // Loop while tracking is active
                val currentLapDuration = System.currentTimeMillis() - timeStarted
                val totalMillis = timeRun + currentLapDuration // timeRun is previously accumulated time
                timeRunInMilis.postValue(totalMillis)
                timeRunInseconds.postValue(totalMillis / 1000L)
                delay(TIMER_UPDATE_INTERVAL)
            }
            // The coroutine stops when isTracking.value becomes false
        }
    }


    private fun pauseService() {
        isTracking.postValue(false) // Signal that tracking should stop; this stops the coroutine in startTimerLogic
        // isTimerEnabled = false; // Removed

        if (timeStarted != 0L) { // Check if the timer was actually running for this segment
            val finalLapDuration = System.currentTimeMillis() - timeStarted
            timeRun += finalLapDuration // Add the duration of the just-completed segment to the total
            timeStarted = 0L // Reset timeStarted to indicate the segment has ended
        }
    }

    /**
     * Updates the notification's action button (Pause/Resume) based on tracking state.
     * @param isTracking Whether tracking is currently active.
     */
    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "Pause" else "Resume"
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        }
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // The reflection code was here and is now removed.
        // curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
        //     isAccessible = true
        //     set(curNotificationBuilder, ArrayList<NotificationCompat.Action>())
        // }

        if (!serviceKilled){
            curNotificationBuilder.clearActions() // Clear previous actions from the existing curNotificationBuilder
            curNotificationBuilder.addAction(R.drawable.round_pause_24, notificationActionText, pendingIntent) // Add the new action to the existing curNotificationBuilder
            notificationManager.notify(NOTIFICATION_ID, curNotificationBuilder.build())
        }

    }


    /**
     * Enables/disables location updates based on the tracking state.
     * @param isTracking Whether to request or remove location updates.
     */
    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermissions(this)) {
                val request = LocationRequest().apply {
                    interval = Constants.LOCATION_UPDATE_INTERVAL
                    fastestInterval = Constants.FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }


    /**
     * Callback triggered when a new location is received. Adds coordinates to pathPoints.
     */
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            if (isTracking.value!!) {
                p0.locations.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        Timber.d("NEW LOCATION: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }


    /**
     * Adds a new location point to the current polyline.
     * @param location The latest Location object from the device.
     */
    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }


    /**
     * Starts a new polyline segment when tracking begins or resumes.
     */
    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))


    /**
     * Starts the foreground service and initializes notification updates.
     */
    private fun startForegroundService() {
        // addEmptyPolyline() // Removed, called by startTimerLogic now
        isTracking.postValue(true) // Signal tracking active

        val notificationManager = getSystemService(NOTIFICATION_SERVICE)
                as NotificationManager

        createNotificationChannel(notificationManager)

        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        timeRunInseconds.observe(this, Observer {
            if (!serviceKilled) {
                val notification = curNotificationBuilder
                    .setContentText(TrackingUtility.getFormattedStopWatchTime(it * 1000L))
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        })
        startTimerLogic() // Call the refactored timer logic function
    }


    /**
     * Creates a notification channel for Android Oreo and above.
     * @param notificationManager The system NotificationManager.
     */

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}