package com.university.marathononline.ui.view.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.lifecycle.*
import com.university.marathononline.R
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.user.UserApiService
import com.university.marathononline.data.repository.UserRepository
import com.university.marathononline.databinding.ActivityForgetPasswordBinding
import com.university.marathononline.ui.viewModel.ForgetPasswordViewModel
import com.university.marathononline.utils.*
import handleApiError
import kotlinx.coroutines.*

class ForgetPasswordActivity: BaseActivity<ForgetPasswordViewModel, ActivityForgetPasswordBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeUI()
        setUpObserve()
    }

    private fun setUpObserve() {
        viewModel.checkEmailResponse.observe(this, Observer {
            binding.progressBar.visible(it == Resource.Loading)
            when(it) {
                is Resource.Success -> {
                    binding.emailErrorText.visible(!it.value.exists)
                    if(it.value.exists) {
                        viewModel.sendOtp()
                        openOTPVerifyForm()
                    } else {
                        binding.emailErrorText.text = getMessage(R.string.error_invalid_email)
                    }
                }
                is Resource.Loading -> {}
                is Resource.Failure -> {
                    it.fetchErrorMessage()
                    handleApiError(it)
                }
            }
        })

        viewModel.updatePasswordResponse.observe(this, Observer {
            binding.progressBar.visible(it == Resource.Loading)
            when(it) {
                is Resource.Success -> {
                    if (it.value.status) showSuccessToastAndNavigate()
                    else Toast.makeText(this, it.value.message, Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {}
                is Resource.Failure -> {
                    it.fetchErrorMessage()
                    handleApiError(it)
                }
            }
        })
    }

    private fun showSuccessToastAndNavigate() {
        Toast.makeText(this, getMessage(R.string.password_update_success), Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            delay(2000)
            startNewActivity(LoginActivity::class.java)
        }
    }


    private fun initializeUI() {
        binding.apply {
            progressBar.visible(false)
            resetPasswordButton.setOnClickListener{
                onResetPasswordClick()
            }

            sendOTPButton.setOnClickListener{
                viewModel.setEmail(emailEditText.text.toString())
                viewModel.checkEmail()
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
                getMessage(R.string.error_invalid_otp)
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
            sendOTPButton.text = getMessage(R.string.resend_otp)
        }
    }

    private fun validateFields(): Boolean {
        val errorMessage = getMessage(R.string.error_field_required)
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
                    getMessage(R.string.error_password_mismatch)
                else
                    null
            confirmPasswordErrorText.text = errorMessage
            passwordErrorText.text = errorMessage
            return errorMessage == null
        }
    }

    override fun getViewModel() = ForgetPasswordViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) = ActivityForgetPasswordBinding.inflate(inflater)

    override fun getActivityRepositories() = listOf( UserRepository(retrofitInstance.buildApi(UserApiService::class.java)))
}