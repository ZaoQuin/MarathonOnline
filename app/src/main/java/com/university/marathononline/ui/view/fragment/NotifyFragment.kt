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
import com.university.marathononline.data.repository.NotificationRepository
import com.university.marathononline.ui.adapter.NotifyAdapter
import com.university.marathononline.ui.viewModel.NotifyViewModel
import com.university.marathononline.utils.visible
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class NotifyFragment : BaseFragment<NotifyViewModel, FragmentNotifyBinding>() {

    private lateinit var adapter: NotifyAdapter

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
        viewModel.getNotifications()

        binding.notifies.layoutManager = LinearLayoutManager(requireContext())
        adapter = NotifyAdapter(emptyList())
        binding.notifies.adapter = adapter

        observe()
    }

    private fun observe() {
        viewModel.getNotificationResponse.observe(viewLifecycleOwner){
            when(it){
                is Resource.Success -> {
                    binding.empty.visible(it.value.size == 0)
                    Log.d("NotificationFrag", it.value.toString())
                    viewModel.setNotification(it.value)
                }
                is Resource.Failure -> {handleApiError(it)
                }
                else -> Unit
            }
        }

        viewModel.notifies.observe(viewLifecycleOwner) { notifies: List<Notification> ->
            adapter.updateData(notifies)
        }
    }
}
