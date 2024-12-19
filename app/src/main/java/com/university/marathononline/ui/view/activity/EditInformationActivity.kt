package com.university.marathononline.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.Observer
import com.university.marathononline.R.string.*
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.user.UserApiService
import com.university.marathononline.data.models.EGender
import com.university.marathononline.data.models.ERole
import com.university.marathononline.data.models.User
import com.university.marathononline.data.repository.UserRepository
import com.university.marathononline.databinding.ActivityEditInformationBinding
import com.university.marathononline.ui.viewModel.EditInformationViewModel
import com.university.marathononline.utils.*
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Calendar

class EditInformationActivity : BaseActivity<EditInformationViewModel, ActivityEditInformationBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntentExtras(intent)
        setUpObserve()
    }

    private fun handleIntentExtras(intent: Intent) {
        intent.apply {
            viewModel.apply {
                (getSerializableExtra(KEY_USER) as? User)?.let { setUser(it) }
            }
        }
    }

    private fun setUpObserve() {
        viewModel.user.observe(this, Observer {
            initializeUI()
            updateUI(it)
        })

        viewModel.updateResponse.observe(this, Observer {
            handleUpdateResponse(it)
        })
    }

    private fun handleUpdateResponse(resource: Resource<User>){
        when(resource){
            is Resource.Success -> {
                finishAndGoBack()
            }
            is Resource.Failure -> handleApiError(resource)
            else -> Unit
        }
    }

    private fun updateUI(user: User) {
        binding.apply {
            fullNameEditText.setText(user.fullName)
            phoneNumberEditText.setText(user.phoneNumber)
            addressText.setText(user.address)
            if(user.role == ERole.RUNNER) {
                radioMale.isChecked = user.gender == EGender.MALE
                radioFemale.isChecked = user.gender == EGender.FEMALE
                setBirthday(user.birthday)
            } else {
                genderLabel.visible(false)
                genderGroup.visible(false)
                birthdayLabel.visible(false)
                birthdayGroup.visible(false)
            }
        }
    }

    private fun setBirthday(birthday: String) {
        val (day, month, year) = DateUtils.convertToDayMonthYear(birthday) ?: return
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        binding.spinnerDay.setSelection(day - 1)
        binding.spinnerMonth.setSelection(month - 1)
        binding.spinnerYear.setSelection(currentYear - year)
    }

    private fun initializeUI() {
        setupDateOfBirthSpinners()

        binding.apply {
            val genderMap = mapOf(
                radioMale to EGender.MALE,
                radioFemale to EGender.FEMALE
            )

            genderMap.forEach { (button, role) ->
                button.setOnClickListener {
                    viewModel.selectedGender(role)
                }
            }
        }

        setupClickListeners()
    }

    private fun setupClickListeners(){
        binding.apply {
            buttonBack.setOnClickListener{ finish() }
            saveButton.setOnClickListener { onSaveButtonCLick() }
        }
    }

    private fun onSaveButtonCLick() {
        if(!validateFields())
            return

        binding.apply {
            DateUtils.convertToDateString(
                spinnerDay.getIntegerSelectedItem(),
                spinnerMonth.getIntegerSelectedItem(),
                spinnerYear.getIntegerSelectedItem()
            )?.let {
                viewModel.updateUser(
                    fullNameEditText.text.toString(),
                    phoneNumberEditText.text.toString(),
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
            binding.spinnerDay.adapter = adapterSpinner(1, 31, context)
            binding.spinnerMonth.adapter = adapterSpinner(1, 12, context)
            binding.spinnerYear.adapter = adapterSpinner(1900, currentYear, context)
        }
    }

    private fun validateFields(): Boolean {
        val errorMessage = getMessage(error_field_required)
        binding.apply {
            val fields = listOf(
                fullNameEditText to fullnameErrorText,
                phoneNumberEditText to phoneNumberErrorText,
                addressText to addressErrorText
            )
            return fields.all { (field, errorText) -> !field.isEmpty(errorText, errorMessage) }
        }
    }

    override fun getViewModel() = EditInformationViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) = ActivityEditInformationBinding.inflate(inflater)

    override fun getActivityRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val api = retrofitInstance.buildApi(UserApiService::class.java, token)
        return listOf(UserRepository(api))
    }
}