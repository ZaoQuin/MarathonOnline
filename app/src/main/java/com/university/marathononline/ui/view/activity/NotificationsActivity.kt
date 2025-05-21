package com.university.marathononline.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.notify.NotificationApiService
import com.university.marathononline.data.models.Notification
import com.university.marathononline.data.repository.NotificationRepository
import com.university.marathononline.databinding.ActivityNotificationsBinding
import com.university.marathononline.ui.adapter.NotifyAdapter
import com.university.marathononline.ui.viewModel.NotifyViewModel
import com.university.marathononline.utils.KEY_NOTIFICATIONS
import com.university.marathononline.utils.visible
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class NotificationsActivity: BaseActivity<NotifyViewModel, ActivityNotificationsBinding>(){

    private lateinit var adapter: NotifyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIntentExtras(intent)
        setupAdapter()

        observeViewModel()
    }

    private fun setupAdapter(){
        binding.notifies.layoutManager = LinearLayoutManager(this)
        adapter = NotifyAdapter(emptyList(), setRead = { notify ->
            notify.isRead = true
            val updatedNotifications = adapter.getCurrentData().map {
                if(it.id == notify.id) notify else it
            }
            adapter.updateData(updatedNotifications)
        })
        binding.notifies.adapter = adapter
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
        viewModel.notifies.observe(this) { notifies: List<Notification> ->
            binding.empty.visible(notifies.size == 0)
            adapter.updateData(notifies)
        }
    }

    override fun getViewModel(): Class<NotifyViewModel> = NotifyViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) = ActivityNotificationsBinding.inflate(inflater)

    override fun getActivityRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val api = retrofitInstance.buildApi(NotificationApiService::class.java )
        return listOf(NotificationRepository(api))
    }
}