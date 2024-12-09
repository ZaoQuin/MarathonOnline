package com.university.marathononline.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.lifecycle.Observer
import com.university.marathononline.R
import com.university.marathononline.R.string.*
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.user.UserApiService
import com.university.marathononline.data.models.ERole
import com.university.marathononline.data.repository.UserRepository
import com.university.marathononline.data.response.CheckEmailResponse
import com.university.marathononline.databinding.ActivityRegisterBasicInformationBinding
import com.university.marathononline.ui.viewModel.RegisterBasicInformationViewModel
import com.university.marathononline.utils.*
import handleApiError

class RegisterBasicInformationActivity : BaseActivity<RegisterBasicInformationViewModel, ActivityRegisterBasicInformationBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntentExtras(intent)
        initializeUI()
        setUpObserve()
    }

    private fun handleIntentExtras(intent: Intent) {
        intent.apply {
            viewModel.apply {
                getStringExtra(KEY_ROLE)?.let { selectedRole(ERole.valueOf(it)) }
            }
        }
    }

    private fun setUpObserve() {
        viewModel.checkEmailResponse.observe(this, Observer {
            binding.progressBar.visible(it == Resource.Loading)
            handleEmailResponse(it)
        })
    }

    private fun handleEmailResponse(resource: Resource<CheckEmailResponse>){
        when(resource) {
            is Resource.Success -> {
                if (!resource.value.exists) {
                    binding.emailErrorText.text = null
                    navigateToNextActivityBasedOnRole()
                } else {
                    binding.emailErrorText.text = getMessage(exist_email)
                }
            }
            is Resource.Failure -> handleApiError(resource)
            else -> Unit
        }
    }

    private fun initializeUI() {
        binding.apply {
            progressBar.visible(false)

            passwordText.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    val drawableEnd = passwordText.compoundDrawables[2]
                    if (drawableEnd != null && event.rawX >= (passwordText.right - drawableEnd.bounds.width())) {
                        passwordText.togglePasswordVisibility(
                            drawableVisible = R.drawable.password_icon,
                            drawableInvisible = R.drawable.password_visible_off_icon
                        )
                        return@setOnTouchListener true
                    }
                }
                false
            }

            confirmPasswordText.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    val drawableEnd = confirmPasswordText.compoundDrawables[2]
                    if (drawableEnd != null && event.rawX >= (confirmPasswordText.right - drawableEnd.bounds.width())) {
                        confirmPasswordText.togglePasswordVisibility(
                            drawableVisible = R.drawable.password_icon,
                            drawableInvisible = R.drawable.password_visible_off_icon
                        )
                        return@setOnTouchListener true
                    }
                }
                false
            }
        }
        setupClickListeners()
    }

    private fun setupClickListeners(){
        binding.apply {
            continueButton.setOnClickListener { onContinueButtonClick() }
            buttonBack.setOnClickListener { finishAndGoBack() }
        }
    }

    private fun validateFields(): Boolean {
        val errorMessage = getMessage(error_field_required)
        binding.apply {
            val fields = listOf(
                fullnameText to fullnameErrorText,
                emailText to emailErrorText,
                passwordText to passwordErrorText,
                confirmPasswordText to confirmErrorPasswordText
            )
            return isMatch(passwordText, confirmPasswordText,
                passwordErrorText, confirmErrorPasswordText,
                getString(error_password_mismatch))
                    && emailText.isEmail( emailErrorText, getString(error_invalid_email))
                    && passwordText.isValidPassword( passwordErrorText, getString(error_invalid_password))
                    && fields.any { (field, errorText) -> !field.isEmpty(errorText, errorMessage) }
        }

    }

    private fun onContinueButtonClick(){
        if (!validateFields()) return
        viewModel.setEmail(binding.emailText.getString())
        viewModel.checkEmail()
    }

    private fun navigateToNextActivityBasedOnRole() {
        viewModel.role.value?.let {
            val nextActivity =
                if (it == ERole.RUNNER) {
                    RegisterRunnerInfoActivity::class.java
                } else {
                    RegisterOrganizerInfoActivity::class.java
                }
            binding.apply {
                startNewActivity(nextActivity,
                    mapOf(
                        KEY_FULL_NAME to fullnameText.getString(),
                        KEY_EMAIL to emailText.getString(),
                        KEY_PASSWORD to passwordText.getString()
                    ))
            }
        }
    }

    override fun getViewModel() = RegisterBasicInformationViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) = ActivityRegisterBasicInformationBinding.inflate(inflater)

    override fun getActivityRepositories() = listOf( UserRepository(retrofitInstance.buildApi(UserApiService::class.java)) )
}