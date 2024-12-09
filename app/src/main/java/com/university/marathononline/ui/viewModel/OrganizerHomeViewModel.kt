package com.university.marathononline.ui.viewModel

import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.repository.AuthRepository

class OrganizerHomeViewModel(
    private val authRepository: AuthRepository
): BaseViewModel(listOf(authRepository)) {
}