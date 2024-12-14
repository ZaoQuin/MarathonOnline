package com.university.marathononline.ui.view.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.university.marathononline.R
import com.university.marathononline.R.string.*
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.auth.AuthApiService
import com.university.marathononline.data.models.ERole
import com.university.marathononline.data.models.LoginInfo
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.data.response.AuthResponse
import com.university.marathononline.databinding.ActivityLoginBinding
import com.university.marathononline.ui.viewModel.LoginViewModel
import com.university.marathononline.utils.*
import handleApiError
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity<LoginViewModel, ActivityLoginBinding>() {

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

            val roleMap = mapOf(
                runnerRole to ERole.RUNNER,
                organizerRole to ERole.ORGANIZER
            )

            roleMap.forEach { (button, role) ->
                button.setOnClickListener {
                    viewModel.selectedRole(role)
                }
            }

            passwordEditText.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    val drawableEnd = passwordEditText.compoundDrawables[2]
                    if (drawableEnd != null && event.rawX >= (passwordEditText.right - drawableEnd.bounds.width())) {
                        passwordEditText.togglePasswordVisibility(
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

    private fun setUpObserve() {
        viewModel.loginResponse.observe(this) { handleLoginResponse(it) }

        viewModel.loginInfo.observe(this) { loginInfo -> updateLoginFields(loginInfo) }
    }

    private fun setupClickListeners() {
        binding.apply {
            signUpText.setOnClickListener { navigateToRegister() }
            loginButton.setOnClickListener { onLoginButtonClick() }
            forgetPasswordText.setOnClickListener{ navigateToForgetPassword() }
        }
    }

    private fun onLoginButtonClick() {
        if (!validateFields()) return
        binding.apply {
            val email = emailEditText.getString()
            val password = passwordEditText.getString()
            viewModel.login(email, password)
            if (remeberMe.isChecked) {
                viewModel.saveLoginInfo(email, password)
            } else {
                viewModel.clearLoginInfo()
            }
        }
    }

    private fun updateLoginFields(loginInfo: LoginInfo){
        binding.apply {
            emailEditText.setText(loginInfo.email)
            passwordEditText.setText(loginInfo.password)
            remeberMe.isChecked = loginInfo.remember
        }
    }

    private fun handleLoginResponse(response: Resource<AuthResponse>) {
        binding.apply {
            progressBar.visible(response is Resource.Loading)
            appName.visible(response !is Resource.Loading)
            loginButton.enable(response !is Resource.Loading)
        }

        when (response) {
            is Resource.Success -> {
                if(viewModel.selectedRole.value == null)
                    viewModel.selectedRole(ERole.RUNNER)
                if(viewModel.selectedRole.value != response.value.role){
                    Toast.makeText(this, getString(error_selected_role), Toast.LENGTH_SHORT).show()
                } else {
                    onLoginSuccess(response.value)
                }
            }
            is Resource.Failure -> {
                handleApiError(response)

                Toast.makeText(this, getString(error_login), Toast.LENGTH_SHORT).show()
            }
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
        val errorMessage = getMessage(error_field_required)
        binding.apply {
            val fields = listOf(
                emailEditText to emailErrorText,
                passwordEditText to passwordErrorText
            )
            return emailEditText.isEmail( emailErrorText, getString(error_invalid_email))
                    && passwordEditText.isValidPassword( passwordErrorText, getString(error_invalid_password))
                    && fields.all { (field, errorText) -> !field.isEmpty(errorText, errorMessage) }
        }
    }

    private fun navigateToForgetPassword() {
        startNewActivity(ForgetPasswordActivity::class.java)
    }

    private fun navigateToRegister() {
        startNewActivity(RoleSelectionActivity::class.java, true)
    }

    override fun getViewModel() = LoginViewModel::class.java
    override fun getActivityBinding(inflater: LayoutInflater) = ActivityLoginBinding.inflate(inflater)
    override fun getActivityRepositories() = listOf( AuthRepository(retrofitInstance.buildApi(AuthApiService::class.java), userPreferences))
}