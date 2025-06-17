package com.university.marathononline.service.foreground

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.university.marathononline.R
import com.university.marathononline.ui.view.activity.RecordActivity
import com.university.marathononline.ui.viewModel.tracking.LocationTracker
import com.university.marathononline.ui.viewModel.tracking.RecordingManager
import com.university.marathononline.ui.viewModel.tracking.StepCounter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.LocalDateTime


class RunningForegroundService : Service() {

    private lateinit var locationTracker: LocationTracker
    private lateinit var stepCounter: StepCounter
    private lateinit var recordingManager: RecordingManager
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var startTime: LocalDateTime? = null
    private var isPaused = false
    private var isStopping = false

    companion object {
        const val CHANNEL_ID = "RunningServiceChannel"
        const val NOTIFICATION_ID = 1
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
    }

    override fun onCreate() {
        super.onCreate()
        locationTracker = LocationTracker(this)
        stepCounter = StepCounter(this)
        recordingManager = RecordingManager()

        startTime = LocalDateTime.now()

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        setupTracking()
    }

    private fun setupTracking() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf()
            return
        }

        locationTracker.onLocationUpdate = { _, distanceInKm ->
            if (!isPaused && !isStopping) {
                recordingManager.updateDistance(distanceInKm)
                broadcastUpdate()
            }
        }

        serviceScope.launch {
            locationTracker.isGPSEnabled.collect { isEnabled ->
                if (!isEnabled && !isStopping) {
                    stopSelf()
                }
            }
        }

        serviceScope.launch {
            while (recordingManager.isRecording.value && !isPaused && !isStopping) {
                recordingManager.updateTime()
                updateNotification()
                broadcastUpdate()
                kotlinx.coroutines.delay(1000)
            }
        }

        recordingManager.startRecording()
        if (locationTracker.startLocationUpdates()) {
            stepCounter.startCounting()
        } else {
            stopSelf()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Running Tracker",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Theo dõi quá trình chạy bộ"
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val stopIntent = Intent(this, RunningForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openIntent = Intent(this, RecordActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formattedTime = recordingManager.time.value
        val distance = recordingManager.distance.value
        val pace = recordingManager.averagePace.value

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_runner)
            .setContentTitle("🏃‍♂️ Marathon Online")
            .setContentText("$formattedTime")
            .setSubText("$distance • $pace")
            .setContentIntent(openPendingIntent)
            .setOngoing(true)
            .setShowWhen(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(ContextCompat.getColor(this, R.color.main_color))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setAutoCancel(false)

        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText("⏱️ $formattedTime\n📍 $distance\n⚡ $pace")
            .setSummaryText(if (isPaused) "⏸️ Tạm dừng" else if (isStopping) "⏹️ Đang dừng..." else "▶️ Đang chạy")

        builder.setStyle(bigTextStyle)

        if (!isStopping) {
            builder.addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_stop,
                    "Dừng",
                    stopPendingIntent
                ).build()
            )
        }

        return builder.build()
    }

    private fun updateNotification() {
        if (!isStopping) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, buildNotification())
        }
    }

    private fun broadcastUpdate() {
        if (!isStopping) {
            val intent = Intent("RUNNING_UPDATE")
            intent.putExtra("time", recordingManager.time.value)
            intent.putExtra("distance", recordingManager.distance.value)
            intent.putExtra("pace", recordingManager.averagePace.value)
            intent.putExtra("isRecording", recordingManager.isRecording.value && !isPaused && !isStopping)
            intent.putExtra("isPaused", isPaused)
            intent.putExtra("isStopping", isStopping)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }
    }

    private fun broadcastStop() {
        val intent = Intent("RUNNING_STOPPED")
        intent.putExtra("steps", stepCounter.steps.value)
        intent.putExtra("distance", recordingManager.totalDistance)
        intent.putExtra("avgSpeed", recordingManager.getAverageSpeed())
        intent.putExtra("startTime", startTime?.toString() ?: LocalDateTime.now().toString())
        intent.putExtra("endTime", LocalDateTime.now().toString())
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun stopTracking() {
        if (isStopping) return

        isStopping = true
        Log.d("RunningForegroundService", "Stopping tracking...")

        broadcastUpdate()

        locationTracker.stopLocationUpdates()
        stepCounter.stopCounting()
        recordingManager.stopRecording()

        val prefs = getSharedPreferences("RunningData", Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putInt("steps", stepCounter.steps.value)
            putFloat("distance", recordingManager.totalDistance.toFloat())
            putFloat("avgSpeed", recordingManager.getAverageSpeed().toFloat())
            putString("startTime", startTime?.toString() ?: LocalDateTime.now().toString())
            putString("endTime", LocalDateTime.now().toString())
            apply()
        }

        broadcastStop()
        serviceScope.cancel()
    }

    private fun pauseTracking() {
        if (isStopping) return

        isPaused = true
        locationTracker.stopLocationUpdates()
        stepCounter.stopCounting()
        updateNotification()
        broadcastUpdate()
    }

    private fun resumeTracking() {
        if (isStopping) return

        isPaused = false
        if (locationTracker.startLocationUpdates()) {
            stepCounter.startCounting()
        }
        updateNotification()
        broadcastUpdate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                Log.d("RunningForegroundService", "Received ACTION_STOP")
                stopTracking()

                getSharedPreferences("RunningData", Context.MODE_PRIVATE).edit().clear().apply()

                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_PLAY -> {
                Log.d("RunningForegroundService", "Received play action")
                if (!isStopping) {
                    resumeTracking()
                }
            }
            ACTION_PAUSE -> {
                Log.d("RunningForegroundService", "Received pause action")
                if (!isStopping) {
                    pauseTracking()
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d("RunningForegroundService", "Service destroyed")
        super.onDestroy()
        if (!isStopping) {
            stopTracking()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}