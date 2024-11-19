package com.university.marathononline.ui.view.activity

import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.Observer
import com.university.marathononline.R
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.user.UserApiService
import com.university.marathononline.data.models.EGender
import com.university.marathononline.data.models.User
import com.university.marathononline.data.repository.UserRepository
import com.university.marathononline.databinding.ActivityEditInformationBinding
import com.university.marathononline.ui.viewModel.EditInformationViewModel
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.KEY_USER
import com.university.marathononline.utils.adapterSpinner
import com.university.marathononline.utils.getMessage
import com.university.marathononline.utils.handleApiError
import com.university.marathononline.utils.isEmpty
import com.university.marathononline.utils.startNewActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Calendar

class EditInformationActivity : BaseActivity<EditInformationViewModel, ActivityEditInformationBinding, UserRepository>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (intent.getSerializableExtra(KEY_USER) as? User)?.let { viewModel.setUser(it) }

        initializeUI()
        setUpObserve()
    }

    private fun setUpObserve() {
        viewModel.user.observe(this, Observer {
            updateUI(it)
        })

        viewModel.updateResponse.observe(this, Observer {
            when(it){
                is Resource.Success -> {
                    startNewActivity(InformationActivity::class.java, true)
                }
                is Resource.Loading -> {}
                is Resource.Failure -> handleApiError(it)
            }
        })
    }

    private fun updateUI(user: User) {
        binding.apply {
            fullNameEditText.setText(user.fullName)
            phoneNumberEditText.setText(user.phoneNumber)
            addressText.setText(user.address)
            radioMale.isChecked = user.gender == EGender.MALE
            radioFemale.isChecked = user.gender == EGender.FEMALE
            val (day, month, year) = DateUtils.convertToDayMonthYear(user.birthday)!!
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            binding.spinnerDay.setSelection(day - 1)
            binding.spinnerMonth.setSelection(month - 1)
            binding.spinnerYear.setSelection(currentYear - year)
        }
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

            buttonBack.setOnClickListener{ finish() }

            saveButton.setOnClickListener { onSaveButtonCLick() }
        }
    }

    private fun onSaveButtonCLick() {
        if(!validateFields())
            return

        binding.apply {
            DateUtils.convertToDateString(
                spinnerDay.selectedItem.toString().toInt(),
                spinnerMonth.selectedItem.toString().toInt(),
                spinnerYear.selectedItem.toString().toInt()
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
        binding.spinnerDay.adapter = adapterSpinner(1, 31, this)
        binding.spinnerMonth.adapter = adapterSpinner(1, 12, this)
        binding.spinnerYear.adapter = adapterSpinner(1900, currentYear, this)
    }

    private fun validateFields(): Boolean {
        val errorMessage = getMessage(R.string.error_field_required)
        binding.apply {
            return !fullNameEditText.isEmpty(fullnameErrorText, errorMessage) ||
                    !phoneNumberEditText.isEmpty(phoneNumberEditText, errorMessage) ||
                    !addressText.isEmpty(addressErrorText, errorMessage)
        }
    }

    override fun getViewModel() = EditInformationViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) = ActivityEditInformationBinding.inflate(inflater)

    override fun getActivityRepository(): UserRepository {
        val token = runBlocking { userPreferences.authToken.first() }
        val api = retrofitInstance.buildApi(UserApiService::class.java, token)
        return UserRepository(api)
    }
}