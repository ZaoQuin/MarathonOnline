package com.university.marathononline.ui.view.activity

import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.Observer
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.data.api.user.UserApiService
import com.university.marathononline.data.models.ERole
import com.university.marathononline.data.repository.UserRepository
import com.university.marathononline.databinding.ActivityRoleSelectionBinding
import com.university.marathononline.ui.viewModel.RoleSelectionViewModel
import com.university.marathononline.utils.*

class RoleSelectionActivity : BaseActivity<RoleSelectionViewModel, ActivityRoleSelectionBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeUI()
        setUpObserver()
    }

    private fun initializeUI(){
        binding.apply {
            continueButton.enable(false)

            val roleMap = mapOf(
                runnerRole to ERole.RUNNER,
                organizerRole to ERole.ORGANIZER
            )

            roleMap.forEach { (button, role) ->
                button.setOnClickListener {
                    viewModel.selectedRole(role)
                }
            }

            continueButton.setOnClickListener{
                viewModel.role.value?.let {
                    val data = mapOf(KEY_ROLE to it.name)

                    startNewActivity(RegisterBasicInformationActivity::class.java,
                        data)
                }
            }

            loginText.setOnClickListener{
                startNewActivity(LoginActivity::class.java, true)
            }
        }
    }

    private fun setUpObserver() {
        viewModel.role.observe(this, Observer {
            binding.continueButton.enable(viewModel.isRoleSelected())
        })
    }

    override fun getViewModel() = RoleSelectionViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) = ActivityRoleSelectionBinding.inflate(inflater)

    override fun getActivityRepositories() = listOf(UserRepository(retrofitInstance.buildApi(UserApiService::class.java)))
}