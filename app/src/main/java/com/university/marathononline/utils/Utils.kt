package com.university.marathononline.utils

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.data.request.RefreshTokenRequest
import com.university.marathononline.ui.view.activity.LoginActivity
import kotlinx.coroutines.launch

fun <A: Activity> Activity.startNewActivity(activity: Class<A>){
    Intent(this, activity).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
    }
}

fun View.visible(isVisible: Boolean){
    visibility = if(isVisible) View.VISIBLE else View.GONE
}

fun View.enable(enabled: Boolean){
    isEnabled = enabled
    alpha = if(enabled) 1f else 0.5f
}

fun View.snackbar(message: String, action: (() -> Unit)? = null){
    val snackbar = Snackbar.make(this, message, Snackbar.LENGTH_LONG)
    action?.let{
        snackbar.setAction("Retry"){
            it()
        }
    }

    snackbar.show()
}

fun <T> Fragment.handleApiError(
    failure: Resource.Failure<T>,
    repository: AuthRepository? = null,
    retry: (() -> Unit)? = null
) {
    when{
        failure.errorCode == 401 -> handleUnauthorizedError(repository)
        failure.isNetworkError -> requireView().snackbar("Please check your internet connection", retry)

        else -> {
            val error = failure.errorBody?.string().toString()
            requireView().snackbar(error)
        }
    }
}

fun <T> AppCompatActivity.handleApiError(
    failure: Resource.Failure<T>,
    repository: AuthRepository? = null,
    retry: (() -> Unit)? = null
) {
    when{
        failure.errorCode == 401 -> {
            if(this is LoginActivity){
                window.decorView.snackbar("You've entered incorrect email or password")
            } else {
                handleUnauthorizedError(repository)
            }
        }
        failure.isNetworkError -> window.decorView.snackbar("Please check your internet connection", retry)

        else -> {
            val error = failure.errorBody?.string().toString()
            window.decorView.snackbar(error)
        }
    }
}


private fun Fragment.handleUnauthorizedError(repository: AuthRepository?) {
    lifecycleScope.launch {
        val userResult = repository?.getUser()
        if (userResult is Resource.Success) {
            val refreshResult = repository.refreshAccessToken(
                RefreshTokenRequest(userResult.value.refreshToken)
            )
            if (refreshResult is Resource.Success) {
                val newToken = refreshResult.value.token
                repository.saveAuthToken(newToken)
                requireView().snackbar("Token refreshed successfully")
            } else {
                logout()
                requireView().snackbar("Failed to refresh token")
            }
        } else {
            logout()
            requireView().snackbar("Unable to get user data")
        }
    }
}

private fun AppCompatActivity.handleUnauthorizedError(repository: AuthRepository?) {
    lifecycleScope.launch {
        val userResult = repository?.getUser()
        if (userResult is Resource.Success) {
            val refreshResult = repository.refreshAccessToken(
                RefreshTokenRequest(userResult.value.refreshToken)
            )
            if (refreshResult is Resource.Success) {
                val newToken = refreshResult.value.token
                repository.saveAuthToken(newToken)
                window.decorView.snackbar("Token refreshed successfully")
            } else {
                logout()
                window.decorView.snackbar("Failed to refresh token")
            }
        } else {
            logout()
            window.decorView.snackbar("Unable to get user data")
        }
    }
}

private fun Fragment.logout() {
    (this as BaseActivity<*, *, *>).logout()
}


private fun AppCompatActivity.logout() {
    (this as BaseActivity<*, *, *>).logout()
}



