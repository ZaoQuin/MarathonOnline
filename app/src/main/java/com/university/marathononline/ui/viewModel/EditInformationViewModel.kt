package com.university.marathononline.ui.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.models.EGender
import com.university.marathononline.data.models.User
import com.university.marathononline.data.repository.UserRepository
import kotlinx.coroutines.launch

class EditInformationViewModel(
    private val repository: UserRepository
): BaseViewModel(repository) {
    private val _user: MutableLiveData<User> = MutableLiveData()
    val user: LiveData<User> get() = _user

    private val _selectedGender: MutableLiveData<EGender> = MutableLiveData()
    val selectedGender: LiveData<EGender> get() = _selectedGender

    private val _updateResponse: MutableLiveData<Resource<User>> = MutableLiveData()
    val updateResponse: LiveData<Resource<User>> get() = _updateResponse

    fun setUser(user: User) {
        _user.value = user
    }

    fun selectedGender(gender: EGender){
        _selectedGender.value = gender
    }

    fun updateUser(fullname: String, phoneNumber: String, birthday: String, address: String) {
        viewModelScope.launch {
            _updateResponse.value = Resource.Loading
            _user.value?.let {
                Log.d("Edit User Before", it.fullName)
                val request = it.copy(
                    fullName = fullname,
                    phoneNumber = phoneNumber,
                    gender = selectedGender.value ?: EGender.MALE,
                    birthday = birthday,
                    address = address,
                    refreshToken = it.refreshToken ?: ""
                )
                Log.d("Edit User", request.fullName)
                _updateResponse.value = repository.updateUser(request)
            }
        }
    }
}