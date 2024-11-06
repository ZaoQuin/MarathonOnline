package com.university.marathononline.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {

    private val _selectedPage = MutableLiveData<Int>()
    val selectedPage: LiveData<Int> get() = _selectedPage

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
}