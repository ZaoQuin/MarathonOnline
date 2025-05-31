package com.university.marathononline.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.university.marathononline.R
import com.university.marathononline.ui.view.activity.NotificationsActivity
import com.university.marathononline.data.models.Notification
import com.university.marathononline.data.models.ENotificationType
import com.university.marathononline.utils.KEY_NOTIFICATION_DATA
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "marathon_notifications"
        private const val CHANNEL_NAME = "Marathon Notifications"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "From: ${remoteMessage.from}")

        // Xử lý data payload
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataPayload(remoteMessage.data)
        }

        // Xử lý notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            showNotification(it.title, it.body, remoteMessage.data)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")

        // Gửi token lên server
        sendTokenToServer(token)
    }

    private fun handleDataPayload(data: Map<String, String>) {
        try {
            // Parse dữ liệu từ Firebase
            val notificationData = parseNotificationData(data)

            // Lưu notification vào local database hoặc gửi broadcast
            saveNotificationLocally(notificationData)

            // Hiển thị notification
            showNotification(
                notificationData.title ?: "Marathon Online",
                notificationData.content ?: "Bạn có thông báo mới",
                data
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error handling data payload: ${e.message}")
        }
    }

    private fun parseNotificationData(data: Map<String, String>): Notification {
        return Notification(
            id = data["id"]?.toLongOrNull(),
            title = data["title"],
            content = data["content"],
            createAt = data["createAt"],
            isRead = false,
            type = data["type"]?.let { ENotificationType.valueOf(it) }
        )
    }

    private fun saveNotificationLocally(notification: Notification) {
        // Có thể lưu vào Room Database hoặc SharedPreferences
        // Hoặc gửi broadcast để activity nhận và xử lý
        val intent = Intent("com.university.marathononline.NEW_NOTIFICATION")
        intent.putExtra(KEY_NOTIFICATION_DATA, notification)
        sendBroadcast(intent)
    }

    private fun showNotification(title: String?, body: String?, data: Map<String, String>) {
        val intent = Intent(this, NotificationsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            // Truyền dữ liệu notification nếu cần
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.marathon_online) // Thêm icon notification
            .setContentTitle(title ?: "Marathon Online")
            .setContentText(body ?: "Bạn có thông báo mới")
            .setAutoCancel(true)
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for Marathon Online notifications"
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendTokenToServer(token: String) {
        // Gửi FCM token lên server để lưu trữ
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Gọi API để lưu token
                // Ví dụ: api.updateFCMToken(token)
                Log.d(TAG, "Token sent to server successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send token to server: ${e.message}")
            }
        }
    }
}