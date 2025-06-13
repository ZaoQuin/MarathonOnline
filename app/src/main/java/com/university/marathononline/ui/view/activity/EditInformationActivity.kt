package com.university.marathononline.ui.view.activity

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.university.marathononline.R
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
import android.os.Handler
import android.os.Looper

class EditInformationActivity : BaseActivity<EditInformationViewModel, ActivityEditInformationBinding>() {
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            binding.avatarImage.setImageURI(it)
            selectedImageUri = it
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            pickImageLauncher.launch("image/*")
        } else {
            Toast.makeText(this, "Bạn cần cấp quyền để chọn ảnh", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntentExtras(intent)
        setUpObserve()
        showShimmerLoading()
    }

    private fun showShimmerLoading() {
        binding.apply {
            shimmerFrameLayout.visibility = android.view.View.VISIBLE
            mainContentLayout.visibility = android.view.View.GONE
            shimmerFrameLayout.startShimmer()
        }
    }

    private fun hideShimmerLoading() {
        binding.apply {
            shimmerFrameLayout.stopShimmer()
            shimmerFrameLayout.visibility = android.view.View.GONE
            mainContentLayout.visibility = android.view.View.VISIBLE
        }
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
            Handler(Looper.getMainLooper()).postDelayed({
                initializeUI()
                updateUI(it)
                hideShimmerLoading()
            }, 1000)
        })

        viewModel.updateResponse.observe(this, Observer {
            handleUpdateResponse(it)
        })

        viewModel.uploadAvatarResponse.observe(this@EditInformationActivity) { result ->
            when (result) {
                is Resource.Success -> {
                    Toast.makeText(
                        this@EditInformationActivity,
                        "Đăng tải ảnh thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                    saveInformation()
                }
                is Resource.Loading -> showLoadingDialog()
                is Resource.Failure -> {
                    hideLoadingDialog()
                    result.fetchErrorMessage()
                    Toast.makeText(
                        this@EditInformationActivity,
                        "Lỗi upload ảnh: ${result.errorMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> Unit
            }
        }
    }

    private fun handleUpdateResponse(resource: Resource<User>){
        when(resource){
            is Resource.Loading -> showLoadingDialog()
            is Resource.Success -> {
                hideLoadingDialog()
                Toast.makeText(
                    this@EditInformationActivity,
                    "Cập nhật thành công",
                    Toast.LENGTH_SHORT
                ).show()
                finishAndGoBack()
            }
            is Resource.Failure -> {
                hideLoadingDialog()
                handleApiError(resource)
            }
            else -> Unit
        }
    }

    private fun showLoadingDialog() {
        binding.saveButton.isEnabled = false
    }

    private fun hideLoadingDialog() {
        binding.saveButton.isEnabled = true
    }

    private fun updateUI(user: User) {
        binding.apply {
            fullNameEditText.setText(user.fullName)
            phoneNumberEditText.setText(user.phoneNumber)
            addressText.setText(user.address)

            if(user.avatarUrl.isNullOrEmpty()){
                avatarImage.setImageResource(R.drawable.example_avatar)
            } else {
                Glide.with(this@EditInformationActivity)
                    .load(user.avatarUrl)
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.example_avatar)
                    .into(avatarImage)
            }

            if(user.role == ERole.RUNNER) {
                radioMale.isChecked = user.gender == EGender.MALE
                radioFemale.isChecked = user.gender == EGender.FEMALE
                setBirthday(user.birthday!!)
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

            editAvatarButton.setOnClickListener{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }

    private fun onSaveButtonCLick() {
        if(!validateFields())
            return
        if (selectedImageUri != null){
            viewModel.uploadAvatar(selectedImageUri!!, this@EditInformationActivity)
        } else {
            saveInformation()
        }
    }

    private fun saveInformation(){
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
            binding.spinnerYear.adapter = adapterSpinner(1900, currentYear, context, true)
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

    override fun onDestroy() {
        super.onDestroy()
        binding.shimmerFrameLayout.stopShimmer()
    }

    override fun getViewModel() = EditInformationViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) = ActivityEditInformationBinding.inflate(inflater)

    override fun getActivityRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val api = retrofitInstance.buildApi(UserApiService::class.java, token)
        return listOf(UserRepository(api))
    }
}