package com.university.marathononline.ui.view.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.auth.AuthApiService
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.databinding.ActivityLoginBinding
import com.university.marathononline.ui.viewModel.LoginViewModel
import com.university.marathononline.utils.enable
import com.university.marathononline.utils.handleApiError
import com.university.marathononline.utils.startNewActivity
import com.university.marathononline.utils.visible
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity<LoginViewModel, ActivityLoginBinding, AuthRepository>() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.progressBar.visible(false)
        binding.loginButton.enable(false)
        binding.appName.visible(true)

        binding.signUpText.setOnClickListener{
            Log.d("LoginActivity", "Register button clicked")
            startNewActivity(RoleSelectionActivity::class.java, true)
        }

        binding.loginButton.setOnClickListener {
            Log.d("LoginActivity", "Login button clicked")
            viewModel.login(binding.emailEditText.text.toString(),
                binding.passwordEditText.text.toString())
        }

        binding.emailEditText.addTextChangedListener {
            val email = binding.emailEditText.text.toString().trim()
            binding.loginButton.enable(email.isNotEmpty() && it.toString().isNotEmpty())
        }

        setUpObserve()
    }

    private fun setUpObserve() {
        viewModel.loginResponse.observe(this, Observer {
            binding.progressBar.visible(false)
            binding.appName.visible(true)
            when(it){
                is Resource.Success -> {
                    lifecycleScope.launch {
                        viewModel.saveAuthToken(it.value.accessToken)
                        startNewActivity(MainActivity::class.java)
                    }
                }
                is Resource.Loading -> {
                    binding.appName.visible(false)
                    binding.progressBar.visible(true)
                }
                is Resource.Failure -> handleApiError(it, getActivityRepository())
            }
        })
    }

    override fun getViewModel() = LoginViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) = ActivityLoginBinding.inflate(inflater)

    override fun getActivityRepository(): AuthRepository = AuthRepository(retrofitInstance.buildApi(AuthApiService::class.java), userPreferences)
}