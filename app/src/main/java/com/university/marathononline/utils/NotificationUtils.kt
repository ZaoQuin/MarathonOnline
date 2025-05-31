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
import com.university.marathononline.ui.view.activity.ContestDetailsActivity
import com.university.marathononline.ui.view.activity.ManagementDetailsContestActivity
import com.university.marathononline.ui.view.activity.NotificationsActivity
import com.university.marathononline.ui.view.activity.RunnerRewardsActivity

object NotificationUtils {

    const val CHANNEL_ID_GENERAL = "marathon_general"
    const val CHANNEL_ID_CONTEST = "marathon_contest"
    const val CHANNEL_ID_REWARD = "marathon_reward"

    const val CHANNEL_NAME_GENERAL = "Thông báo chung"
    const val CHANNEL_NAME_CONTEST = "Thông báo cuộc thi"
    const val CHANNEL_NAME_REWARD = "Thông báo giải thưởng"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // General notifications channel
            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                CHANNEL_NAME_GENERAL,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Thông báo chung về ứng dụng"
                enableLights(true)
                enableVibration(true)
            }

            // Contest notifications channel
            val contestChannel = NotificationChannel(
                CHANNEL_ID_CONTEST,
                CHANNEL_NAME_CONTEST,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo về cuộc thi marathon"
                enableLights(true)
                enableVibration(true)
            }

            // Reward notifications channel
            val rewardChannel = NotificationChannel(
                CHANNEL_ID_REWARD,
                CHANNEL_NAME_REWARD,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo về giải thưởng"
                enableLights(true)
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(generalChannel)
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

            else -> CHANNEL_ID_GENERAL
        }
    }

    fun createNotificationIntent(context: Context, data: Map<String, String>): Intent? {
        val type = data["type"]?.let { ENotificationType.valueOf(it) }
        val contestId = data["contestId"]?.toLongOrNull()
        val email = data["email"]

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

            else -> {
                Intent(context, NotificationsActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            }
        }
    }

    fun showNotification(
        context: Context,
        title: String?,
        body: String?,
        data: Map<String, String>,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        val type = data["type"]?.let { ENotificationType.valueOf(it) }
        val channelId = getChannelIdByType(type)

        val intent = createNotificationIntent(context, data)

        val pendingIntent = intent?.let {
            PendingIntent.getActivity(
                context,
                notificationId,
                it,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(getNotificationIcon(type))
            .setContentTitle(title ?: "Marathon Online")
            .setContentText(body ?: "Bạn có thông báo mới")
            .setAutoCancel(true)
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            .setPriority(getNotificationPriority(type))
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))

        pendingIntent?.let {
            notificationBuilder.setContentIntent(it)
        }

        // Add action buttons based on notification type
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
            else -> R.drawable.ic_notify
        }
    }

    private fun getNotificationPriority(type: ENotificationType?): Int {
        return when (type) {
            ENotificationType.REWARD,
            ENotificationType.ACCEPT_CONTEST,
            ENotificationType.NOT_APPROVAL_CONTEST -> NotificationCompat.PRIORITY_HIGH

            ENotificationType.NEW_CONTEST,
            ENotificationType.BLOCK_CONTEST -> NotificationCompat.PRIORITY_DEFAULT

            else -> NotificationCompat.PRIORITY_LOW
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
                // Add "Xem chi tiết" action
                val viewIntent = createNotificationIntent(context, data)
                viewIntent?.let {
                    val viewPendingIntent = PendingIntent.getActivity(
                        context,
                        notificationId + 1,
                        it,
                        PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                    )
                    builder.addAction(
                        R.drawable.ic_add,
                        "Xem chi tiết",
                        viewPendingIntent
                    )
                }

                // Add "Đóng" action
                val dismissIntent = Intent().apply {
                    action = "DISMISS_NOTIFICATION"
                    putExtra("notificationId", notificationId)
                }
                val dismissPendingIntent = PendingIntent.getBroadcast(
                    context,
                    notificationId + 2,
                    dismissIntent,
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(
                    R.drawable.ic_previous,
                    "Đóng",
                    dismissPendingIntent
                )
            }

            ENotificationType.REWARD -> {
                // Add "Xem giải thưởng" action
                val rewardIntent = createNotificationIntent(context, data)
                rewardIntent?.let {
                    val rewardPendingIntent = PendingIntent.getActivity(
                        context,
                        notificationId + 3,
                        it,
                        PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                    )
                    builder.addAction(
                        R.drawable.ic_reward,
                        "Xem giải thưởng",
                        rewardPendingIntent
                    )
                }
            }

            else -> {
                // Default action to open notifications list
                val allNotificationsIntent = Intent(context, NotificationsActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val allNotificationsPendingIntent = PendingIntent.getActivity(
                    context,
                    notificationId + 4,
                    allNotificationsIntent,
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(
                    R.drawable.ic_notify,
                    "Xem tất cả",
                    allNotificationsPendingIntent
                )
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