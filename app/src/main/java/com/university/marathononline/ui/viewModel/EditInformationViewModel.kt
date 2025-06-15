package com.university.marathononline.ui.viewModel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.models.EGender
import com.university.marathononline.data.models.User
import com.university.marathononline.data.repository.UserRepository
import com.university.marathononline.data.response.StringResponse
import com.university.marathononline.utils.FileUtils
import kotlinx.coroutines.launch

class EditInformationViewModel(
    private val repository: UserRepository
) : BaseViewModel(listOf(repository)) {
    private val _user: MutableLiveData<User> = MutableLiveData()
    val user: LiveData<User> get() = _user

    private val _selectedGender: MutableLiveData<EGender> = MutableLiveData()
    val selectedGender: LiveData<EGender> get() = _selectedGender

    private val _updateResponse: MutableLiveData<Resource<User>> = MutableLiveData()
    val updateResponse: LiveData<Resource<User>> get() = _updateResponse

    private val _uploadAvatarResponse = MutableLiveData<Resource<StringResponse>>()
    val uploadAvatarResponse: LiveData<Resource<StringResponse>> get() = _uploadAvatarResponse

    fun setUser(user: User) {
        _user.value = user
    }

    fun selectedGender(gender: EGender) {
        _selectedGender.value = gender
    }

    fun updateUser(fullname: String, phoneNumber: String, birthday: String, address: String, avatarLink: String?) {
        viewModelScope.launch {
            _updateResponse.value = Resource.Loading
            _user.value?.let {
                Log.d("Edit User Before", it.fullName!!)
                val request = it.copy(
                    fullName = fullname,
                    phoneNumber = phoneNumber,
                    gender = selectedGender.value ?: EGender.MALE,
                    birthday = birthday,
                    address = address,
                    refreshToken = it.refreshToken ?: "",
                    avatarUrl = avatarLink
                )
                Log.d("Edit User", request.fullName!!)
                _updateResponse.value = repository.updateUser(request)
            }
        }
    }

    fun uploadAvatar(uri: Uri, context: Context) {
        viewModelScope.launch {
            if (!FileUtils.isValidImageType(uri, context)) {
                _uploadAvatarResponse.value = Resource.Failure(
                    isNetworkError = false,
                    errorCode = null,
                    errorBody = null,
                    errorMessage = FileUtils.getInvalidTypeErrorMessage()
                )
                return@launch
            }

            _uploadAvatarResponse.value = Resource.Loading
            val filePart = FileUtils.prepareFilePart("file", uri, context)

            if (filePart == null) {
                _uploadAvatarResponse.value = Resource.Failure(
                    isNetworkError = false,
                    errorCode = null,
                    errorBody = null,
                    errorMessage = "Không thể xử lý file ảnh. Vui lòng chọn file khác."
                )
                return@launch
            }

            val userId = user.value?.id ?: return@launch
            _uploadAvatarResponse.value = repository.uploadAvatar(userId, filePart)
        }
    }
}