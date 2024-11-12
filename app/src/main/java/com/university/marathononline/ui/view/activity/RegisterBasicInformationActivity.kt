package com.university.marathononline.ui.view.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import com.university.marathononline.R
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.data.api.user.UserApiService
import com.university.marathononline.data.models.ERole
import com.university.marathononline.data.repository.UserRepository
import com.university.marathononline.databinding.ActivityRegisterBasicInformationBinding
import com.university.marathononline.ui.viewModel.RegisterBasicInformationViewModel
import com.university.marathononline.utils.*

class RegisterBasicInformationActivity : BaseActivity<RegisterBasicInformationViewModel, ActivityRegisterBasicInformationBinding, UserRepository>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.getStringExtra(KEY_ROLE)?.let {
            viewModel.selectedRole(ERole.valueOf(it))
            Log.d("RegisterBasicInformationActivity", it)
        }

        initializeUI()
    }

    private fun initializeUI() {
        binding.apply {
            fullnameText.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus)  validateNormalEditText(fullnameText, fullnameErrorText)
            }

            emailText.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) validateEmail()
            }

            confirmPasswordText.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) validatePasswordConfirmation()
            }

            continueButton.setOnClickListener { onContinueButtonClick() }
            buttonBack.setOnClickListener { finishAndGoBack() }
        }
    }

    private fun validateFields(): Boolean {
        val errorMessage = getMessage(R.string.error_field_required)
        binding.apply {
        return  !fullnameText.isEmpty(fullnameErrorText, errorMessage) ||
                !emailText.isEmpty(emailErrorText, errorMessage) ||
                !passwordText.isEmpty(passwordErrorText, errorMessage) ||
                !confirmPasswordText.isEmpty(confirmErrorPasswordText, errorMessage)
        }
    }

    private fun onContinueButtonClick(){
        if(!validateFields())
            return

        viewModel.role.value?.let {
            val nextActivity = if (it == ERole.RUNNER) {
                RegisterRunnerInfoActivity::class.java
            } else {
                RegisterOrganizerInfoActivity::class.java
            }
            startNewActivity(nextActivity,
                mapOf(
                    KEY_FULL_NAME to binding.fullnameText.text.toString(),
                    KEY_EMAIL to binding.emailText.text.toString(),
                    KEY_PASSWORD to binding.passwordText.text.toString()
                ))
        }
    }

    private fun validateEmail(){
        binding.apply {
            val errorMessage =
                if (isValidEmail(emailText.text.toString())) {
                    setDoneIconColor(emailText)
                    null
                }
                else
                    getMessage(R.string.error_invalid_email)
            emailErrorText.text = errorMessage
        }
    }

    private fun validatePasswordConfirmation(){
        binding.apply {
            val errorMessage =
                if(passwordText.text.toString()!=confirmPasswordText.text.toString())
                    getMessage(R.string.error_password_mismatch)
                else
                    null
            confirmErrorPasswordText.text = errorMessage
            passwordErrorText.text = errorMessage
        }
    }

    override fun getViewModel() = RegisterBasicInformationViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) = ActivityRegisterBasicInformationBinding.inflate(inflater)

    override fun getActivityRepository() = UserRepository(retrofitInstance.buildApi(UserApiService::class.java))
}