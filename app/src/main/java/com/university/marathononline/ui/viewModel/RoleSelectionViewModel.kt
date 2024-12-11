package com.university.marathononline.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.models.ERole
import com.university.marathononline.data.repository.UserRepository

class RoleSelectionViewModel(
    private val repository: UserRepository
) : BaseViewModel(listOf(repository)) {
    private val _role: MutableLiveData<ERole> = MutableLiveData()
    val role: LiveData<ERole> get() = _role

    fun selectedRole(role: ERole) {
        _role.value = role
    }

    fun isRoleSelected() = _role.value != null
}