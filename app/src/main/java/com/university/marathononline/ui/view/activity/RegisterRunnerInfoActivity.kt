package com.university.marathononline.ui.view.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.lifecycle.Observer
import com.university.marathononline.R
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.user.UserApiService
import com.university.marathononline.data.models.EGender
import com.university.marathononline.data.repository.UserRepository
import com.university.marathononline.databinding.ActivityRegisterRunnerInfoBinding
import com.university.marathononline.ui.viewModel.RegisterViewModel
import com.university.marathononline.utils.*
import java.util.Calendar

class RegisterRunnerInfoActivity : BaseActivity<RegisterViewModel, ActivityRegisterRunnerInfoBinding, UserRepository>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.apply {
            getStringExtra(KEY_FULL_NAME)?.let {
                viewModel.setFullName(it)
            }
            getStringExtra(KEY_EMAIL)?.let {
                viewModel.setEmail(it)
            }
            getStringExtra(KEY_PASSWORD)?.let {
                viewModel.setPassword(it)
            }
        }


        initializeUI()
        setUpObserve()
    }

    private fun setUpObserve() {
        viewModel.registerResponse.observe(this, Observer {
            binding.progressBar.visible(false)

            when(it){
                is Resource.Success -> {
                    Toast.makeText(this, "Register Completed", Toast.LENGTH_SHORT)
                }
                is Resource.Loading -> {
                    binding.progressBar.visible(true)
                }
                is Resource.Failure -> handleApiError(it)
            }
        })
    }

    private fun initializeUI(){
        binding.progressBar.visible(false)

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

            phoneNumberText.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) validatePhoneNumber()
            }

            registerButton.setOnClickListener{
                onRegisterButtonClick()
            }

            buttonBack.setOnClickListener{
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
        binding.spinnerDay.adapter = adapterSpinner(1, 31, this)
        binding.spinnerMonth.adapter = adapterSpinner(1, 12, this)
        binding.spinnerYear.adapter = adapterSpinner(1900, currentYear, this)
    }

    private fun validateFields(): Boolean {
        val errorMessage = getMessage(R.string.error_field_required)
        binding.apply {
            return usernameText.checkEmpty(usernameErrorText, errorMessage) ||
                    addressText.checkEmpty(addressErrorText, errorMessage) ||
                    phoneNumberText.checkEmpty(phoneNumberErrorText, errorMessage)
        }
    }

    private fun validatePhoneNumber(){
        binding.apply {
            val errorMessage =
                if (isValidPhoneNumber(phoneNumberText.text.toString())) {
                    setDoneIconColor(phoneNumberText)
                    null
                }
                else
                    getMessage(R.string.error_invalid_phone_number)
            phoneNumberErrorText.text = errorMessage
        }
    }

    override fun getViewModel() = RegisterViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) = ActivityRegisterRunnerInfoBinding.inflate(inflater)

    override fun getActivityRepository() = UserRepository(retrofitInstance.buildApi(UserApiService::class.java))
}