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
import com.university.marathononline.utils.KEY_EMAIL
import com.university.marathononline.utils.KEY_USER
import com.university.marathononline.utils.enable
import com.university.marathononline.utils.handleApiError
import com.university.marathononline.utils.startNewActivity
import com.university.marathononline.utils.visible
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class InformationActivity : BaseActivity<InformationViewModel, ActivityInformationBinding, AuthRepository>(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getUser()

        initializeUI()
        setUpObserve()
    }

    private fun initializeUI() {
        binding.apply {
            progressBar.visible(false)
            editButton.enable(false)

            buttonBack.setOnClickListener{ finish() }

            buttonLogout.setOnClickListener{ logout() }

            editButton.setOnClickListener{
                val user = viewModel.user.value

                if(user!=null)
                    startNewActivity(EditInformationActivity::class.java,
                        mapOf(
                            KEY_USER to user
                        )
                    )
            }

            deleteButton.setOnClickListener{

            }

            changePasswordButton.setOnClickListener{
                val user = viewModel.user.value

                if(user!=null)
                    startNewActivity(ChangePasswordActivity::class.java,
                        mapOf(
                            KEY_EMAIL to user.email
                        )
                    )
            }

        }
    }

    private fun setUpObserve() {
        viewModel.getUser.observe(this, Observer {
            binding.progressBar.visible(it == Resource.Loading)
            when(it) {
                is Resource.Success -> {
                    binding.editButton.enable(true)
                    viewModel.setUser(it.value)
                }
                is Resource.Failure -> handleApiError(it, getActivityRepository())
                else -> Unit
            }
        })

        viewModel.user.observe(this, Observer {
            updateUI(it)
        })
    }

    private fun updateUI(user: User) {
        with(binding){
            fullnameText.text = user.fullName
            usernameText.text = "@" + user.username
            addressText.text = user.address
            genderText.text = user.gender.value
            birthdayText.text = user.birthday
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