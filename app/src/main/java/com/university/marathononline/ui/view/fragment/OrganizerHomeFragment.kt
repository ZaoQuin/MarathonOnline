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
import com.university.marathononline.data.models.Notification
import com.university.marathononline.data.repository.NotificationRepository
import com.university.marathononline.databinding.FragmentOrganizerHomeBinding
import com.university.marathononline.ui.adapter.NotifyAdapter
import com.university.marathononline.ui.viewModel.NotifyViewModel
import com.university.marathononline.utils.visible
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class OrganizerHomeFragment : BaseFragment<NotifyViewModel, FragmentOrganizerHomeBinding>() {

    private lateinit var adapter: NotifyAdapter

    override fun getViewModel(): Class<NotifyViewModel> = NotifyViewModel::class.java

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentOrganizerHomeBinding {
        return FragmentOrganizerHomeBinding.inflate(inflater, container, false)
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

        binding.userFullNameText.text = runBlocking { userPreferences.fullName.first() }

        binding.notifies.layoutManager = LinearLayoutManager(requireContext())
        adapter = NotifyAdapter(emptyList(), setRead = { notify ->
            notify.isRead = true
            val updatedNotifications = adapter.getCurrentData().map {
                if (it.id == notify.id) notify else it
            }
            adapter.updateData(updatedNotifications)
            setRead(notify)
        })
        binding.notifies.adapter = adapter

        viewModel.getNotifications()

        observe()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getNotifications()
    }

    private fun setRead(notify: Notification){
        viewModel.setRead(notify)
    }


    private fun observe() {
        viewModel.getNotificationResponse.observe(viewLifecycleOwner){
            when(it){
                is Resource.Success -> {
                    binding.empty.visible(it.value.size == 0)
                    Log.d("NotificationFrag", it.value.toString())
                    viewModel.setNotifications(it.value)
                }
                is Resource.Failure -> handleApiError(it)
                else -> Unit
            }
        }

        viewModel.setReadResponse.observe(viewLifecycleOwner){ resource ->
            when(resource){
                is Resource.Success -> {
                    val updatedNotifications = adapter.getCurrentData().map {
                        if (it.id == resource.value.id) resource.value else it
                    }
                    adapter.updateData(updatedNotifications)
                }
                is Resource.Failure -> handleApiError(resource)
                else -> Unit
            }
        }

        viewModel.notifies.observe(viewLifecycleOwner) { notifies: List<Notification> ->
            adapter.updateData(notifies)
        }
    }
}
