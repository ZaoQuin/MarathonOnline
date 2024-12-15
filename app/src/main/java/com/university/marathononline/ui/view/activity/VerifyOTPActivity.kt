package com.university.marathononline.ui.view.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.university.marathononline.R.string.*
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.auth.AuthApiService
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.databinding.ActivityVerifyOtpBinding
import com.university.marathononline.ui.viewModel.VerifyOTPViewModel
import com.university.marathononline.utils.*
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class VerifyOTPActivity : BaseActivity<VerifyOTPViewModel, ActivityVerifyOtpBinding>(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getUser()
        initializeUI()
        setupObserver()
    }

    private fun setupObserver() {
        with(binding) {
            progressBar.visible(false)
            verifyOtpButton.enable(false)

            viewModel.user.observe(this@VerifyOTPActivity) { resource ->
                progressBar.visible(resource is Resource.Loading)
                when (resource) {
                    is Resource.Success -> {
                        lifecycleScope.launch {
                            viewModel.setEmail(resource.value.email)
                            otpDescription.text =
                                getString(otp_sent_to_email, resource.value.email)
                            viewModel.random()
                        }
                    }
                    is Resource.Failure ->
                        handleApiError(resource)
                    else -> {  }
                }
            }

            viewModel.verifyResponse.observe(this@VerifyOTPActivity) { resource ->
                progressBar.visible(resource is Resource.Loading)
                when (resource) {
                    is Resource.Success -> {
                        Toast.makeText(this@VerifyOTPActivity, resource.value.fullName, Toast.LENGTH_SHORT).show()
                        lifecycleScope.launch {
                            viewModel.saveStatusUser(resource.value.isVerified)
                            startNewActivity(SplashRedirectActivity::class.java, true)
                        }
                    }
                    is Resource.Failure -> {
                        handleApiError(resource)
                    }
                    else -> {  }
                }
            }
        }
    }

    private fun initializeUI() = with(binding) {
        setupOtpFieldFocus()
        verifyOtpButton.setOnClickListener { onVerifyOtpButtonClick() }
        loginText.setOnClickListener { logout() }
        resendOTP.setOnClickListener { resendOtp() }
    }

    private fun setupOtpFieldFocus() = with(binding) {
        arrayOf(otpDigit1, otpDigit2, otpDigit3, otpDigit4, otpDigit5, otpDigit6).forEachIndexed { index, editText ->
            editText.doOnTextChanged { _, _, _, _ -> handleOtpFocus(index) }
        }
    }

    private fun handleOtpFocus(index: Int) = with(binding) {
        when (index) {
            0 -> if (otpDigit1.text.isNotEmpty()) otpDigit2.requestFocus()
            1 -> if (otpDigit2.text.isNotEmpty()) otpDigit3.requestFocus() else otpDigit1.requestFocus()
            2 -> if (otpDigit3.text.isNotEmpty()) otpDigit4.requestFocus() else otpDigit2.requestFocus()
            3 -> if (otpDigit4.text.isNotEmpty()) otpDigit5.requestFocus() else otpDigit3.requestFocus()
            4 -> if (otpDigit5.text.isNotEmpty()) otpDigit6.requestFocus() else otpDigit4.requestFocus()
            5 -> if (otpDigit6.text.isEmpty()) otpDigit5.requestFocus()
        }
        verifyOtpButton.enable(validateFields())
    }

    private fun resendOtp() {
        viewModel.random()
        Toast.makeText(this, getString(otp_resent), Toast.LENGTH_LONG).show()
    }

    private fun onVerifyOtpButtonClick() = with(binding) {
        viewModel.setOTP(
            otpDigit1.getString(),
            otpDigit2.getString(),
            otpDigit3.getString(),
            otpDigit4.getString(),
            otpDigit5.getString(),
            otpDigit6.getString())
        val errorMessage = if (viewModel.isOtpValid()) {
            null
        } else {
            getString(error_invalid_otp)
        }

        otpErrorText.text = errorMessage
        if (errorMessage == null) {
            viewModel.verifyAccount()
        }
    }

    private fun validateFields(): Boolean {
        val errorMessage = getMessage(error_field_required)
        binding.apply {
            val fields = listOf(
                otpDigit1 to otpErrorText,
                otpDigit2 to otpErrorText,
                otpDigit3 to otpErrorText,
                otpDigit4 to otpErrorText,
                otpDigit5 to otpErrorText,
                otpDigit6 to otpErrorText,
            )
            return fields.all { (field, errorText) -> !field.isEmpty(errorText, errorMessage) }
        }
    }

    override fun getViewModel() = VerifyOTPViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) = ActivityVerifyOtpBinding.inflate(inflater)

    override fun getActivityRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val api = retrofitInstance.buildApi(AuthApiService::class.java, token)
        return listOf(AuthRepository(api, userPreferences))
    }
}