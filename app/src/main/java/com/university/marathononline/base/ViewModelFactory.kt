package com.university.marathononline.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.data.repository.UserRepository
import com.university.marathononline.ui.viewModel.EditInformationViewModel
import com.university.marathononline.ui.viewModel.ForgetPasswordViewModel
import com.university.marathononline.ui.viewModel.InformationViewModel
import com.university.marathononline.ui.viewModel.LoginViewModel
import com.university.marathononline.ui.viewModel.ChangePasswordWithOTPViewModel
import com.university.marathononline.ui.viewModel.RegisterBasicInformationViewModel
import com.university.marathononline.ui.viewModel.RegisterViewModel
import com.university.marathononline.ui.viewModel.RoleSelectionViewModel
import com.university.marathononline.ui.viewModel.SplashRedirectViewModel

class ViewModelFactory(
    private val repository: BaseRepository,
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when{
            modelClass.isAssignableFrom(SplashRedirectViewModel::class.java) -> SplashRedirectViewModel(repository as AuthRepository) as T
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(repository as AuthRepository) as T
            modelClass.isAssignableFrom(ChangePasswordWithOTPViewModel::class.java) -> ChangePasswordWithOTPViewModel(repository as AuthRepository) as T
            modelClass.isAssignableFrom(ForgetPasswordViewModel::class.java) -> ForgetPasswordViewModel(repository as UserRepository) as T
            modelClass.isAssignableFrom(InformationViewModel::class.java) -> InformationViewModel(repository as AuthRepository) as T
            modelClass.isAssignableFrom(RoleSelectionViewModel::class.java) -> RoleSelectionViewModel(repository as UserRepository) as T
            modelClass.isAssignableFrom(RegisterBasicInformationViewModel::class.java) -> RegisterBasicInformationViewModel(repository as UserRepository) as T
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> RegisterViewModel(repository as UserRepository) as T
            modelClass.isAssignableFrom(EditInformationViewModel::class.java) -> EditInformationViewModel(repository as UserRepository) as T
            else -> throw IllegalArgumentException("ViewModelClass Not Found")
        }
    }
}