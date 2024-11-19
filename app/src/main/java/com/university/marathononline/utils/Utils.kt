package com.university.marathononline.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.university.marathononline.R
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseFragment
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.data.request.RefreshTokenRequest
import com.university.marathononline.ui.view.activity.LoginActivity
import kotlinx.coroutines.launch
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

fun Activity.finishAndGoBack() {
    finish()
}

fun <A: Activity> Activity.startNewActivity(activity: Class<A>,
                                            clearBackStack: Boolean = false){
    Intent(this, activity).also {
        if (clearBackStack) it.flags = NO_BACK_STACK_FLAGS
        startActivity(it)
    }
}

fun <A : Activity> Activity.startNewActivity(
    activity: Class<A>,
    data: Map<String, Any> = emptyMap(),
    clearBackStack: Boolean = false
) {
    val intent = Intent(this, activity)

    for ((key, value) in data) {
        when (value) {
            is String -> intent.putExtra(key, value)
            is Int -> intent.putExtra(key, value)
            is Boolean -> intent.putExtra(key, value)
            is Float -> intent.putExtra(key, value)
            is Double -> intent.putExtra(key, value)
            is Long -> intent.putExtra(key, value)
            else -> throw IllegalArgumentException("Unsupported data type")
        }
    }

    if (clearBackStack) intent.flags = NO_BACK_STACK_FLAGS

    startActivity(intent)
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
        failure.errorCode == 401 -> {
                handleUnauthorizedError(repository, retry)
        }
        failure.errorCode == 403 -> {
            requireView().snackbar("You don't have permission to access this resource.")
        }
        failure.errorCode == 500 -> {
            requireView().snackbar("Something went wrong on the server, please try again later.")
        }
        failure.isNetworkError -> {
            requireView().snackbar("Please check your internet connection", retry)
        }
        else -> {
            val errorMessage = failure.errorBody?.string().orEmpty()
            if (errorMessage.isNotBlank()) {
                requireView().snackbar(errorMessage)
            } else {
                requireView().snackbar("An unknown error occurred.")
            }
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
            if (this is LoginActivity) {
                window.decorView.snackbar("You've entered incorrect email or password")
            } else {
                handleUnauthorizedError(repository, retry)
            }
        }
        failure.errorCode == 403 -> {
            failure.getErrorMessage()
            window.decorView.snackbar("You don't have permission to access this resource.")
        }
        failure.errorCode == 500 -> {
            window.decorView.snackbar("Something went wrong on the server, please try again later.")
        }
        failure.isNetworkError -> {
            window.decorView.snackbar("Please check your internet connection", retry)
        }
        else -> {
            val errorMessage = failure.errorBody?.string().orEmpty()
            if (errorMessage.isNotBlank()) {
                window.decorView.snackbar(errorMessage)
            } else {
                window.decorView.snackbar("An unknown error occurred.")
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
                requireView().snackbar("Token refreshed successfully")

                retry?.invoke()
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
                window.decorView.snackbar("Token refreshed successfully")

                retry?.invoke()
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
    (this as BaseFragment<*, *, *>).logout()
}


private fun AppCompatActivity.logout() {
    (this as BaseActivity<*, *, *>).logout()
}

fun isValidEmail(email: String): Boolean {
    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    return email.matches(emailPattern.toRegex())
}

fun isValidPhoneNumber(phoneNumber: String): Boolean {
    val regex = "^[+]?[0-9]{10,13}\$".toRegex()
    return phoneNumber.matches(regex)
}



fun  AppCompatActivity.setDoneIconColor(editText: EditText) {
    val doneIcon = editText.compoundDrawablesRelative[2]
    doneIcon?.let {
        DrawableCompat.setTint(it, ContextCompat.getColor(this, R.color.main_color))
        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, it, null)
    }
}

fun AppCompatActivity.getMessage(id: Int): String{
    return ContextCompat.getString(this, id)
}

fun Fragment.getMessage(id: Int): String{
    return ContextCompat.getString(requireContext(), id)
}

fun EditText.isEmpty(errorTextView: TextView, errorMessage: String): Boolean {
    return if (text.toString().trim().isEmpty()) {
        errorTextView.text = errorMessage
        true
    } else {
        errorTextView.text = null
        false
    }
}

fun AppCompatActivity.validateNormalEditText(text: EditText, error: TextView){
    if (!text.isEmpty(error, getMessage(R.string.error_field_required))) {
        setDoneIconColor(text)
    }
}

fun adapterSpinner(min: Int, max: Int, context: Context):  ArrayAdapter<String>{
    val arrange = (min..max).map { it.toString() }.reversed()
    val adapter = ArrayAdapter(context, R.layout.spinner_item, arrange)
    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item )
    return adapter
}

fun createOrGetKey(): SecretKey {
    val keyStore = KeyStore.getInstance("AndroidKeyStore")
    keyStore.load(null)

    val existingKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
    if (existingKey != null) {
        return existingKey
    }

    val keyGenerator = KeyGenerator.getInstance(
        KeyProperties.KEY_ALGORITHM_AES,
        "AndroidKeyStore"
    )

    val keyGenParameterSpec = KeyGenParameterSpec.Builder(
        KEY_ALIAS,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .build()

    keyGenerator.init(keyGenParameterSpec)
    return keyGenerator.generateKey()
}