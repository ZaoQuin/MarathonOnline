package com.university.marathononline.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ContestDetailsViewModel: ViewModel() {

    private val _selectedTab = MutableLiveData<Int>()
    val selectedTab: LiveData<Int> get() = _selectedTab

    fun selectTab(index: Int){
        _selectedTab.value = index
    }
}