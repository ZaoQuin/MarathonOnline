package com.university.marathononline.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseFragment
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.data.request.RefreshTokenRequest
import kotlinx.coroutines.launch

fun <T> Fragment.handleApiError(
    failure: Resource.Failure<T>,
    repository: AuthRepository? = null,
    retry: (() -> Unit)? = null
) {
    when {
        failure.errorCode == 401 -> handleUnauthorizedError(repository, retry)
        failure.errorCode == 403 -> requireView().snackBar("You don't have permission to access this resource.")
        failure.errorCode == 500 -> requireView().snackBar("Something went wrong on the server, please try again later.")
        failure.isNetworkError -> requireView().snackBar("Please check your internet connection", retry)
        else -> {
            val errorMessage = failure.errorBody?.string().orEmpty()
            if (errorMessage.isNotBlank()) {
                requireView().snackBar(errorMessage)
            } else {
                requireView().snackBar("An unknown error occurred.")
            }
        }
    }
}

fun <T> AppCompatActivity.handleApiError(
    failure: Resource.Failure<T>,
    repository: AuthRepository? = null,
    retry: (() -> Unit)? = null
) {
    when {
        failure.errorCode == 401 -> handleUnauthorizedError(repository, retry)
        failure.errorCode == 403 -> window.decorView.snackBar("You don't have permission to access this resource.")
        failure.errorCode == 500 -> window.decorView.snackBar("Something went wrong on the server, please try again later.")
        failure.isNetworkError -> window.decorView.snackBar("Please check your internet connection", retry)
        else -> {
            val errorMessage = failure.errorBody?.string().orEmpty()
            if (errorMessage.isNotBlank()) {
                window.decorView.snackBar(errorMessage)
            } else {
                window.decorView.snackBar("An unknown error occurred.")
            }
        }
    }
}

private fun Fragment.handleUnauthorizedError(
    repository: AuthRepository?,
    retry: (() -> Unit)?
) {
    lifecycleScope.launch {
        val userResult = repository?.getUser()
        if (userResult is Resource.Success) {
            val refreshResult = repository.refreshAccessToken(
                RefreshTokenRequest(userResult.value.refreshToken)
            )
            if (refreshResult is Resource.Success) {
                val newToken = refreshResult.value.token
                repository.saveAuthToken(newToken)
                requireView().snackBar("Token refreshed successfully")

                retry?.invoke()
            } else {
                logout()
                requireView().snackBar("Failed to refresh token")
            }
        } else {
            logout()
            requireView().snackBar("Unable to get user data")
        }
    }
}

private fun AppCompatActivity.handleUnauthorizedError(
    repository: AuthRepository?,
    retry: (() -> Unit)?
) {
    lifecycleScope.launch {
        val userResult = repository?.getUser()
        if (userResult is Resource.Success) {
            val refreshResult = repository.refreshAccessToken(
                RefreshTokenRequest(userResult.value.refreshToken)
            )
            if (refreshResult is Resource.Success) {
                val newToken = refreshResult.value.token
                repository.saveAuthToken(newToken)
                window.decorView.snackBar("Token refreshed successfully")

                retry?.invoke()
            } else {
                logout()
                window.decorView.snackBar("Failed to refresh token")
            }
        } else {
            logout()
            window.decorView.snackBar("Unable to get user data")
        }
    }
}

private fun Fragment.logout() {
    (this as BaseFragment<*, *, *>).logout()
}


private fun AppCompatActivity.logout() {
    (this as BaseActivity<*, *, *>).logout()
}
