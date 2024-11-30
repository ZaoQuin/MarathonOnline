package com.university.marathononline.ui.viewModel

import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.repository.AuthRepository

class SplashRedirectViewModel(
    private val repository: AuthRepository
): BaseViewModel(listOf(repository)) {
}