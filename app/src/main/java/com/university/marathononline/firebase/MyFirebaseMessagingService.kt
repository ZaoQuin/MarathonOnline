package com.university.marathononline.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.university.marathononline.R
import com.university.marathononline.ui.view.activity.NotificationsActivity
import com.university.marathononline.data.models.Notification
import com.university.marathononline.data.models.ENotificationType
import com.university.marathononline.ui.view.activity.RecordFeedbackActivity
import com.university.marathononline.utils.KEY_FEEDBACK
import com.university.marathononline.utils.KEY_NOTIFICATION_DATA
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "marathon_notifications"
        private const val CHANNEL_NAME = "Marathon Notifications"
        private const val FEEDBACK_CHANNEL_ID = "feedback_notifications"
        private const val FEEDBACK_CHANNEL_NAME = "Feedback Notifications"
        private const val NOTIFICATION_ID = 1001
        const val ACTION_NEW_NOTIFICATION = "com.university.marathononline.NEW_NOTIFICATION"
        const val ACTION_NEW_FEEDBACK = "com.university.marathononline.NEW_FEEDBACK"
        const val ACTION_UPDATE_BADGE = "com.university.marathononline.UPDATE_BADGE"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "From: ${remoteMessage.from}")

        // Xử lý data payload
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataPayload(remoteMessage.data, remoteMessage.notification)
        }

        // Xử lý notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }
    }

    private fun handleDataPayload(data: Map<String, String>, notification: RemoteMessage.Notification?) {
        try {
            val feedbackType = data["type"]

            when (feedbackType) {
                "ADMIN_FEEDBACK", "RUNNER_FEEDBACK" -> {
                    handleFeedbackNotification(data, notification)
                }
                else -> {
                    handleGeneralNotification(data, notification)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling data payload: ${e.message}")
        }
    }

    private fun handleFeedbackNotification(data: Map<String, String>, notification: RemoteMessage.Notification?) {
        try {
            val feedbackId = data["feedbackId"]?.toLongOrNull()
            val recordId = data["recordId"]?.toLongOrNull()
            val runnerId = data["runnerId"]?.toLongOrNull()
            val feedbackType = data["type"]

            Log.d(TAG, "Handling feedback notification: feedbackId=$feedbackId, recordId=$recordId, type=$feedbackType")

            // Tạo notification object cho feedback
            val notificationData = Notification(
                id = data["notificationId"]?.toLongOrNull(),
                title = notification?.title ?: getFeedbackTitle(feedbackType),
                content = notification?.body ?: getFeedbackContent(feedbackType),
                createAt = getCurrentTimestamp(),
                isRead = false,
                type = ENotificationType.RECORD_FEEDBACK
            )

            // Gửi broadcast riêng cho feedback
            sendFeedbackBroadcast(notificationData, feedbackId, recordId, feedbackType)

            // Hiển thị notification với action khác cho feedback
            showFeedbackNotification(
                notificationData.title ?: "Feedback mới",
                notificationData.content ?: "Bạn có feedback mới",
                notificationData,
                recordId,
                feedbackId
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error handling feedback notification: ${e.message}")
        }
    }

    private fun handleGeneralNotification(data: Map<String, String>, notification: RemoteMessage.Notification?) {
        val notificationData = parseNotificationData(data, notification)

        Log.d(TAG, "Parsed notification: ID=${notificationData.id}, Title='${notificationData.title}', Content='${notificationData.content}'")

        // Gửi broadcast để cập nhật UI real-time
        sendNotificationBroadcast(notificationData)

        // Hiển thị system notification
        showNotification(
            notificationData.title ?: notification?.title ?: "Marathon Online",
            notificationData.content ?: notification?.body ?: "Bạn có thông báo mới",
            notificationData
        )
    }

    private fun parseNotificationData(data: Map<String, String>, notification: RemoteMessage.Notification?): Notification {
        // Get notification ID from data payload
        val notificationId = data["notificationId"]?.toLongOrNull() ?: data["id"]?.toLongOrNull()

        // Get type from data payload
        val notificationType = data["type"]?.let {
            try {
                when (it) {
                    "ADMIN_FEEDBACK", "RUNNER_FEEDBACK" -> ENotificationType.RECORD_FEEDBACK
                    else -> ENotificationType.valueOf(it)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Unknown notification type: $it")
                null
            }
        }

        // Create notification with available data, filling in missing fields
        return Notification(
            id = notificationId,
            title = data["title"] ?: notification?.title ?: getDefaultTitleForType(notificationType),
            content = data["content"] ?: data["body"] ?: notification?.body ?: getDefaultContentForType(notificationType),
            createAt = data["createAt"] ?: getCurrentTimestamp(),
            isRead = false,
            type = notificationType
        ).also {
            Log.d(TAG, "Created notification object: ID=${it.id}, Title='${it.title}', Content='${it.content}', Type=${it.type}")
        }
    }

    private fun getFeedbackTitle(feedbackType: String?): String {
        return when (feedbackType) {
            "ADMIN_FEEDBACK" -> "Phản hồi từ Admin"
            "RUNNER_FEEDBACK" -> "Phản hồi mới từ Runner"
            else -> "Feedback mới"
        }
    }

    private fun getFeedbackContent(feedbackType: String?): String {
        return when (feedbackType) {
            "ADMIN_FEEDBACK" -> "Admin đã phản hồi về record của bạn"
            "RUNNER_FEEDBACK" -> "Có phản hồi mới về record"
            else -> "Bạn có feedback mới"
        }
    }

    private fun getDefaultTitleForType(type: ENotificationType?): String {
        return when (type) {
            ENotificationType.REWARD -> "Giải thưởng mới"
            ENotificationType.NEW_CONTEST -> "Cuộc thi mới"
            ENotificationType.BLOCK_CONTEST -> "Thông báo chặn"
            ENotificationType.ACCEPT_CONTEST -> "Cuộc thi được duyệt"
            ENotificationType.NOT_APPROVAL_CONTEST -> "Cuộc thi không được duyệt"
            ENotificationType.REJECTED_RECORD -> "Phát hiện gian lận"
            ENotificationType.RECORD_FEEDBACK -> "Feedback mới"
            null -> "Marathon Online"
        }
    }

    private fun getDefaultContentForType(type: ENotificationType?): String {
        return when (type) {
            ENotificationType.REWARD -> "Bạn có giải thưởng mới"
            ENotificationType.NEW_CONTEST -> "Có cuộc thi mới dành cho bạn"
            ENotificationType.BLOCK_CONTEST -> "Bạn đã bị chặn khỏi cuộc thi"
            ENotificationType.ACCEPT_CONTEST -> "Cuộc thi của bạn đã được duyệt"
            ENotificationType.NOT_APPROVAL_CONTEST -> "Cuộc thi của bạn không được duyệt"
            ENotificationType.REJECTED_RECORD -> "Bản ghi gần đây của bạn có vấn đề."
            ENotificationType.RECORD_FEEDBACK -> "Bạn có feedback mới về record"
            null -> "Bạn có thông báo mới"
        }
    }

    private fun getCurrentTimestamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun sendNotificationBroadcast(notification: Notification) {
        Log.d(TAG, "Sending broadcast for notification: ${notification.id}")

        // Gửi local broadcast
        val localIntent = Intent(ACTION_NEW_NOTIFICATION)
        localIntent.putExtra(KEY_NOTIFICATION_DATA, notification)
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent)

        // Gửi global broadcast
        val globalIntent = Intent(ACTION_NEW_NOTIFICATION)
        globalIntent.putExtra(KEY_NOTIFICATION_DATA, notification)
        sendBroadcast(globalIntent)

        // Gửi broadcast để cập nhật badge
        val badgeIntent = Intent(ACTION_UPDATE_BADGE)
        sendBroadcast(badgeIntent)

        Log.d(TAG, "Broadcasts sent successfully")
    }

    private fun sendFeedbackBroadcast(
        notification: Notification,
        feedbackId: Long?,
        recordId: Long?,
        feedbackType: String?
    ) {
        Log.d(TAG, "Sending feedback broadcast: feedbackId=$feedbackId, recordId=$recordId")

        val feedbackIntent = Intent(ACTION_NEW_FEEDBACK).apply {
            putExtra(KEY_NOTIFICATION_DATA, notification)
            putExtra("feedbackId", feedbackId)
            putExtra("recordId", recordId)
            putExtra("feedbackType", feedbackType)
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(feedbackIntent)
        sendBroadcast(feedbackIntent)
        sendNotificationBroadcast(notification)

        Log.d(TAG, "Feedback broadcasts sent successfully")
    }

    private fun showNotification(title: String?, body: String?, notification: Notification) {
        val intent = Intent(this, NotificationsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(KEY_NOTIFICATION_DATA, notification)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.marathon_online)
            .setContentTitle(title ?: "Marathon Online")
            .setContentText(body ?: "Bạn có thông báo mới")
            .setAutoCancel(true)
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())

        Log.d(TAG, "System notification shown: $title")
    }

    private fun showFeedbackNotification(
        title: String,
        body: String,
        notification: Notification,
        recordId: Long?,
        feedbackId: Long?
    ) {
        val intent = Intent(this, RecordFeedbackActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(KEY_NOTIFICATION_DATA, notification)
            putExtra("recordId", recordId)
            putExtra("feedbackId", feedbackId)
            putExtra("openFeedbackTab", true) // Để mở tab feedback
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            (System.currentTimeMillis() + (feedbackId ?: 0)).toInt(),
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, FEEDBACK_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_feedback) // Icon riêng cho feedback
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE) // Categorize as message

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(
            "feedback_${feedbackId}_${recordId}".hashCode(),
            notificationBuilder.build()
        )

        Log.d(TAG, "Feedback notification shown: $title")
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // General notification channel
            val generalChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for Marathon Online notifications"
                enableLights(true)
                enableVibration(true)
            }

            // Feedback notification channel
            val feedbackChannel = NotificationChannel(
                FEEDBACK_CHANNEL_ID,
                FEEDBACK_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for feedback notifications"
                enableLights(true)
                enableVibration(true)
                setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, null)
            }

            notificationManager.createNotificationChannel(generalChannel)
            notificationManager.createNotificationChannel(feedbackChannel)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Token sent to server successfully: $token")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send token to server: ${e.message}")
            }
        }
    }
}