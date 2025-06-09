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
import com.university.marathononline.data.models.Notification
import com.university.marathononline.data.models.ENotificationType
import com.university.marathononline.ui.view.activity.NotificationsActivity
import com.university.marathononline.ui.view.activity.RecordFeedbackActivity
import com.university.marathononline.utils.*
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
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataPayload(remoteMessage.data, remoteMessage.notification)
        }

        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }
    }

    private fun handleDataPayload(data: Map<String, String>, notification: RemoteMessage.Notification?) {
        try {
            when (data[KEY_FEEDBACK_TYPE]) {
                ADMIN_FEEDBACK, RUNNER_FEEDBACK -> handleFeedbackNotification(data, notification)
                else -> handleGeneralNotification(data, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling data payload: ${e.message}", e)
        }
    }

    private fun handleFeedbackNotification(data: Map<String, String>, notification: RemoteMessage.Notification?) {
        val feedbackId = data[KEY_FEEDBACK_ID]?.toLongOrNull()
        val recordId = data[KEY_RECORD_ID]?.toLongOrNull()
        val registrationId = data[KEY_REGISTRATION_ID]?.toLongOrNull()
        val feedbackType = data[KEY_FEEDBACK_TYPE]

        if (feedbackId == null && recordId == null && registrationId == null) {
            Log.w(TAG, "Invalid feedback notification data: $data")
            return
        }

        val notificationData = Notification(
            id = data[KEY_NOTIFICATION_ID]?.toLongOrNull() ?: System.currentTimeMillis(),
            title = notification?.title ?: getString(R.string.feedback_title, feedbackType ?: "Feedback"),
            content = notification?.body ?: getString(R.string.feedback_content, feedbackType ?: "Feedback"),
            createAt = getCurrentTimestamp(),
            isRead = false,
            type = when {
                recordId != null -> ENotificationType.RECORD_FEEDBACK
                registrationId != null -> ENotificationType.BLOCK_CONTEST
                else -> return
            },
            objectId = recordId ?: registrationId
        )

        sendFeedbackBroadcast(notificationData, feedbackId, recordId, registrationId, feedbackType)
        showNotification(
            title = notificationData.title!!,
            body = notificationData.content!!,
            notification = notificationData,
            channelId = FEEDBACK_CHANNEL_ID,
            intent = createFeedbackIntent(notificationData, feedbackId, recordId, registrationId)
        )
    }

    private fun handleGeneralNotification(data: Map<String, String>, notification: RemoteMessage.Notification?) {
        val notificationData = parseNotificationData(data, notification)
        Log.d(TAG, "Parsed notification: ID=${notificationData.id}, Title='${notificationData.title}', Content='${notificationData.content}'")

        sendNotificationBroadcast(notificationData)
        showNotification(
            title = notificationData.title ?: notification?.title ?: getString(R.string.app_name),
            body = notificationData.content ?: notification?.body ?: getString(R.string.default_notification_body),
            notification = notificationData,
            channelId = CHANNEL_ID,
            intent = createGeneralIntent(notificationData)
        )
    }

    private fun parseNotificationData(data: Map<String, String>, notification: RemoteMessage.Notification?): Notification {
        val notificationId = data[KEY_NOTIFICATION_ID]?.toLongOrNull() ?: data["id"]?.toLongOrNull() ?: System.currentTimeMillis()
        val objectId = data[KEY_OBJECT_ID]?.toLongOrNull()
        val notificationType = data[KEY_NOTIFICATION_TYPE]?.let {
            try {
                ENotificationType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Unknown notification type: $it")
                null
            }
        }

        return Notification(
            id = notificationId,
            title = data["title"] ?: notification?.title ?: getDefaultTitleForType(notificationType),
            content = data["content"] ?: data["body"] ?: notification?.body ?: getDefaultContentForType(notificationType),
            createAt = data["createAt"] ?: getCurrentTimestamp(),
            isRead = false,
            type = notificationType,
            objectId = objectId
        ).also {
            Log.d(TAG, "Created notification object: ID=${it.id}, Title='${it.title}', Content='${it.content}', Type=${it.type}, ObjectId=${it.objectId}")
        }
    }

    private fun getDefaultTitleForType(type: ENotificationType?): String = when (type) {
        ENotificationType.REWARD -> getString(R.string.notification_reward_title)
        ENotificationType.NEW_CONTEST -> getString(R.string.notification_new_contest_title)
        ENotificationType.BLOCK_CONTEST -> getString(R.string.notification_block_contest_title)
        ENotificationType.ACCEPT_CONTEST -> getString(R.string.notification_accept_contest_title)
        ENotificationType.NOT_APPROVAL_CONTEST -> getString(R.string.notification_not_approved_title)
        ENotificationType.REJECTED_RECORD -> getString(R.string.notification_rejected_record_title)
        ENotificationType.RECORD_FEEDBACK -> getString(R.string.notification_feedback_title)
        null -> getString(R.string.app_name)
    }

    private fun getDefaultContentForType(type: ENotificationType?): String = when (type) {
        ENotificationType.REWARD -> getString(R.string.notification_reward_content)
        ENotificationType.NEW_CONTEST -> getString(R.string.notification_new_contest_content)
        ENotificationType.BLOCK_CONTEST -> getString(R.string.notification_block_contest_content)
        ENotificationType.ACCEPT_CONTEST -> getString(R.string.notification_accept_contest_content)
        ENotificationType.NOT_APPROVAL_CONTEST -> getString(R.string.notification_not_approved_content)
        ENotificationType.REJECTED_RECORD -> getString(R.string.notification_rejected_record_content)
        ENotificationType.RECORD_FEEDBACK -> getString(R.string.notification_feedback_content)
        null -> getString(R.string.default_notification_body)
    }

    private fun getCurrentTimestamp(): String {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
    }

    private fun sendNotificationBroadcast(notification: Notification) {
        Log.d(TAG, "Sending broadcast for notification: ${notification.id}")

        val localIntent = Intent(ACTION_NEW_NOTIFICATION).apply {
            putExtra(KEY_NOTIFICATION_DATA, notification)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent)

        val globalIntent = Intent(ACTION_NEW_NOTIFICATION).apply {
            putExtra(KEY_NOTIFICATION_DATA, notification)
        }
        sendBroadcast(globalIntent)

        sendBroadcast(Intent(ACTION_UPDATE_BADGE))
        Log.d(TAG, "Broadcasts sent successfully")
    }

    private fun sendFeedbackBroadcast(
        notification: Notification,
        feedbackId: Long?,
        recordId: Long?,
        registrationId: Long?,
        feedbackType: String?
    ) {
        Log.d(TAG, "Sending feedback broadcast: feedbackId=$feedbackId, recordId=$recordId, registrationId=$registrationId")

        val feedbackIntent = Intent(ACTION_NEW_FEEDBACK).apply {
            putExtra(KEY_NOTIFICATION_DATA, notification)
            putExtra(KEY_FEEDBACK_ID, feedbackId)
            putExtra(KEY_RECORD_ID, recordId)
            putExtra(KEY_REGISTRATION_ID, registrationId)
            putExtra(KEY_FEEDBACK_TYPE, feedbackType)
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(feedbackIntent)
        sendBroadcast(feedbackIntent)
        sendNotificationBroadcast(notification)
        Log.d(TAG, "Feedback broadcasts sent successfully")
    }

    private fun showNotification(
        title: String,
        body: String,
        notification: Notification,
        channelId: String,
        intent: Intent?
    ) {
        val pendingIntent = intent?.let {
            PendingIntent.getActivity(
                this,
                notification.id?.toInt() ?: System.currentTimeMillis().toInt(),
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.marathon_online)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .apply { pendingIntent?.let { setContentIntent(it) } }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notification.id?.toInt() ?: System.currentTimeMillis().toInt(), notificationBuilder.build())
        Log.d(TAG, "Notification shown: $title")
    }

    private fun createGeneralIntent(notification: Notification): Intent {
        return Intent(this, NotificationsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(KEY_NOTIFICATION_DATA, notification)
        }
    }

    private fun createFeedbackIntent(notification: Notification, feedbackId: Long?, recordId: Long?, registrationId: Long?): Intent {
        return Intent(this, RecordFeedbackActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(KEY_NOTIFICATION_DATA, notification)
            putExtra(KEY_FEEDBACK_ID, feedbackId)
            putExtra(KEY_RECORD_ID, recordId)
            putExtra(KEY_REGISTRATION_ID, registrationId)
            putExtra(OPEN_FEEDBACK_TAB, true)
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val generalChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_general_desc)
                enableLights(true)
                enableVibration(true)
            }

            val feedbackChannel = NotificationChannel(
                FEEDBACK_CHANNEL_ID,
                FEEDBACK_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_feedback_desc)
                enableLights(true)
                enableVibration(true)
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
                Log.e(TAG, "Failed to send token to server: ${e.message}", e)
            }
        }
    }
}