package com.university.marathononline.ui.view.activity

import com.university.marathononline.utils.KEY_NOTIFICATIONS
import com.university.marathononline.utils.KEY_NOTIFICATION_DATA
import com.university.marathononline.utils.visible
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
import com.university.marathononline.firebase.MyFirebaseMessagingService
import com.university.marathononline.ui.adapter.NotifyAdapter
import com.university.marathononline.ui.viewModel.NotifyViewModel
import java.text.SimpleDateFormat
import java.util.*

class NotificationsActivity: BaseActivity<NotifyViewModel, ActivityNotificationsBinding>() {

    private lateinit var adapter: NotifyAdapter
    private lateinit var notificationReceiver: BroadcastReceiver
    private lateinit var localNotificationReceiver: BroadcastReceiver

    companion object {
        private const val TAG = "NotificationsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar()
        handleIntentExtras(intent)
        setupAdapter()
        setupNotificationReceivers()
        setupFabRefresh()
        observeViewModel()

        // Show loading and load notifications from server
        showLoadingState(true)
        viewModel.getNotifications()
    }

    private fun setupToolbar() {
//        binding.toolbar.setNavigationOnClickListener {
//            onBackPressed()
//        }
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

        adapter = NotifyAdapter(emptyList(), setRead = { notify ->
            // Cập nhật UI ngay lập tức với animation
            val originalIsRead = notify.isRead
            notify.isRead = true

            val updatedNotifications = adapter.getCurrentData().map {
                if (it.id == notify.id) notify else it
            }

            // Animate the change
            adapter.updateData(updatedNotifications)
            viewModel.setNotifications(updatedNotifications)

            // Gọi API để cập nhật trạng thái đã đọc
            viewModel.setRead(notify)
        })

        binding.notifies.adapter = adapter
    }

