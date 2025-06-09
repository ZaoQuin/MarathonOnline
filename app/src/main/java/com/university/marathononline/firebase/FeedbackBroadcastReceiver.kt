package com.university.marathononline.firebase

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.university.marathononline.utils.KEY_NOTIFICATION

class FeedbackBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "FeedbackReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            MyFirebaseMessagingService.ACTION_NEW_FEEDBACK -> {
                handleNewFeedback(context, intent)
            }
        }
    }

    private fun handleNewFeedback(context: Context, intent: Intent) {
        try {
            val notification = intent.getParcelableExtra<Notification>(KEY_NOTIFICATION)
            val feedbackId = intent.getLongExtra("feedbackId", -1L)
            val recordId = intent.getLongExtra("recordId", -1L)
            val feedbackType = intent.getStringExtra("feedbackType")

            Log.d(TAG, "Received feedback notification: feedbackId=$feedbackId, recordId=$recordId, type=$feedbackType")

            // Hiển thị toast hoặc snackbar
            when (feedbackType) {
                "ADMIN_FEEDBACK" -> {
                    showFeedbackToast(context, "Admin đã phản hồi về record của bạn", recordId)
                }
                "RUNNER_FEEDBACK" -> {
                    showFeedbackToast(context, "Có phản hồi mới từ runner", recordId)
                }
            }

            // Cập nhật UI nếu cần
            updateFeedbackUI(context, feedbackId, recordId, feedbackType)

        } catch (e: Exception) {
            Log.e(TAG, "Error handling feedback broadcast: ${e.message}")
        }
    }

    private fun showFeedbackToast(context: Context, message: String, recordId: Long?) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()

        // Gửi broadcast để MainActivity hoặc các activity khác có thể hiển thị dialog
        recordId?.let {
            val showDialogIntent = Intent("com.university.marathononline.SHOW_FEEDBACK_DIALOG")
            showDialogIntent.putExtra("recordId", it)
            showDialogIntent.putExtra("message", message)
            LocalBroadcastManager.getInstance(context).sendBroadcast(showDialogIntent)
        }
    }

    private fun updateFeedbackUI(context: Context, feedbackId: Long?, recordId: Long?, feedbackType: String?) {
        // Cập nhật UI nếu đang hiển thị record detail hoặc feedback list
        // Gửi broadcast để các fragment/activity khác cập nhật
        val updateIntent = Intent("com.university.marathononline.FEEDBACK_UPDATED").apply {
            putExtra("feedbackId", feedbackId)
            putExtra("recordId", recordId)
            putExtra("feedbackType", feedbackType)
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(updateIntent)
    }
}