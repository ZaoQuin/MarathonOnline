package com.university.marathononline.ui.view.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.university.marathononline.R
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.notify.NotificationApiService
import com.university.marathononline.data.models.Notification
import com.university.marathononline.data.repository.NotificationRepository
import com.university.marathononline.databinding.ActivityNotificationsBinding
import com.university.marathononline.ui.adapter.NotifyAdapter
import com.university.marathononline.ui.viewModel.NotifyViewModel
import com.university.marathononline.utils.ACTION_NEW_FEEDBACK
import com.university.marathononline.utils.ACTION_NEW_NOTIFICATION
import com.university.marathononline.utils.KEY_NOTIFICATION_DATA
import com.university.marathononline.utils.finishAndGoBack
import com.university.marathononline.utils.visible
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*

class NotificationsActivity : BaseActivity<NotifyViewModel, ActivityNotificationsBinding>() {

    private lateinit var adapter: NotifyAdapter
    private lateinit var notificationReceiver: BroadcastReceiver
    private lateinit var feedbackReceiver: BroadcastReceiver

    companion object {
        private const val TAG = "NotificationsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntentExtras(intent)
        setupAdapter()
        setupButton()
        setupBroadcastReceivers()
        observeViewModel()
        viewModel.getNotifications()
    }

    private fun setupAdapter() {
        binding.notifies.apply {
            layoutManager = LinearLayoutManager(this@NotificationsActivity)
            itemAnimator = DefaultItemAnimator().apply {
                addDuration = 300
                removeDuration = 300
                moveDuration = 300
                changeDuration = 300
            }
        }

        adapter = NotifyAdapter(emptyList()) { notification ->
            notification.isRead = true
            val position = adapter.getCurrentData().indexOfFirst { it.id == notification.id }
            if (position != -1) {
                adapter.notifyItemChanged(position)
            }
            viewModel.setNotifications(adapter.getCurrentData())
            viewModel.setRead(notification)
        }

        binding.notifies.adapter = adapter
    }

    private fun setupButton(){
        binding.btnBack.setOnClickListener {
            finishAndGoBack()
        }
    }

