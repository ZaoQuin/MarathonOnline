package com.university.marathononline.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.data.repository.RaceRepository
import com.university.marathononline.data.repository.RegistrationRepository
import com.university.marathononline.data.repository.UserRepository
import com.university.marathononline.ui.viewModel.AccountDeletedViewModel
import com.university.marathononline.ui.viewModel.ChangePasswordViewModel
import com.university.marathononline.ui.viewModel.ContestDetailsViewModel
import com.university.marathononline.ui.viewModel.ContestViewModel
import com.university.marathononline.ui.viewModel.DailyStatisticsViewModel
import com.university.marathononline.ui.viewModel.DeleteUserAccountViewModel
import com.university.marathononline.ui.viewModel.EditInformationViewModel
import com.university.marathononline.ui.viewModel.ForgetPasswordViewModel
import com.university.marathononline.ui.viewModel.InformationViewModel
import com.university.marathononline.ui.viewModel.LoginViewModel
import com.university.marathononline.ui.viewModel.VerifyOTPViewModel
import com.university.marathononline.ui.viewModel.HomeViewModel
import com.university.marathononline.ui.viewModel.LeaderBoardViewModel
import com.university.marathononline.ui.viewModel.MonthlyStatisticsViewModel
import com.university.marathononline.ui.viewModel.PaymentConfirmationViewModel
import com.university.marathononline.ui.viewModel.ProfileViewModel
import com.university.marathononline.ui.viewModel.RecordViewModel
import com.university.marathononline.ui.viewModel.RegisterBasicInformationViewModel
import com.university.marathononline.ui.viewModel.RegisterViewModel
import com.university.marathononline.ui.viewModel.RoleSelectionViewModel
import com.university.marathononline.ui.viewModel.RunnerContestsViewModel
import com.university.marathononline.ui.viewModel.RunnerRewardsViewModel
import com.university.marathononline.ui.viewModel.SearchViewModel
import com.university.marathononline.ui.viewModel.SplashRedirectViewModel
import com.university.marathononline.ui.viewModel.YearlyStatisticsViewModel

class ViewModelFactory(
    private val repositories: List<BaseRepository>
): ViewModelProvider.Factory {

    private fun <T : BaseRepository> findRepository(type: Class<T>): T {
        return repositories.find { type.isInstance(it) } as T
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when{
            modelClass.isAssignableFrom(SplashRedirectViewModel::class.java) -> SplashRedirectViewModel(findRepository(AuthRepository::class.java)) as T
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(findRepository(AuthRepository::class.java)) as T
            modelClass.isAssignableFrom(VerifyOTPViewModel::class.java) -> VerifyOTPViewModel(findRepository(AuthRepository::class.java)) as T
            modelClass.isAssignableFrom(ForgetPasswordViewModel::class.java) -> ForgetPasswordViewModel(findRepository(UserRepository::class.java)) as T
            modelClass.isAssignableFrom(InformationViewModel::class.java) -> InformationViewModel(findRepository(AuthRepository::class.java)) as T
            modelClass.isAssignableFrom(RoleSelectionViewModel::class.java) -> RoleSelectionViewModel(findRepository(UserRepository::class.java)) as T
            modelClass.isAssignableFrom(RegisterBasicInformationViewModel::class.java) -> RegisterBasicInformationViewModel(findRepository(UserRepository::class.java)) as T
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> RegisterViewModel(findRepository(UserRepository::class.java)) as T
            modelClass.isAssignableFrom(EditInformationViewModel::class.java) -> EditInformationViewModel(findRepository(UserRepository::class.java)) as T
            modelClass.isAssignableFrom(ChangePasswordViewModel::class.java) -> ChangePasswordViewModel(findRepository(UserRepository::class.java)) as T
            modelClass.isAssignableFrom(DeleteUserAccountViewModel::class.java) -> DeleteUserAccountViewModel(findRepository(AuthRepository::class.java)) as T
            modelClass.isAssignableFrom(AccountDeletedViewModel::class.java) -> AccountDeletedViewModel(findRepository(AuthRepository::class.java)) as T
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(findRepository(ContestRepository::class.java)) as T
            modelClass.isAssignableFrom(ContestViewModel::class.java) -> ContestViewModel(findRepository(ContestRepository::class.java)) as T
            modelClass.isAssignableFrom(RecordViewModel::class.java) -> RecordViewModel(findRepository(RegistrationRepository::class.java),
                                                                                        findRepository(RaceRepository::class.java)) as T
            modelClass.isAssignableFrom(ContestDetailsViewModel::class.java) -> ContestDetailsViewModel(findRepository(ContestRepository::class.java)) as T
            modelClass.isAssignableFrom(PaymentConfirmationViewModel::class.java) -> PaymentConfirmationViewModel(findRepository(AuthRepository::class.java)) as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> ProfileViewModel(findRepository(RaceRepository::class.java),
                findRepository(ContestRepository::class.java)) as T
            modelClass.isAssignableFrom(DailyStatisticsViewModel::class.java) -> DailyStatisticsViewModel(findRepository(RaceRepository::class.java)) as T
            modelClass.isAssignableFrom(MonthlyStatisticsViewModel::class.java) -> MonthlyStatisticsViewModel(findRepository(RaceRepository::class.java)) as T
            modelClass.isAssignableFrom(YearlyStatisticsViewModel::class.java) -> YearlyStatisticsViewModel(findRepository(RaceRepository::class.java)) as T
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> SearchViewModel(findRepository(ContestRepository::class.java)) as T
            modelClass.isAssignableFrom(LeaderBoardViewModel::class.java) -> LeaderBoardViewModel() as T
            modelClass.isAssignableFrom(RunnerContestsViewModel::class.java) -> RunnerContestsViewModel() as T
            modelClass.isAssignableFrom(RunnerRewardsViewModel::class.java) -> RunnerRewardsViewModel() as T
            else -> throw IllegalArgumentException("ViewModelClass Not Found")
        }
    }
}