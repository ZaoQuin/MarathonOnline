package com.university.marathononline.ui.view.activity

import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.Observer
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.auth.AuthApiService
import com.university.marathononline.data.models.User
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.databinding.ActivityInformationBinding
import com.university.marathononline.ui.viewModel.InformationViewModel
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.handleApiError
import com.university.marathononline.utils.visible
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class InformationActivity : BaseActivity<InformationViewModel, ActivityInformationBinding, AuthRepository>(){


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.progressBar.visible(false)

        viewModel.getUser()

        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.buttonLogout.setOnClickListener {
            logout()
        }

        setUpObserve()
    }

    private fun setUpObserve() {
        viewModel.user.observe(this, Observer {
            when(it) {
                is Resource.Success -> {
                    binding.progressBar.visible(false)
                    updateUI(it.value)
                }
                is Resource.Loading -> {
                    binding.progressBar.visible(true)

                }
                is Resource.Failure -> handleApiError(it, getActivityRepository())
            }
        })
    }

    private fun updateUI(user: User) {
        with(binding){
            fullnameText.text = user.fullName
            usernameText.text = "@" + user.username
            addressText.text = user.address
            genderText.text = user.gender.value
            birthdayText.text = DateUtils.getFormattedDate(user.birthday)
            emailText.text = user.email
            phoneNumberText.text = user.phoneNumber
        }
    }

    override fun getViewModel(): Class<InformationViewModel> = InformationViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) = ActivityInformationBinding.inflate(inflater)

    override fun getActivityRepository(): AuthRepository {
        val token = runBlocking { userPreferences.authToken.first() }
        val api = retrofitInstance.buildApi(AuthApiService::class.java, token)
        return AuthRepository(api, userPreferences)
    }
}