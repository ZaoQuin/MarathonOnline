package com.university.marathononline.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.university.marathononline.R.string.error_field_required
import com.university.marathononline.R.string.error_invalid_otp
import com.university.marathononline.R.string.password_update_success
import com.university.marathononline.R.string.resend_otp
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.auth.AuthApiService
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.databinding.ActivityDeleteUserAccountBinding
import com.university.marathononline.ui.viewModel.DeleteUserAccountViewModel
import com.university.marathononline.utils.KEY_EMAIL
import com.university.marathononline.utils.enable
import com.university.marathononline.utils.finishAndGoBack
import com.university.marathononline.utils.getMessage
import com.university.marathononline.utils.isEmpty
import com.university.marathononline.utils.startNewActivity
import com.university.marathononline.utils.visible
import handleApiError
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class DeleteUserAccountActivity :
    BaseActivity<DeleteUserAccountViewModel, ActivityDeleteUserAccountBinding>() {
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
        viewModel.deleteAccountResponse.observe(this, Observer {
            when(it){
                is Resource.Success -> {
                    lifecycleScope.launch {
                        userPreferences.saveDeleted(true)
                        startNewActivity(AccountDeletedActivity::class.java, true)
                    }
                }
                is Resource.Failure -> handleApiError(it)
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

            sendOTPButton.setOnClickListener{
                viewModel.sendOtp()
                openOTPVerifyForm()
            }

            cancelButton.setOnClickListener{finishAndGoBack()}

            buttonBack.setOnClickListener { finishAndGoBack() }

            deleteAccountButton.setOnClickListener { handleDeleteAccount()}
        }
    }

    private fun handleDeleteAccount() {
        if(!validateFields())
            return

        viewModel.setOTP(binding.otpEditText.text.toString())
        val errorMessage =
            if(!viewModel.isOtpValid())
                getMessage(error_invalid_otp)
            else {
                viewModel.delete()
                null
            }

        binding.otpErrorText.text = errorMessage
    }

    private fun openOTPVerifyForm(){
        binding.apply {
            listOf(otpEditLabel, otpEditText, deleteAccountButton)
                .forEach { it.visible(true) }
            emailEditText.enable(false)
            sendOTPButton.text = getMessage(resend_otp)
        }
    }

    private fun validateFields(): Boolean {
        val errorMessage = getMessage(error_field_required)
        binding.apply {
            return !otpEditText.isEmpty(otpErrorText, errorMessage)
        }
    }

    override fun getViewModel() = DeleteUserAccountViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) = ActivityDeleteUserAccountBinding.inflate(inflater)

    override fun getActivityRepositories() : List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val api = retrofitInstance.buildApi(AuthApiService::class.java, token)
        return listOf( AuthRepository(api, userPreferences))
    }
}