package com.university.marathononline.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.university.marathononline.R
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.data.request.RefreshTokenRequest
import com.university.marathononline.ui.view.activity.LoginActivity
import kotlinx.coroutines.launch

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
        DrawableCompat.setTint(it,  ContextCompat.getColor(this, R.color.main_color))
        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, it, null)
    }
}

fun AppCompatActivity.togglePasswordVisibility(editText: EditText) {
    editText.setOnClickListener {
        val drawableEnd = 2

        val drawable = editText.compoundDrawables[drawableEnd]
        if (drawable != null && editText.drawableState.contains(android.R.attr.state_pressed)) {
            val currentInputType = editText.inputType

            if (currentInputType == InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                editText.inputType = InputType.TYPE_CLASS_TEXT
                editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null, null, ContextCompat.getDrawable(editText.context, R.drawable.password_icon), null
                )
            } else {
                editText.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null, null, ContextCompat.getDrawable(editText.context, R.drawable.password_visible_off_icon), null
                )
            }
            editText.setSelection(editText.text.length)
        }
    }
}

fun AppCompatActivity.getMessage(id: Int): String{
    return ContextCompat.getString(this, id)
}

fun Fragment.getMessage(id: Int): String{
    return ContextCompat.getString(requireContext(), id)
}

fun EditText.checkEmpty(errorTextView: TextView, errorMessage: String): Boolean {
    return if (text.toString().trim().isEmpty()) {
        errorTextView.text = errorMessage
        false
    } else {
        errorTextView.text = null
        true
    }
}

fun AppCompatActivity.validateNormalEditText(text: EditText, error: TextView){
    if (!text.checkEmpty(error, getMessage(R.string.error_field_required))) {
        setDoneIconColor(text)
    }
}

fun adapterSpinner(min: Int, max: Int, context: Context):  ArrayAdapter<String>{
    val arrange = (min..max).map { it.toString() }.reversed()
    val adapter = ArrayAdapter(context, R.layout.spinner_item, arrange)
    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item )
    return adapter
}