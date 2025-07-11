package com.university.marathononline.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.university.marathononline.R
import com.university.marathononline.data.models.ENotificationType
import com.university.marathononline.data.models.Notification
import com.university.marathononline.ui.view.activity.ContestDetailsActivity
import com.university.marathononline.ui.view.activity.ManagementDetailsContestActivity
import com.university.marathononline.ui.view.activity.NotificationsActivity
import com.university.marathononline.ui.view.activity.RecordFeedbackActivity
import com.university.marathononline.ui.view.activity.RunnerRewardsActivity

object NotificationUtils {

    const val CHANNEL_ID_GENERAL = "marathon_notifications"
    const val CHANNEL_ID_FEEDBACK = "feedback_notifications"
    const val CHANNEL_ID_CONTEST = "marathon_contest"
    const val CHANNEL_ID_REWARD = "marathon_reward"
    const val KEY_EMAIL = "email"
    const val KEY_OBJECT_ID = "objectId"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                context.getString(R.string.notification_channel_general_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_general_desc)
                enableLights(true)
                enableVibration(true)
            }

            val feedbackChannel = NotificationChannel(
                CHANNEL_ID_FEEDBACK,
                context.getString(R.string.notification_channel_feedback_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_feedback_desc)
                enableLights(true)
                enableVibration(true)
            }

            val contestChannel = NotificationChannel(
                CHANNEL_ID_CONTEST,
                context.getString(R.string.notification_channel_contest_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_contest_desc)
                enableLights(true)
                enableVibration(true)
            }

            val rewardChannel = NotificationChannel(
                CHANNEL_ID_REWARD,
                context.getString(R.string.notification_channel_reward_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_reward_desc)
                enableLights(true)
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(generalChannel)
            notificationManager.createNotificationChannel(feedbackChannel)
            notificationManager.createNotificationChannel(contestChannel)
            notificationManager.createNotificationChannel(rewardChannel)
        }
    }

    fun getChannelIdByType(type: ENotificationType?): String {
        return when (type) {
            ENotificationType.NEW_CONTEST,
            ENotificationType.BLOCK_CONTEST,
            ENotificationType.ACCEPT_CONTEST,
            ENotificationType.NOT_APPROVAL_CONTEST -> CHANNEL_ID_CONTEST
            ENotificationType.REWARD -> CHANNEL_ID_REWARD
            ENotificationType.RECORD_FEEDBACK -> CHANNEL_ID_FEEDBACK
            else -> CHANNEL_ID_GENERAL
        }
    }

    fun createNotificationIntent(context: Context, data: Map<String, String>, notification: Notification? = null): Intent? {
        val type = data["type"]?.let {
            try {
                ENotificationType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
        val contestId = data[KEY_OBJECT_ID]?.toLongOrNull() ?: data["contestId"]?.toLongOrNull()
        val email = data[KEY_EMAIL]
        val recordId = data[KEY_RECORD_ID]?.toLongOrNull()
        val registrationId = data[KEY_REGISTRATION_ID]?.toLongOrNull()
        val feedbackId = data[KEY_FEEDBACK_ID]?.toLongOrNull()

        return when (type) {
            ENotificationType.ACCEPT_CONTEST,
            ENotificationType.NOT_APPROVAL_CONTEST -> {
                if (contestId != null) {
                    Intent(context, ManagementDetailsContestActivity::class.java).apply {
                        putExtra("contestId", contestId)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                } else null
            }
            ENotificationType.NEW_CONTEST,
            ENotificationType.BLOCK_CONTEST -> {
                if (contestId != null) {
                    Intent(context, ContestDetailsActivity::class.java).apply {
                        putExtra("contestId", contestId)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                } else null
            }
            ENotificationType.REWARD -> {
                if (email != null && contestId != null) {
                    Intent(context, RunnerRewardsActivity::class.java).apply {
                        putExtra(KEY_EMAIL, email)
                        putExtra("contestId", contestId)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                } else null
            }
            ENotificationType.RECORD_FEEDBACK -> {
                Intent(context, RecordFeedbackActivity::class.java).apply {
                    putExtra(KEY_NOTIFICATION_DATA, notification)
                    putExtra(KEY_FEEDBACK_ID, feedbackId)
                    putExtra(KEY_RECORD_ID, recordId)
                    putExtra(KEY_REGISTRATION_ID, registrationId)
                    putExtra(OPEN_FEEDBACK_TAB, true)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            }
            else -> {
                Intent(context, NotificationsActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    notification?.let { putExtra(KEY_NOTIFICATION_DATA, it) }
                }
            }
        }
    }

    fun showNotification(
        context: Context,
        title: String?,
        body: String?,
        data: Map<String, String>,
        notification: Notification? = null,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        val type = data["type"]?.let {
            try {
                ENotificationType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
        val channelId = getChannelIdByType(type)
        val intent = createNotificationIntent(context, data, notification)

        val pendingIntent = intent?.let {
            PendingIntent.getActivity(
                context,
                notificationId,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(getNotificationIcon(type))
            .setContentTitle(title ?: context.getString(R.string.app_name))
            .setContentText(body ?: context.getString(R.string.default_notification_body))
            .setAutoCancel(true)
            .setPriority(getNotificationPriority(type))
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setCategory(getNotificationCategory(type))

        pendingIntent?.let { notificationBuilder.setContentIntent(it) }
        addNotificationActions(context, notificationBuilder, type, data, notificationId)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun getNotificationIcon(type: ENotificationType?): Int {
        return when (type) {
            ENotificationType.REWARD -> R.drawable.ic_reward
            ENotificationType.NEW_CONTEST -> R.drawable.ic_contest
            ENotificationType.BLOCK_CONTEST -> R.drawable.ic_cancel
            ENotificationType.ACCEPT_CONTEST -> R.drawable.ic_completed
            ENotificationType.NOT_APPROVAL_CONTEST -> R.drawable.ic_cancel
            ENotificationType.RECORD_FEEDBACK -> R.drawable.ic_feedback
            else -> R.drawable.ic_notify
        }
    }

    private fun getNotificationPriority(type: ENotificationType?): Int {
        return when (type) {
            ENotificationType.REWARD,
            ENotificationType.ACCEPT_CONTEST,
            ENotificationType.NOT_APPROVAL_CONTEST,
            ENotificationType.RECORD_FEEDBACK -> NotificationCompat.PRIORITY_HIGH
            ENotificationType.NEW_CONTEST,
            ENotificationType.BLOCK_CONTEST -> NotificationCompat.PRIORITY_DEFAULT
            else -> NotificationCompat.PRIORITY_LOW
        }
    }

    private fun getNotificationCategory(type: ENotificationType?): String {
        return when (type) {
            ENotificationType.RECORD_FEEDBACK -> NotificationCompat.CATEGORY_MESSAGE
            ENotificationType.REWARD -> NotificationCompat.CATEGORY_EVENT
            else -> NotificationCompat.CATEGORY_STATUS
        }
    }

    private fun addNotificationActions(
        context: Context,
        builder: NotificationCompat.Builder,
        type: ENotificationType?,
        data: Map<String, String>,
        notificationId: Int
    ) {
        when (type) {
            ENotificationType.NEW_CONTEST -> {
                val viewIntent = createNotificationIntent(context, data)
                viewIntent?.let {
                    val viewPendingIntent = PendingIntent.getActivity(
                        context,
                        notificationId + 1,
                        it,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    builder.addAction(R.drawable.ic_add, context.getString(R.string.view_details), viewPendingIntent)
                }

                val dismissIntent = Intent().apply {
                    action = "DISMISS_NOTIFICATION"
                    putExtra(KEY_NOTIFICATION_ID, notificationId)
                }
                val dismissPendingIntent = PendingIntent.getBroadcast(
                    context,
                    notificationId + 2,
                    dismissIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(R.drawable.ic_previous, context.getString(R.string.dismiss), dismissPendingIntent)
            }
            ENotificationType.REWARD -> {
                val rewardIntent = createNotificationIntent(context, data)
                rewardIntent?.let {
                    val rewardPendingIntent = PendingIntent.getActivity(
                        context,
                        notificationId + 3,
                        it,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    builder.addAction(R.drawable.ic_reward, context.getString(R.string.view_reward), rewardPendingIntent)
                }
            }
            else -> {
                val allNotificationsIntent = Intent(context, NotificationsActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val allNotificationsPendingIntent = PendingIntent.getActivity(
                    context,
                    notificationId + 4,
                    allNotificationsIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(R.drawable.ic_notify, context.getString(R.string.view_all), allNotificationsPendingIntent)
            }
        }
    }

    fun cancelNotification(context: Context, notificationId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }

    fun cancelAllNotifications(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }
}