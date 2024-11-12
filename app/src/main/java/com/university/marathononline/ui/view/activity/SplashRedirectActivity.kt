package com.university.marathononline.ui.view.activity

import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.data.api.auth.AuthApiService
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.databinding.ActivitySplashRedirectBinding
import com.university.marathononline.ui.viewModel.SplashRedirectViewModel
import com.university.marathononline.utils.startNewActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class SplashRedirectActivity : BaseActivity<SplashRedirectViewModel, ActivitySplashRedirectBinding, AuthRepository>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            delay(3000)
            setupObserve()
        }
    }

    private fun setupObserve() {
        observeAuthToken()
    }

    private fun observeAuthToken() {
        lifecycleScope.launch {
            // Lấy authToken và isVerified một cách đồng bộ hơn, không delay
            val token = userPreferences.authToken.firstOrNull()
            if (token == null) {
                startNewActivity(LoginActivity::class.java, true)
            } else {
                val isVerified = userPreferences.isVerified.firstOrNull()
                when {
                    isVerified == null -> startNewActivity(LoginActivity::class.java, true)
                    !isVerified -> startNewActivity(OtpVerificationActivity::class.java)
                    else -> startNewActivity(MainActivity::class.java, true)
                }
            }
            // Đảm bảo các công việc được xử lý sau khi giao diện đã sẵn sàng
        }
    }

    private fun observeUserVerification(token: String) {
        lifecycleScope.launchWhenStarted {
            userPreferences.isVerified.collect { isVerified ->
                when {
                    isVerified == null -> startNewActivity(LoginActivity::class.java, true)
                    !isVerified -> startNewActivity(OtpVerificationActivity::class.java)
                    else -> startNewActivity(MainActivity::class.java, true)
                }
            }
        }
    }

    override fun getViewModel() = SplashRedirectViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) = ActivitySplashRedirectBinding.inflate(inflater)

    override fun getActivityRepository() = AuthRepository(retrofitInstance.buildApi(AuthApiService::class.java, ""), userPreferences)

}