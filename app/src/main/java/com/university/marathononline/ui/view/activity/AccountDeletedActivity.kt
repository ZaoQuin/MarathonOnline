package com.university.marathononline.ui.view.activity

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.data.api.auth.AuthApiService
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.databinding.ActivityAccountDeletedBinding
import com.university.marathononline.ui.viewModel.AccountDeletedViewModel
import com.university.marathononline.utils.finishAndGoBack
import com.university.marathononline.utils.startNewActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class AccountDeletedActivity :
    BaseActivity<AccountDeletedViewModel, ActivityAccountDeletedBinding, AuthRepository>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeUI()
    }
    private fun initializeUI() {
        binding.apply {

            loginAgainButton.setOnClickListener { handleLoginAgain() }
        }
    }

    private fun handleLoginAgain(){
        viewModel.clearAuthenticated()
        startNewActivity(LoginActivity::class.java, true)
    }


    override fun getViewModel() = AccountDeletedViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) = ActivityAccountDeletedBinding.inflate(inflater)

    override fun getActivityRepository(): AuthRepository {
        val token = runBlocking { userPreferences.authToken.first() }
        val api = retrofitInstance.buildApi(AuthApiService::class.java, token)
        return AuthRepository(api, userPreferences)
    }
}