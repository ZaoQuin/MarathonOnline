package com.university.marathononline.ui.view.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.messaging.FirebaseMessaging
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.notify.NotificationApiService
import com.university.marathononline.data.models.Notification
import com.university.marathononline.data.repository.NotificationRepository
import com.university.marathononline.databinding.ActivityNotificationsBinding
import com.university.marathononline.ui.adapter.NotifyAdapter
import com.university.marathononline.ui.viewModel.NotifyViewModel
import com.university.marathononline.utils.KEY_NOTIFICATIONS
import com.university.marathononline.utils.KEY_NOTIFICATION_DATA
import com.university.marathononline.utils.visible
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class NotificationsActivity: BaseActivity<NotifyViewModel, ActivityNotificationsBinding>(){

    private lateinit var adapter: NotifyAdapter
    private lateinit var notificationReceiver: BroadcastReceiver

    companion object {
        private const val TAG = "NotificationsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIntentExtras(intent)
        setupAdapter()
        setupNotificationReceiver()
        observeViewModel()

        // Load notifications from server
        viewModel.getNotifications()
    }

    private fun setupAdapter(){
        binding.notifies.layoutManager = LinearLayoutManager(this)
        adapter = NotifyAdapter(emptyList(), setRead = { notify ->
            // Cập nhật UI ngay lập tức
            notify.isRead = true
            val updatedNotifications = adapter.getCurrentData().map {
                if(it.id == notify.id) notify else it
            }
            adapter.updateData(updatedNotifications)

            // Gọi API để cập nhật trạng thái đã đọc
            viewModel.setRead(notify)
        })
        binding.notifies.adapter = adapter
    }

    private fun setupNotificationReceiver() {
        notificationReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.getSerializableExtra(KEY_NOTIFICATION_DATA)?.let { notification ->
                    if (notification is Notification) {
                        // Thêm notification mới vào đầu danh sách
                        val currentNotifications = adapter.getCurrentData().toMutableList()
                        currentNotifications.add(0, notification)
                        adapter.updateData(currentNotifications)

                        // Cập nhật empty state
                        binding.empty.visible(currentNotifications.isEmpty())
                    }
                }
            }
        }

        // Register broadcast receiver
        val filter = IntentFilter("com.university.marathononline.NEW_NOTIFICATION")

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(notificationReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(notificationReceiver, filter)
        }
    }

    private fun handleIntentExtras(intent: Intent) {
        intent.apply {
            viewModel.apply {
                (getSerializableExtra(KEY_NOTIFICATIONS) as? List<Notification>)?.let {
                    setNotifications(it)
                }
            }
        }
    }

    private fun observeViewModel(){
        // Observe notifications list
        viewModel.notifies.observe(this) { notifies: List<Notification> ->
            binding.empty.visible(notifies.isEmpty())
            adapter.updateData(notifies)
        }

        // Observe get notifications response
        viewModel.getNotificationResponse.observe(this) {
            when (it) {
                is Resource.Loading -> {
                    // Show loading state
//                    binding.progressBar?.visible(true)
                }
                is Resource.Success -> {
//                    binding.progressBar?.visible(false)
                    viewModel.setNotifications(it.value)
                }
                is Resource.Failure -> {
//                    binding.progressBar?.visible(false)
                    Log.e(TAG, "Error loading notifications")
                    // Show error message to user
                }
            }
        }

        // Observe set read response
        viewModel.setReadResponse.observe(this) { resource ->
            when (resource) {
                is com.university.marathononline.data.api.Resource.Success -> {
                    Log.d(TAG, "Notification marked as read successfully")
                }
                is com.university.marathononline.data.api.Resource.Failure -> {
                    Log.e(TAG, "Error marking notification as read")
                }
                else -> {
                    // Loading state - không cần xử lý gì
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(notificationReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver: ${e.message}")
        }
    }

//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
//        // Xử lý khi activity được mở từ notification
//        intent?.let { handleIntentExtras(it) }
//    }

    override fun getViewModel(): Class<NotifyViewModel> = NotifyViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) = ActivityNotificationsBinding.inflate(inflater)

    override fun getActivityRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val api = retrofitInstance.buildApi(NotificationApiService::class.java, token)
        return listOf(NotificationRepository(api))
    }
}