package com.university.marathononline.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.university.marathononline.R.string.*
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.user.UserApiService
import com.university.marathononline.data.repository.UserRepository
import com.university.marathononline.databinding.ActivityChangePasswordBinding
import com.university.marathononline.ui.viewModel.ChangePasswordViewModel
import com.university.marathononline.utils.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChangePasswordActivity :
    BaseActivity<ChangePasswordViewModel, ActivityChangePasswordBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntentExtras(intent)
        initializeUI()
        setUpObserve()
    }

    private fun handleIntentExtras(intent: Intent) {
        intent.apply {
            viewModel.apply {
                getStringExtra(KEY_EMAIL)?.let { setEmail(it) }
            }
        }
    }

    private fun setUpObserve() {
        viewModel.updatePasswordResponse.observe(this, Observer {
            when(it) {
                is Resource.Success -> {
                    if (it.value.status) showSuccessToastAndNavigate()
                    else Toast.makeText(this, it.value.message, Toast.LENGTH_SHORT).show()
                }
                is Resource.Failure -> {
                    it.getErrorMessage()
                    handleApiError(it)
                }
                else -> Unit
            }
        })
    }

    private fun showSuccessToastAndNavigate() {
        Toast.makeText(this, getMessage(password_update_success), Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            delay(2000)
            startNewActivity(LoginActivity::class.java)
        }
    }


    private fun initializeUI() {
        binding.apply {
            viewModel.email.value?.let {
                emailEditText.text = Editable.Factory.getInstance().newEditable(it)
            }

            resetPasswordButton.setOnClickListener{
                onResetPasswordClick()
            }

            sendOTPButton.setOnClickListener{
                viewModel.sendOtp()
                openOTPVerifyForm()
            }

            buttonBack.setOnClickListener { finishAndGoBack() }
        }
    }

    private fun onResetPasswordClick() {
        if (!validatePasswordConfirmation() || !validateFields()) return

        val password = binding.passwordEditText.text.toString()
        viewModel.setOTP(binding.otpEditText.text.toString())

        val errorMessage =
            if(!viewModel.isOtpValid())
                getMessage(error_invalid_otp)
            else {
                viewModel.setPassword(password)
                viewModel.updatePassword()
                null
            }

        binding.otpErrorText.text = errorMessage
    }

    private fun openOTPVerifyForm(){
        binding.apply {
            listOf(otpEditLabel, otpEditText, passwordLabel, passwordEditText, confirmPasswordLabel, confirmPasswordEditText, resetPasswordButton)
                .forEach { it.visible(true) }
            emailEditText.enable(false)
            sendOTPButton.text = getMessage(resend_otp)
        }
    }

    private fun validateFields(): Boolean {
        val errorMessage = getMessage(error_field_required)
        binding.apply {
            return  !passwordEditText.isEmpty(passwordErrorText, errorMessage) ||
                    !confirmPasswordEditText.isEmpty(confirmPasswordEditText, errorMessage) ||
                    !otpEditText.isEmpty(otpErrorText, errorMessage)
        }
    }

    private fun validatePasswordConfirmation(): Boolean{
        binding.apply {
            val password = passwordEditText.text?.toString() ?: ""
            val confirmPassword = confirmPasswordEditText.text?.toString() ?: ""

            val errorMessage =
                if (password != confirmPassword)
                    getMessage(error_password_mismatch)
                else
                    null
            confirmPasswordErrorText.text = errorMessage
            passwordErrorText.text = errorMessage
            return errorMessage == null
        }
    }

    override fun getViewModel() = ChangePasswordViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) = ActivityChangePasswordBinding.inflate(inflater)

    override fun getActivityRepositories() = listOf( UserRepository(retrofitInstance.buildApi(UserApiService::class.java)))
}