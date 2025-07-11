package com.university.marathononline.ui.viewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.repository.NotificationRepository
import com.university.marathononline.data.repository.RecordRepository
import com.university.marathononline.data.api.record.CreateRecordRequest
import com.university.marathononline.data.api.share.StringResponse
import kotlinx.coroutines.launch

class MainViewModel(
    private val notificationRepository: NotificationRepository,
    private val recordRepository: RecordRepository
): BaseViewModel(listOf(notificationRepository, recordRepository)) {
    private val _selectedPage = MutableLiveData<Int>()
    val selectedPage: LiveData<Int> get() = _selectedPage

    private val _updateFCMToken: MutableLiveData<Resource<Any>> = MutableLiveData()
    val updateFCMToken: LiveData<Resource<Any>> get() = _updateFCMToken

    private val _syncRecords: MutableLiveData<Resource<StringResponse>> = MutableLiveData()
    val syncRecords: LiveData<Resource<StringResponse>> get() = _syncRecords


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

    fun syncRecords(request: List<CreateRecordRequest>) {
        viewModelScope.launch {
            _syncRecords.value = Resource.Loading
            _syncRecords.value = recordRepository.sync(request)
        }
    }
}