package com.university.marathononline.ui.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.university.marathononline.base.BaseFragment
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.notify.NotificationApiService
import com.university.marathononline.databinding.FragmentNotifyBinding
import com.university.marathononline.data.models.Notification
import com.university.marathononline.data.models.Race
import com.university.marathononline.data.models.User
import com.university.marathononline.data.repository.NotificationRepository
import com.university.marathononline.ui.adapter.NotifyAdapter
import com.university.marathononline.ui.view.activity.MainActivity
import com.university.marathononline.ui.viewModel.NotifyViewModel
import com.university.marathononline.utils.KEY_NOTIFICATIONS
import com.university.marathononline.utils.KEY_RACES
import com.university.marathononline.utils.KEY_USER
import com.university.marathononline.utils.visible
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class NotifyFragment : BaseFragment<NotifyViewModel, FragmentNotifyBinding>() {

    private lateinit var adapter: NotifyAdapter
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.getNotifications()
    }

    override fun getViewModel(): Class<NotifyViewModel> = NotifyViewModel::class.java

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentNotifyBinding {
        return FragmentNotifyBinding.inflate(inflater, container, false)
    }

    override fun getFragmentRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val api = retrofitInstance.buildApi(NotificationApiService::class.java, token)
        return listOf(
            NotificationRepository(api)
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (arguments?.getSerializable(KEY_NOTIFICATIONS) as? List<Notification>)?.let { viewModel.setNotification(it)}

        binding.notifies.layoutManager = LinearLayoutManager(requireContext())
        adapter = NotifyAdapter(emptyList(), setRead = { notify ->
            notify.isRead = true
            val updatedNotifications = adapter.getCurrentData().map {
                if (it.id == notify.id) notify else it
            }
            adapter.updateData(updatedNotifications)
            val unreadCount = updatedNotifications.filter { notification -> !notification.isRead!! }
            mainActivity.updateCountNotification(unreadCount.size)
            setRead(notify)
        })
        binding.notifies.adapter = adapter

        observe()
    }

    private fun setRead(notify: Notification){
        viewModel.setRead(notify)
    }

    private fun observe() {

        mainActivity = (requireActivity() as MainActivity)

        viewModel.getNotificationResponse.observe(viewLifecycleOwner){
            when(it){
                is Resource.Success -> {
                    viewModel.setNotification(it.value)
                    Log.d("NotifyFragment", "Number of notifications: ${it.value.size}")
                    val unreadCount = it.value.filter { notification -> !notification.isRead!! }
                    mainActivity.updateCountNotification(unreadCount.size)
                }
                is Resource.Failure -> {handleApiError(it)
                }
                else -> Unit
            }
        }

        viewModel.notifies.observe(viewLifecycleOwner) { notifies: List<Notification> ->
            binding.empty.visible(notifies.size == 0)
            Log.d("NotificationFrag", notifies.toString())
            adapter.updateData(notifies)
        }
    }
}
