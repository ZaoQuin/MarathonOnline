package com.university.marathononline.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.data.repository.UserRepository
import com.university.marathononline.ui.viewModel.InformationViewModel
import com.university.marathononline.ui.viewModel.LoginViewModel

class ViewModelFactory(
    private val repository: BaseRepository
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when{
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(repository as AuthRepository) as T
            modelClass.isAssignableFrom(InformationViewModel::class.java) -> InformationViewModel(repository as AuthRepository) as T
            else -> throw IllegalArgumentException("ViewModelClass Not Found")
        }
    }
}