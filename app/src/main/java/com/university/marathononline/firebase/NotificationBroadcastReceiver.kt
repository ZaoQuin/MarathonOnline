package com.university.marathononline.firebase

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.university.marathononline.utils.NotificationUtils

class NotificationBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "NotificationReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "DISMISS_NOTIFICATION" -> {
                val notificationId = intent.getIntExtra("notificationId", -1)
                if (notificationId != -1) {
                    NotificationUtils.cancelNotification(context, notificationId)
                    Log.d(TAG, "Notification dismissed: $notificationId")
                }
            }

            "MARK_AS_READ" -> {
                val notificationId = intent.getIntExtra("notificationId", -1)
                val notificationDataId = intent.getLongExtra("notificationDataId", -1L)

                if (notificationId != -1) {
                    NotificationUtils.cancelNotification(context, notificationId)
                }

                if (notificationDataId != -1L) {
                    // Send broadcast to update UI
                    val updateIntent = Intent("com.university.marathononline.NOTIFICATION_READ")
                    updateIntent.putExtra("notificationId", notificationDataId)
                    context.sendBroadcast(updateIntent)

                    Log.d(TAG, "Notification marked as read: $notificationDataId")
                }
            }

            "CLEAR_ALL_NOTIFICATIONS" -> {
                NotificationUtils.cancelAllNotifications(context)
                Log.d(TAG, "All notifications cleared")
            }
        }
    }
}