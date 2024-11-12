package com.university.marathononline.ui.view.activity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.Observer
import com.university.marathononline.R
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.user.UserApiService
import com.university.marathononline.data.repository.UserRepository
import com.university.marathononline.databinding.ActivityRegisterOrganizerInfoBinding
import com.university.marathononline.ui.viewModel.RegisterViewModel
import com.university.marathononline.utils.*

class RegisterOrganizerInfoActivity : BaseActivity<RegisterViewModel, ActivityRegisterOrganizerInfoBinding, UserRepository>() {
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
                    startNewActivity(SignUpSuccessActivity::class.java, true)
                }
                is Resource.Loading -> {
                    binding.progressBar.visible(true)
                }
                is Resource.Failure -> handleApiError(it)
            }
        })
    }

    private fun initializeUI() {
        binding.progressBar.visible(false)

        binding.apply {
            progressBar.visible(false)

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

            registerButton.setOnClickListener {
                onRegisterButtonClick()
            }

            buttonBack.setOnClickListener {
                finishAndGoBack()
            }
        }
    }

    private fun validatePhoneNumber() {
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


    private fun onRegisterButtonClick() {
        if(!validateFields())
            return

        binding.apply {
            viewModel.register(
                usernameText.text.toString(),
                phoneNumberText.text.toString(),
                "",
                addressText.text.toString()
            )
    }
        }

    private fun validateFields(): Boolean {
        val errorMessage = getMessage(R.string.error_field_required)
        binding.apply {
            return !usernameText.isEmpty(usernameErrorText, errorMessage) ||
                    !addressText.isEmpty(addressErrorText, errorMessage) ||
                    !phoneNumberText.isEmpty(phoneNumberErrorText, errorMessage)
        }

    }

    override fun getViewModel() = RegisterViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) = ActivityRegisterOrganizerInfoBinding.inflate(inflater)

    override fun getActivityRepository() = UserRepository(retrofitInstance.buildApi(UserApiService::class.java))
}
