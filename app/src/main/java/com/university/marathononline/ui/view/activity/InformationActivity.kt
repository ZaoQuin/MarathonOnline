package com.university.marathononline.ui.view.activity

import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.university.marathononline.R
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.auth.AuthApiService
import com.university.marathononline.data.models.User
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.databinding.ActivityInformationBinding
import com.university.marathononline.ui.viewModel.InformationViewModel
import com.university.marathononline.utils.KEY_EMAIL
import com.university.marathononline.utils.KEY_USER
import com.university.marathononline.utils.enable
import com.university.marathononline.utils.finishAndGoBack
import com.university.marathononline.utils.startNewActivity
import com.university.marathononline.utils.visible
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class InformationActivity : BaseActivity<InformationViewModel, ActivityInformationBinding>(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getUser()

        initializeUI()
        setUpObserve()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getUser()
    }

    private fun initializeUI() {
        binding.apply {
            progressBar.visible(false)
            editButton.enable(false)

            buttonBack.setOnClickListener{ finishAndGoBack() }

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

            deleteButton.setOnClickListener {
                val user = viewModel.user.value

                if(user!=null)
                    startNewActivity(DeleteUserAccountActivity::class.java,
                        mapOf(
                            KEY_EMAIL to user.email!!
                        )
                    )
            }

            changePasswordButton.setOnClickListener{
                val user = viewModel.user.value

                if(user!=null)
                    startNewActivity(ChangePasswordActivity::class.java,
                        mapOf(
                            KEY_EMAIL to user.email!!
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
                is Resource.Failure -> handleApiError(it)
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
            genderText.text = user.gender!!.value
            birthdayText.text = user.birthday
            emailText.text = user.email
            phoneNumberText.text = user.phoneNumber

            if(user!!.avatarUrl.isNullOrEmpty()){
                avartar.setImageResource(R.drawable.example_avatar)
            } else {
                Glide.with(this@InformationActivity)
                    .load(user.avatarUrl)
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.example_avatar)
                    .into(avartar)
            }
        }
    }

    override fun getViewModel(): Class<InformationViewModel> = InformationViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) = ActivityInformationBinding.inflate(inflater)

    override fun getActivityRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val api = retrofitInstance.buildApi(AuthApiService::class.java, token)
        return listOf(AuthRepository(api, userPreferences))
    }
}