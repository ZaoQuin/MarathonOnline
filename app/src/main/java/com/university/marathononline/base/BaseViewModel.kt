package com.university.marathononline.base

import androidx.lifecycle.ViewModel
import com.university.marathononline.data.api.auth.AuthApiService
import com.university.marathononline.data.repository.AuthRepository

abstract class BaseViewModel(
    private val repositories: List<BaseRepository>
) : ViewModel() {

    private fun <T : BaseRepository> findRepository(type: Class<T>): BaseRepository {
        return repositories.find { type.isInstance(it) }
            ?: throw IllegalArgumentException("Repository not found: ${type.simpleName}")
    }

    suspend fun logout(api: AuthApiService) {
        val authRepository = findRepository(AuthRepository::class.java)
        authRepository.logout(api)
    }
}