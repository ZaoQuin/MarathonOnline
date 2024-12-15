package com.university.marathononline.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.models.Notification
import com.university.marathononline.data.repository.NotificationRepository
import kotlinx.coroutines.launch

class MainViewModel(
    private val notificationRepository: NotificationRepository
): BaseViewModel(listOf(notificationRepository)) {
    private val _selectedPage = MutableLiveData<Int>()
    val selectedPage: LiveData<Int> get() = _selectedPage

    private val _getNotificationResponse = MutableLiveData<Resource<List<Notification>>>()
    val getNotificationResponse: LiveData<Resource<List<Notification>>> get() = _getNotificationResponse


    init {
        _selectedPage.value = 0
    }

    fun onPageSelected(position: Int) {
        _selectedPage.value = position
    }

    fun onNavOptionSelected(position: Int): Boolean {
        _selectedPage.value = position
        return true
    }

    fun getNotification() {
        viewModelScope.launch {
            _getNotificationResponse.value = Resource.Loading
            _getNotificationResponse.value = notificationRepository.getNotificationsByJWT()
        }
    }
}