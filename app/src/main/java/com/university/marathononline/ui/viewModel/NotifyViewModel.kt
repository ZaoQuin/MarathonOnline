package com.university.marathononline.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.models.Notification
import com.university.marathononline.data.repository.NotificationRepository
import kotlinx.coroutines.launch

class NotifyViewModel(
    private val notificationRepository: NotificationRepository
): BaseViewModel(listOf(notificationRepository)) {
    private val _notifies = MutableLiveData<List<Notification>>()
    val notifies: LiveData<List<Notification>> get() = _notifies

    private val _getNotificationResponse = MutableLiveData<Resource<List<Notification>>>()
    val getNotificationResponse: LiveData<Resource<List<Notification>>> get() = _getNotificationResponse

    fun setNotification(notifications: List<Notification>){
        _notifies.value = notifications
    }

    fun getNotifications(){
        viewModelScope.launch {
            _getNotificationResponse.value = Resource.Loading
            _getNotificationResponse.value = notificationRepository.getNotificationsByJWT()
        }
    }
}