package com.university.marathononline.base

import androidx.lifecycle.ViewModel
import com.university.marathononline.data.api.auth.AuthApiService

abstract class BaseViewModel(
    private val repository: BaseRepository
) : ViewModel() {

    suspend fun logout(api: AuthApiService) = repository.logout(api)
}