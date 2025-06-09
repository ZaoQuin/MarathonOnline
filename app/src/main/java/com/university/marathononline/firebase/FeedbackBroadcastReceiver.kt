package com.university.marathononline.firebase

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.university.marathononline.utils.ACTION_NEW_FEEDBACK
import com.university.marathononline.utils.FEEDBACK_UPDATED
import com.university.marathononline.utils.KEY_FEEDBACK_ID
import com.university.marathononline.utils.KEY_FEEDBACK_TYPE
import com.university.marathononline.utils.KEY_MESSAGE
import com.university.marathononline.utils.KEY_NOTIFICATION
import com.university.marathononline.utils.KEY_RECORD_ID
import com.university.marathononline.utils.KEY_REGISTRATION_ID
import com.university.marathononline.utils.SHOW_FEEDBACK_DIALOG

class FeedbackBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "FeedbackReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_NEW_FEEDBACK -> {
                handleNewFeedback(context, intent)
            }
        }
    }

    private fun handleNewFeedback(context: Context, intent: Intent) {
        try {
            val notification = intent.getParcelableExtra<Notification>(KEY_NOTIFICATION)
            val feedbackId = intent.getLongExtra(KEY_FEEDBACK_ID, -1L)
            val recordId = intent.getLongExtra(KEY_RECORD_ID, -1L)
            val registrationId = intent.getLongExtra(KEY_REGISTRATION_ID, -1L)
            val feedbackType = intent.getStringExtra(KEY_FEEDBACK_TYPE)

            updateFeedbackUI(context, feedbackId, recordId, registrationId, feedbackType)

        } catch (e: Exception) {
            Log.e(TAG, "Error handling feedback broadcast: ${e.message}")
        }
    }

    private fun showFeedbackToast(context: Context, message: String, recordId: Long?) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()

        recordId?.let {
            val showDialogIntent = Intent(SHOW_FEEDBACK_DIALOG)
            showDialogIntent.putExtra(KEY_RECORD_ID, it)
            showDialogIntent.putExtra(KEY_MESSAGE, message)
            LocalBroadcastManager.getInstance(context).sendBroadcast(showDialogIntent)
        }
    }

    private fun updateFeedbackUI(context: Context, feedbackId: Long?, recordId: Long?, registrationId: Long?, feedbackType: String?) {
        val updateIntent = Intent(FEEDBACK_UPDATED).apply {
            putExtra(KEY_FEEDBACK_ID, feedbackId)
            putExtra(KEY_RECORD_ID, recordId)
            putExtra(KEY_REGISTRATION_ID, registrationId)
            putExtra(KEY_FEEDBACK_TYPE, feedbackType)
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(updateIntent)
    }
}