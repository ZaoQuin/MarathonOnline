package com.university.marathononline.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.repository.AuthRepository
import kotlinx.coroutines.launch

class MainViewModel(
    private val authRepository: AuthRepository
): BaseViewModel(listOf(authRepository)) {
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