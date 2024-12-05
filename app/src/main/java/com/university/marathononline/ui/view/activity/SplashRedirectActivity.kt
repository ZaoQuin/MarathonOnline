package com.university.marathononline.ui.view.activity

import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.data.api.auth.AuthApiService
import com.university.marathononline.data.models.ERole
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.databinding.ActivitySplashRedirectBinding
import com.university.marathononline.ui.viewModel.SplashRedirectViewModel
import com.university.marathononline.utils.startNewActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class SplashRedirectActivity :
    BaseActivity<SplashRedirectViewModel, ActivitySplashRedirectBinding>() {
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

    override fun onResume() {
        super.onResume()
        observeAuthToken()
    }

    private fun observeAuthToken() {
        lifecycleScope.launch {
            val token = userPreferences.authToken.firstOrNull()
            if (token == null) {
                startNewActivity(LoginActivity::class.java, true)
            } else {
                val isVerified = userPreferences.isVerified.firstOrNull()
                val isDeleted = userPreferences.isDeleted.firstOrNull()
                val role = userPreferences.role.firstOrNull()
                when {
                    isDeleted == true -> startNewActivity(AccountDeletedActivity::class.java, true)
                    isVerified == null -> startNewActivity(LoginActivity::class.java, true)
                    !isVerified -> startNewActivity(VerifyOTPActivity::class.java)
                    else -> {
                        when (role) {
                            ERole.RUNNER -> startNewActivity(MainActivity::class.java, true)
                            ERole.ORGANIZER -> startNewActivity(OrganizerMainActivity::class.java, true)
                            else -> startNewActivity(LoginActivity::class.java, true)
                        }
                    }
                }
            }
        }
    }

    override fun getViewModel() = SplashRedirectViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) =
        ActivitySplashRedirectBinding.inflate(inflater)

    override fun getActivityRepositories() = listOf(
        AuthRepository(
            retrofitInstance.buildApi(AuthApiService::class.java, ""),
            userPreferences
        )
    )

}