package com.university.marathononline.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.university.marathononline.base.BaseFragment
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.auth.AuthApiService
import com.university.marathononline.data.models.User
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.databinding.FragmentOrganizerInformationBinding
import com.university.marathononline.ui.view.activity.ChangePasswordActivity
import com.university.marathononline.ui.view.activity.DeleteUserAccountActivity
import com.university.marathononline.ui.view.activity.EditInformationActivity
import com.university.marathononline.ui.viewModel.InformationViewModel
import com.university.marathononline.utils.KEY_EMAIL
import com.university.marathononline.utils.KEY_USER
import com.university.marathononline.utils.enable
import com.university.marathononline.utils.startNewActivity
import com.university.marathononline.utils.visible
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class OrganizerInformationFragment : BaseFragment<InformationViewModel, FragmentOrganizerInformationBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getUser()

        initializeUI()
        setUpObserve()
    }

    private fun initializeUI() {
        binding.apply {
            progressBar.visible(false)
            editButton.enable(false)

            buttonLogout.setOnClickListener{ logout() }

            editButton.setOnClickListener{
                val user = viewModel.user.value

                if(user!=null)
                    startNewActivity(
                        EditInformationActivity::class.java,
                        mapOf(
                            KEY_USER to user
                        )
                    )
            }

            deleteButton.setOnClickListener {
                val user = viewModel.user.value

                if(user!=null)
                    startNewActivity(
                        DeleteUserAccountActivity::class.java,
                        mapOf(
                            KEY_EMAIL to user.email
                        )
                    )
            }

            changePasswordButton.setOnClickListener{
                val user = viewModel.user.value

                if(user!=null)
                    startNewActivity(
                        ChangePasswordActivity::class.java,
                        mapOf(
                            KEY_EMAIL to user.email
                        )
                    )
            }

        }
    }

    private fun setUpObserve() {
        viewModel.getUser.observe(viewLifecycleOwner, Observer {
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

        viewModel.user.observe(viewLifecycleOwner){
            updateUI(it)
        }
    }

    private fun updateUI(user: User) {
        with(binding){
            fullnameText.text = user.fullName
            usernameText.text = "@" + user.username
            addressText.text = user.address
            emailText.text = user.email
            phoneNumberText.text = user.phoneNumber
        }
    }

    override fun getViewModel() = InformationViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentOrganizerInformationBinding.inflate(inflater, container, false)

    override fun getFragmentRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val api = retrofitInstance.buildApi(AuthApiService::class.java, token)
        return listOf(AuthRepository(api, userPreferences))
    }
}