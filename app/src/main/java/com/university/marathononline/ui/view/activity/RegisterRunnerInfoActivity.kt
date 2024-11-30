package com.university.marathononline.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.Observer
import com.university.marathononline.R.string.error_field_required
import com.university.marathononline.R.string.error_invalid_phone_number
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.user.UserApiService
import com.university.marathononline.data.models.EGender
import com.university.marathononline.data.models.User
import com.university.marathononline.data.repository.UserRepository
import com.university.marathononline.databinding.ActivityRegisterRunnerInfoBinding
import com.university.marathononline.ui.viewModel.RegisterViewModel
import com.university.marathononline.utils.*
import handleApiError
import java.util.Calendar

class RegisterRunnerInfoActivity : BaseActivity<RegisterViewModel, ActivityRegisterRunnerInfoBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntentExtras(intent)
        initializeUI()
        setUpObserve()
    }

    private fun handleIntentExtras(intent: Intent) {
        intent.apply {
            viewModel.apply {
                getStringExtra(KEY_FULL_NAME)?.let { setFullName(it) }
                getStringExtra(KEY_EMAIL)?.let { setEmail(it) }
                getStringExtra(KEY_PASSWORD)?.let { setPassword(it) }
            }
        }
    }

    private fun setUpObserve() {
        viewModel.registerResponse.observe(this, Observer {
            binding.apply {
                progressBar.visible(it == Resource.Loading)
                registerButton.enable(it == Resource.Loading)
            }
            handleRegisterResponse(it)
        })
    }

    private fun handleRegisterResponse(resource: Resource<User>){
        when(resource){
            is Resource.Success -> {
                startNewActivity(SignUpSuccessActivity::class.java, true)
            }
            is Resource.Failure -> handleApiError(resource)
            else -> Unit
        }
    }

    private fun initializeUI(){
        setupDateOfBirthSpinners()

        binding.apply {
            progressBar.visible(false)

            val genderMap = mapOf(
                radioMale to EGender.MALE,
                radioFemale to EGender.FEMALE
            )

            genderMap.forEach { (button, role) ->
                button.setOnClickListener {
                    viewModel.selectedGender(role)
                }
            }

            val editTextMap = mapOf(
                usernameText to usernameErrorText,
                addressText to addressErrorText
            )

            editTextMap.forEach { (text, error) ->
                text.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) validateNormalEditText(text, error)
                }
            }
        }

        setupClickListeners()
    }

    private fun setupClickListeners(){
        binding.apply {
            registerButton.setOnClickListener {
                onRegisterButtonClick()
            }

            buttonBack.setOnClickListener {
                finishAndGoBack()
            }
        }
    }

    private fun onRegisterButtonClick() {
        if(!validateFields())
            return

        binding.apply {
            DateUtils.convertToDateString(
                spinnerDay.selectedItem.toString().toInt(),
                spinnerMonth.selectedItem.toString().toInt(),
                spinnerYear.selectedItem.toString().toInt()
            )?.let {
                viewModel.register(
                    usernameText.text.toString(),
                    phoneNumberText.text.toString(),
                    it,
                    addressText.text.toString()
                )
            }
        }

    }

    private fun setupDateOfBirthSpinners(){
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val context = this
        binding.apply {
            spinnerDay.adapter = adapterSpinner(1, 31, context)
            spinnerMonth.adapter = adapterSpinner(1, 12, context)
            spinnerYear.adapter = adapterSpinner(1900, currentYear, context)
        }
    }

    private fun validateFields(): Boolean {
        val errorMessage = getMessage(error_field_required)
        binding.apply {
            val fields = listOf(
                usernameText to usernameErrorText,
                addressText to addressErrorText,
                phoneNumberText to phoneNumberErrorText
            )
            return phoneNumberText.isPhoneNumber(phoneNumberErrorText, getString(error_invalid_phone_number))
                    && fields.any { (field, errorText) -> !field.isEmpty(errorText, errorMessage) }
        }
    }

    override fun getViewModel() = RegisterViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) = ActivityRegisterRunnerInfoBinding.inflate(inflater)

    override fun getActivityRepositories() = listOf(UserRepository(retrofitInstance.buildApi(UserApiService::class.java)))
}