package com.university.marathononline.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.university.marathononline.data.api.RetrofitInstance
import com.university.marathononline.data.api.auth.AuthApiService
import com.university.marathononline.data.response.UserPreferences
import com.university.marathononline.ui.view.activity.SplashRedirectActivity
import com.university.marathononline.utils.startNewActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

abstract class BaseActivity<VM: BaseViewModel, B: ViewBinding, R: BaseRepository>: AppCompatActivity() {

    protected lateinit var userPreferences: UserPreferences
    protected lateinit var binding: B
    protected lateinit var viewModel: VM
    protected val retrofitInstance = RetrofitInstance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userPreferences = UserPreferences(this)
        binding = getActivityBinding(layoutInflater)
        val factory = ViewModelFactory(getActivityRepository())
        viewModel = ViewModelProvider(this, factory)[getViewModel()]

        lifecycleScope.launch {
            userPreferences.authToken.first()
        }

        setContentView(binding.root)
    }

    abstract fun getViewModel(): Class<VM>

    abstract fun getActivityBinding(inflater: LayoutInflater): B

    abstract fun getActivityRepository(): R

    fun logout() = lifecycleScope.launch {
        val authToken = userPreferences.authToken.first()
        val api = retrofitInstance.buildApi(AuthApiService::class.java, authToken)
        viewModel.logout(api)
        userPreferences.clearAuth()
        startNewActivity(SplashRedirectActivity::class.java, true)
    }
}