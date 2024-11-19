package com.university.marathononline.ui.view.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.university.marathononline.R
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.auth.AuthApiService
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.data.response.AuthResponse
import com.university.marathononline.databinding.ActivityLoginBinding
import com.university.marathononline.ui.viewModel.LoginViewModel
import com.university.marathononline.utils.*
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity<LoginViewModel, ActivityLoginBinding, AuthRepository>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        viewModel.getLoginInfo()
        
        initializeUI()
        setUpObserve()
    }

    private fun initializeUI() {
        binding.apply {            
            progressBar.visible(false)
            appName.visible(true)

            emailEditText.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) emailEditText.validateEmail()
            }

            passwordEditText.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) passwordEditText.validatePassword()
            }

            signUpText.setOnClickListener { navigateToRegister() }

            loginButton.setOnClickListener { onLoginButtonClick() }

            forgetPasswordText.setOnClickListener{ navigateToForgetPassword() }
        }
    }

    private fun navigateToForgetPassword() {
        startNewActivity(ForgetPasswordActivity::class.java)
    }

    private fun onLoginButtonClick() {
        if (!validateFields()) return
        binding.apply {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            viewModel.login(email, password)
            if(remeberMe.isChecked)
                viewModel.saveLoginInfo(email, password)
            else
                viewModel.clearLoginInfo()
        }
    }

    private fun setUpObserve() {
        viewModel.loginResponse.observe(this) { handleLoginResponse(it) }
        viewModel.loginInfo.observe(this) { loginInfo ->
            binding.apply {
                emailEditText.setText(loginInfo.email)
                passwordEditText.setText(loginInfo.password)
                remeberMe.isChecked = loginInfo.remember
            }
        }
    }

    private fun handleLoginResponse(response: Resource<AuthResponse>) {
        binding.apply {
            progressBar.visible(response is Resource.Loading)
            appName.visible(response !is Resource.Loading)
            loginButton.enable(response !is Resource.Loading)
        }

        when (response) {
            is Resource.Success -> onLoginSuccess(response.value)
            is Resource.Failure -> handleApiError(response, getActivityRepository())
            else -> Unit
        }
    }

    private fun onLoginSuccess(authResponse: AuthResponse) {
        lifecycleScope.launch {
            viewModel.saveAuthenticatedUser(authResponse)
            startNewActivity(SplashRedirectActivity::class.java, true)
        }
    }

    private fun validateFields(): Boolean {
        val errorMessage = getMessage(R.string.error_field_required)

        binding.apply {
            return emailEditText.validateField(emailErrorText, errorMessage) &&
                    passwordEditText.validateField(passwordErrorText, errorMessage)
        }
    }

    private fun navigateToRegister() {
        Log.d("LoginActivity", "Register button clicked")
        startNewActivity(RoleSelectionActivity::class.java, true)
    }

    private fun EditText.validateEmail() {
        binding.emailErrorText.text = if (isValidEmail(text.toString())) {
            setDoneIconColor(this)
            null
        } else {
            getMessage(R.string.error_invalid_email)
        }
    }

    private fun EditText.validatePassword() {
        this.isEmpty(binding.passwordErrorText, getMessage(R.string.error_field_required))
    }

    private fun EditText.validateField(errorTextView: TextView, errorMessage: String): Boolean {
        return !this.isEmpty(errorTextView, errorMessage)
    }

    override fun getViewModel() = LoginViewModel::class.java
    override fun getActivityBinding(inflater: LayoutInflater) = ActivityLoginBinding.inflate(inflater)
    override fun getActivityRepository() = AuthRepository(retrofitInstance.buildApi(AuthApiService::class.java), userPreferences)
}