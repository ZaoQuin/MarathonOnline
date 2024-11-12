package com.university.marathononline.ui.view.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.university.marathononline.R
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.auth.AuthApiService
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.databinding.ActivityOtpVerificationBinding
import com.university.marathononline.ui.viewModel.OtpVerificationViewModel
import com.university.marathononline.utils.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class OtpVerificationActivity : BaseActivity<OtpVerificationViewModel, ActivityOtpVerificationBinding, AuthRepository>(){
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

            viewModel.user.observe(this@OtpVerificationActivity) { resource ->
                progressBar.visible(resource is Resource.Loading)
                when (resource) {
                    is Resource.Success -> {
                        lifecycleScope.launch {
                            viewModel.setEmail(resource.value.email)
                            otpDescription.text =
                                getString(R.string.otp_sent_to_email, resource.value.email)
                            viewModel.random()
                        }
                    }
                    is Resource.Failure ->
                        handleApiError(resource, getActivityRepository())
                    else -> {  }
                }
            }

            viewModel.verifyResponse.observe(this@OtpVerificationActivity) { resource ->
                progressBar.visible(resource is Resource.Loading)
                when (resource) {
                    is Resource.Success -> {
                        Toast.makeText(this@OtpVerificationActivity, resource.value.fullName, Toast.LENGTH_SHORT).show()
                        lifecycleScope.launch {
                            viewModel.saveStatusUser(resource.value.isVerified)
                            startNewActivity(SplashRedirectActivity::class.java, true)
                        }
                    }
                    is Resource.Failure -> {
                        Log.d("OtpVerificationActivity", resource.getErrorMessage())
                        handleApiError(resource, getActivityRepository())
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
        Toast.makeText(this, getString(R.string.otp_resent), Toast.LENGTH_LONG).show()
    }

    private fun onVerifyOtpButtonClick() = with(binding) {
        viewModel.setOTP(
            otpDigit1.text.toString(),
            otpDigit2.text.toString(),
            otpDigit3.text.toString(),
            otpDigit4.text.toString(),
            otpDigit5.text.toString(),
            otpDigit6.text.toString())
        val errorMessage =
            if
                (!viewModel.isOtpValid()) getString(R.string.error_invalid_otp)
            else {
                null
            }
        otpErrorText.text = errorMessage
        if (errorMessage == null)
            viewModel.verifyAccount()
    }

    private fun validateFields(): Boolean {
        val errorMessage = getString(R.string.error_otp_required)
        return with(binding) {
            listOf(
                otpDigit1.isEmpty(otpErrorText, errorMessage),
                otpDigit2.isEmpty(otpErrorText, errorMessage),
                otpDigit3.isEmpty(otpErrorText, errorMessage),
                otpDigit4.isEmpty(otpErrorText, errorMessage),
                otpDigit5.isEmpty(otpErrorText, errorMessage),
                otpDigit6.isEmpty(otpErrorText, errorMessage)
            ).all { !it }
        }
    }

    override fun getViewModel() = OtpVerificationViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) = ActivityOtpVerificationBinding.inflate(inflater)

    override fun getActivityRepository(): AuthRepository {
        val token = runBlocking { userPreferences.authToken.first() }
        val api = retrofitInstance.buildApi(AuthApiService::class.java, token)
        return AuthRepository(api, userPreferences)
    }
}