    private fun setupBroadcastReceivers() {
        notificationReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d(TAG, "Received notification broadcast: ${intent?.action}")
                handleNewNotification(intent)
            }
        }

        feedbackReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d(TAG, "Received feedback broadcast: ${intent?.action}")
                handleNewNotification(intent)
            }
        }

        LocalBroadcastManager.getInstance(this).apply {
            registerReceiver(notificationReceiver, IntentFilter(ACTION_NEW_NOTIFICATION))
            registerReceiver(feedbackReceiver, IntentFilter(ACTION_NEW_FEEDBACK))
        }
        Log.d(TAG, "Broadcast receivers registered successfully")
    }

    private fun handleNewNotification(intent: Intent?) {
        if (intent == null) {
            Log.w(TAG, "Received null intent in handleNewNotification")
            return
        }

        val notification = intent.getSerializableExtra(KEY_NOTIFICATION_DATA) as? Notification
        if (notification == null) {
            Log.w(TAG, "No valid notification data found in intent")
            return
        }

        Log.d(TAG, "Processing notification - ID: ${notification.id}, Title: '${notification.title}'")

        if (!isValidNotification(notification)) {
            Log.w(TAG, "Invalid notification data received, skipping")
            return
        }

        val processedNotification = ensureNotificationHasTimestamp(notification)
        updateNotificationsList(processedNotification)
    }

    private fun updateNotificationsList(notification: Notification) {
        try {
            val currentNotifications = adapter.getCurrentData().toMutableList()
            val existingIndex = currentNotifications.indexOfFirst { it.id == notification.id }

            if (existingIndex == -1) {
                Log.d(TAG, "Adding new notification to list at position 0")
                currentNotifications.add(0, notification)
                adapter.updateData(currentNotifications)
                viewModel.setNotifications(currentNotifications)
                updateEmptyState(currentNotifications.isEmpty())

                runOnUiThread {
                    binding.notifies.smoothScrollToPosition(0)
                    binding.notifies.post {
                        binding.notifies.findViewHolderForAdapterPosition(0)?.itemView?.let { view ->
                            try {
                                val slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_top)
                                view.startAnimation(slideIn)
                            } catch (e: Exception) {
                                Log.w(TAG, "Error applying animation: ${e.message}", e)
                            }
                        }
                    }
                }
            } else {
                Log.d(TAG, "Updating existing notification at position $existingIndex")
                currentNotifications[existingIndex] = notification
                adapter.updateData(currentNotifications)
                viewModel.setNotifications(currentNotifications)
                runOnUiThread { adapter.notifyItemChanged(existingIndex) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating notifications list: ${e.message}", e)
            showErrorState(getString(R.string.error_updating_notifications))
        }
    }

    private fun isValidNotification(notification: Notification): Boolean {
        val isValid = notification.id != null && notification.id!! > 0 && !notification.title.isNullOrBlank()
        Log.d(TAG, "Notification validation - ID: ${notification.id}, Title: '${notification.title}', Valid: $isValid")
        return isValid
    }

    private fun ensureNotificationHasTimestamp(notification: Notification): Notification {
        return if (notification.createAt.isNullOrBlank()) {
            Log.w(TAG, "Notification ${notification.id} has no timestamp, setting current timestamp")
            notification.copy(createAt = getCurrentTimestamp())
        } else {
            notification
        }
    }

    private fun getCurrentTimestamp(): String {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
    }

    private fun handleIntentExtras(intent: Intent?) {
        Log.d(TAG, "Handling intent extras")
        intent?.getSerializableExtra(KEY_NOTIFICATION_DATA)?.let { data ->
            if (data is Notification && isValidNotification(data)) {
                Log.d(TAG, "Opened from notification: ID=${data.id}, Title='${data.title}'")
                val processedNotification = ensureNotificationHasTimestamp(data).apply { isRead = true }
                viewModel.setNotifications(listOf(processedNotification))
                viewModel.setRead(processedNotification)
            } else {
                Log.w(TAG, "Invalid notification in intent extras: $data")
            }
        }
    }

    private fun observeViewModel() {
        viewModel.notifies.observe(this) { notifications ->
            Log.d(TAG, "Received ${notifications.size} notifications from ViewModel")
            val validNotifications = notifications.filter { isValidNotification(it) }
                .map { ensureNotificationHasTimestamp(it) }
            adapter.updateData(validNotifications)
            updateEmptyState(validNotifications.isEmpty())
        }

        viewModel.getNotificationResponse.observe(this) { resource ->
            showLoadingState(resource == Resource.Loading)
            when (resource) {
                is Resource.Loading -> {
                    Log.d(TAG, "Loading notifications from server")
                }
                is Resource.Success -> {
                    Log.d(TAG, "Successfully loaded ${resource.value.size} notifications")
                    val validNotifications = resource.value.filter { isValidNotification(it) }
                        .map { ensureNotificationHasTimestamp(it) }
                    viewModel.setNotifications(validNotifications)
                }
                is Resource.Failure -> {
                    showErrorState(getString(R.string.error_loading_notifications))
                }
            }
        }

        viewModel.setReadResponse.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> Log.d(TAG, "Notification marked as read successfully")
                is Resource.Failure -> {
                    showErrorState(getString(R.string.error_marking_read))
                }
                else -> Unit
            }
        }
    }

    private fun showLoadingState(isLoading: Boolean) {
        binding.loadingLayout.visible(isLoading)
        binding.notifies.visible(!isLoading && adapter.getCurrentData().isNotEmpty())
        binding.emptyLayout.visible(!isLoading && adapter.getCurrentData().isEmpty())
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyLayout.visible(isEmpty)
        binding.notifies.visible(!isEmpty)
    }

    private fun showErrorState(errorMessage: String) {
        binding.empty.text = errorMessage
        updateEmptyState(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            LocalBroadcastManager.getInstance(this).apply {
                unregisterReceiver(notificationReceiver)
                unregisterReceiver(feedbackReceiver)
            }
            Log.d(TAG, "Broadcast receivers unregistered successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receivers: ${e.message}", e)
        }
    }

    override fun getViewModel(): Class<NotifyViewModel> = NotifyViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater): ActivityNotificationsBinding =
        ActivityNotificationsBinding.inflate(inflater)


    override fun getActivityRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val api = retrofitInstance.buildApi(NotificationApiService::class.java, token)
        return listOf(NotificationRepository(api))
    }
}