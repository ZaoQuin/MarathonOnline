package com.university.marathononline.firebase

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.university.marathononline.utils.KEY_NOTIFICATION_DATA
import com.university.marathononline.utils.KEY_NOTIFICATION_ID
import com.university.marathononline.utils.NOTIFICATION_READ
import com.university.marathononline.utils.NotificationUtils

class NotificationBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "NotificationReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "DISMISS_NOTIFICATION" -> {
                val notificationId = intent.getIntExtra(KEY_NOTIFICATION_ID, -1)
                if (notificationId != -1) {
                    NotificationUtils.cancelNotification(context, notificationId)
                    Log.d(TAG, "Notification dismissed: $notificationId")
                }
            }

            "MARK_AS_READ" -> {
                val notificationId = intent.getIntExtra(KEY_NOTIFICATION_ID, -1)
                val notificationDataId = intent.getLongExtra(KEY_NOTIFICATION_DATA, -1L)

                if (notificationId != -1) {
                    NotificationUtils.cancelNotification(context, notificationId)
                }

                if (notificationDataId != -1L) {
                    val updateIntent = Intent(NOTIFICATION_READ)
                    updateIntent.putExtra(KEY_NOTIFICATION_ID, notificationDataId)
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