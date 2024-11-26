package com.university.marathononline.ui.viewModel

import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AccountDeletedViewModel(
    private val repository: AuthRepository
): BaseViewModel(repository) {
    fun clearAuthenticated() {
        viewModelScope.launch {
            repository.clearAuthenticated()
        }
    }
}