package com.university.marathononline.ui.view.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.lifecycle.Observer
import com.university.marathononline.R
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.data.api.Resource
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
        setUpObserve()
    }

    private fun setUpObserve() {
        viewModel.checkEmailResponse.observe(this, Observer {
            binding.progressBar.visible(it == Resource.Loading)
            when(it){
                is Resource.Success -> {
                    if(!it.value.exists) {
                        binding.emailErrorText.text = null
                        navigateToNextActivityBasedOnRole()
                    }
                    else
                        binding.emailErrorText.text = getMessage(R.string.exist_email)
                }
                is Resource.Loading -> {}
                is Resource.Failure -> handleApiError(it)
            }
        })
    }

    private fun initializeUI() {
        binding.apply {
            progressBar.visible(false)

            fullnameText.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus)  validateNormalEditText(fullnameText, fullnameErrorText)
            }

            emailText.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) validateEmail()
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
        if (!validatePasswordConfirmation() || !validateFields()) return
        viewModel.setEmail(binding.emailText.text.toString())
        viewModel.checkEmail()
    }

    private fun navigateToNextActivityBasedOnRole() {
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
            val email = emailText.text.toString()
            val errorMessage =
                if (isValidEmail(email)) {
                    setDoneIconColor(emailText)
                    null
                }
                else
                    getMessage(R.string.error_invalid_email)
            emailErrorText.text = errorMessage
        }

    }

    private fun validatePasswordConfirmation(): Boolean{
        binding.apply {
            val errorMessage =
                if(passwordText.text.toString()!=confirmPasswordText.text.toString())
                    getMessage(R.string.error_password_mismatch)
                else
                    null
            confirmErrorPasswordText.text = errorMessage
            passwordErrorText.text = errorMessage
            return errorMessage == null
        }
    }

    override fun getViewModel() = RegisterBasicInformationViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) = ActivityRegisterBasicInformationBinding.inflate(inflater)

    override fun getActivityRepository() = UserRepository(retrofitInstance.buildApi(UserApiService::class.java))
}