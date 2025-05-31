package com.university.marathononline.ui.viewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.repository.NotificationRepository
import kotlinx.coroutines.launch

class MainViewModel(
    private val notificationRepository: NotificationRepository
): BaseViewModel(listOf(notificationRepository)) {
    private val _selectedPage = MutableLiveData<Int>()
    val selectedPage: LiveData<Int> get() = _selectedPage

    private val _updateFCMToken: MutableLiveData<Resource<Any>> = MutableLiveData()
    val updateFCMToken: LiveData<Resource<Any>> get() = _updateFCMToken


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

    fun updateFCMToken(token: String, context: Context) {
        viewModelScope.launch {
            _updateFCMToken.value = Resource.Loading
            _updateFCMToken.value = notificationRepository.updateFCMToken(token, context)
        }
    }
}