    private fun setupNotificationReceivers() {
        notificationReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                handleNewNotification(intent)
            }
        }

        localNotificationReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                handleNewNotification(intent)
            }
        }

        // Register receivers
        val filter = IntentFilter(MyFirebaseMessagingService.ACTION_NEW_NOTIFICATION)

        // Register local broadcast receiver
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(localNotificationReceiver, filter)

        // Register global broadcast receiver
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(notificationReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(notificationReceiver, filter)
        }
    }

    private fun setupFabRefresh() {
        binding.fabRefresh.setOnClickListener {
            showLoadingState(true)
            viewModel.getNotifications()

            // Hide FAB temporarily
            binding.fabRefresh.visible(false)
        }
    }

    private fun handleNewNotification(intent: Intent?) {
        Log.d(TAG, "handleNewNotification called")

        if (intent == null) {
            Log.w(TAG, "Intent is null in handleNewNotification")
            return
        }

        // Log all extras for debugging
        val extras = intent.extras
        if (extras != null) {
            Log.d(TAG, "Intent extras:")
            for (key in extras.keySet()) {
                val value = extras.get(key)
                Log.d(TAG, "  $key: $value (${value?.javaClass?.simpleName})")
            }
        }

        intent.getSerializableExtra(KEY_NOTIFICATION_DATA)?.let { notificationData ->
            Log.d(TAG, "Raw notification data: $notificationData")
            Log.d(TAG, "Notification data class: ${notificationData.javaClass.simpleName}")

            if (notificationData is Notification) {
                Log.d(TAG, "Processing notification - ID: ${notificationData.id}, Title: '${notificationData.title}', Body: '${notificationData.content}'")

                // Validate notification data
                if (!isValidNotification(notificationData)) {
                    Log.w(TAG, "Invalid notification data received, skipping")
                    return
                }

                // Fix: Ensure notification has a valid createAt timestamp
                val processedNotification = ensureNotificationHasTimestamp(notificationData)

                val currentNotifications = adapter.getCurrentData().toMutableList()

                val existingIndex = currentNotifications.indexOfFirst { it.id == processedNotification.id }
                if (existingIndex == -1) {
                    Log.d(TAG, "Adding new notification to list")
                    currentNotifications.add(0, processedNotification)
                    adapter.updateData(currentNotifications)
                    viewModel.setNotifications(currentNotifications)

                    updateEmptyState(currentNotifications.isEmpty())

                    // Smooth scroll to top with animation
                    binding.notifies.smoothScrollToPosition(0)

                    // Show new notification with slide animation
                    try {
                        val slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_top)
                        binding.notifies.findViewHolderForAdapterPosition(0)?.itemView?.startAnimation(slideIn)
                    } catch (e: Exception) {
                        Log.w(TAG, "Error applying animation: ${e.message}")
                    }
                } else {
                    Log.d(TAG, "Notification already exists in list, skipping")
                }
            } else {
                Log.w(TAG, "Notification data is not of type Notification: ${notificationData.javaClass}")
            }
        } ?: run {
            Log.w(TAG, "No notification data found in intent")
        }
    }

    private fun isValidNotification(notification: Notification): Boolean {
        // Check if essential fields are not null/empty
        val isValid = !notification.title.isNullOrBlank() && notification.id != null

        if (!isValid) {
            Log.w(TAG, "Invalid notification - ID: ${notification.id}, Title: '${notification.title}'")
        }

        return isValid
    }

    private fun ensureNotificationHasTimestamp(notification: Notification): Notification {
        return if (notification.createAt.isNullOrBlank()) {
            Log.w(TAG, "Notification ${notification.id} has null/empty createAt, setting current timestamp")
            // Create a copy with current timestamp
            notification.apply {
                createAt = getCurrentTimestamp()
            }
        } else {
            notification
        }
    }

    private fun getCurrentTimestamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun handleIntentExtras(intent: Intent) {
        Log.d(TAG, "Handling intent extras")

        intent.apply {
            viewModel.apply {
                (getSerializableExtra(KEY_NOTIFICATIONS) as? List<Notification>)?.let { notifications ->
                    Log.d(TAG, "Found ${notifications.size} notifications in intent")
                    // Fix: Process all notifications to ensure they have timestamps and are valid
                    val processedNotifications = notifications.mapNotNull { notification ->
                        if (isValidNotification(notification)) {
                            ensureNotificationHasTimestamp(notification)
                        } else {
                            Log.w(TAG, "Skipping invalid notification: ${notification.id}")
                            null
                        }
                    }
                    setNotifications(processedNotifications)
                }

                // Xử lý khi mở từ system notification
                (getSerializableExtra(KEY_NOTIFICATION_DATA) as? Notification)?.let { notification ->
                    Log.d(TAG, "Opened from notification: ID=${notification.id}, Title='${notification.title}'")
                    if (isValidNotification(notification)) {
                        val processedNotification = ensureNotificationHasTimestamp(notification)
                        // Có thể scroll đến notification này hoặc highlight nó
                        Log.d(TAG, "Processed notification from intent")
                    } else {
                        Log.w(TAG, "Invalid notification in intent extras")
                    }
                }
            }
        }
    }

    private fun observeViewModel() {
        // Observe notifications list
        viewModel.notifies.observe(this) { notifies: List<Notification> ->
            Log.d(TAG, "Received ${notifies.size} notifications from ViewModel")
            showLoadingState(false)
            updateEmptyState(notifies.isEmpty())

            // Fix: Process and validate notifications before passing to adapter
            val processedNotifications = notifies.mapNotNull { notification ->
                if (isValidNotification(notification)) {
                    ensureNotificationHasTimestamp(notification)
                } else {
                    Log.w(TAG, "Filtering out invalid notification: ${notification.id}")
                    null
                }
            }

            Log.d(TAG, "Processed ${processedNotifications.size} valid notifications")
            adapter.updateData(processedNotifications)

            // Show FAB if there are notifications
            if (processedNotifications.isNotEmpty()) {
                binding.fabRefresh.visible(true)
            }
        }

        // Observe get notifications response
        viewModel.getNotificationResponse.observe(this) {
            when (it) {
                is Resource.Loading -> {
                    Log.d(TAG, "Loading notifications from server")
                    showLoadingState(true)
                }

                is Resource.Success -> {
                    Log.d(TAG, "Successfully loaded ${it.value.size} notifications from server")
                    showLoadingState(false)
                    // Fix: Process and validate notifications before setting them
                    val processedNotifications = it.value.mapNotNull { notification ->
                        if (isValidNotification(notification)) {
                            ensureNotificationHasTimestamp(notification)
                        } else {
                            Log.w(TAG, "Filtering out invalid notification from server: ${notification.id}")
                            null
                        }
                    }
                    Log.d(TAG, "Setting ${processedNotifications.size} valid notifications")
                    viewModel.setNotifications(processedNotifications)
                }

                is Resource.Failure -> {
                    Log.e(TAG, "Error loading notifications: ${it.errorMessage}")
                    showLoadingState(false)

                    // Show error state or retry option
                    showErrorState(it.errorMessage ?: "Có lỗi xảy ra")
                }
            }
        }

        // Observe set read response
        viewModel.setReadResponse.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> {
                    Log.d(TAG, "Notification marked as read successfully")
                }

                is Resource.Failure -> {
                    Log.e(TAG, "Error marking notification as read: ${resource.errorMessage}")
                    // Rollback UI state nếu cần
                }

                else -> {
                    // Loading state
                }
            }
        }
    }

    private fun showLoadingState(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingLayout.visible(true)
            binding.notifies.visible(false)
            binding.emptyLayout.visible(false)
            binding.fabRefresh.visible(false)
        } else {
            binding.loadingLayout.visible(false)
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.emptyLayout.visible(true)
            binding.notifies.visible(false)
            binding.fabRefresh.visible(false)
        } else {
            binding.emptyLayout.visible(false)
            binding.notifies.visible(true)
            binding.fabRefresh.visible(true)
        }
    }

    private fun showErrorState(errorMessage: String) {
        // You can implement a custom error state here
        // For now, we'll show the empty state with error message
        binding.empty.text = "Lỗi: $errorMessage"
        updateEmptyState(true)
        binding.fabRefresh.visible(true) // Show refresh button on error
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(notificationReceiver)
            LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(localNotificationReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receivers: ${e.message}")
        }
    }

    override fun getViewModel(): Class<NotifyViewModel> = NotifyViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) =
        ActivityNotificationsBinding.inflate(inflater)

    override fun getActivityRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val api = retrofitInstance.buildApi(NotificationApiService::class.java, token)
        return listOf(NotificationRepository(api))
    }
}