package com.university.marathononline.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.university.marathononline.data.models.EventHistory
import com.university.marathononline.data.models.User

class LeaderBoardViewModel: ViewModel() {
    private val _eventHistories = MutableLiveData<List<EventHistory>>()
    val eventHistories: LiveData<List<EventHistory>> get() = _eventHistories

    private val _users: MutableLiveData<List<User>> = MutableLiveData()
    val users: LiveData<List<User>> get() = _users

}