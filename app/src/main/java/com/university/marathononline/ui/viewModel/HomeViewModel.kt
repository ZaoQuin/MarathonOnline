package com.university.marathononline.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.models.Notification
import com.university.marathononline.data.models.TrainingDay
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.data.repository.NotificationRepository
import com.university.marathononline.data.repository.TrainingDayRepository
import com.university.marathononline.data.response.GetContestsResponse
import kotlinx.coroutines.launch

class HomeViewModel(
    private val contestRepository: ContestRepository,
    private val authRepository: AuthRepository,
    private val trainingDayRepository: TrainingDayRepository,
    private val notificationRepository: NotificationRepository
) : BaseViewModel(listOf(contestRepository, authRepository, trainingDayRepository)) {
    private val _notifications = MutableLiveData<List<Notification>>()
    val notifications: LiveData<List<Notification>> get() = _notifications

    private val _trainingDay = MutableLiveData<TrainingDay>()
    val trainingDay: LiveData<TrainingDay> get() = _trainingDay

    private val _contests: MutableLiveData<Resource<GetContestsResponse>> = MutableLiveData()
    val contests: LiveData<Resource<GetContestsResponse>> get() = _contests

    private val _getNotifiesResponse: MutableLiveData<Resource<List<Notification>>> = MutableLiveData()
    val getNotifiesResponse: LiveData<Resource<List<Notification>>> get() = _getNotifiesResponse

    private val _getCurrentTrainingDay: MutableLiveData<Resource<TrainingDay>> = MutableLiveData()
    val getCurrentTrainingDay: LiveData<Resource<TrainingDay>> get() = _getCurrentTrainingDay

    fun setNotifications(notifications: List<Notification>){
        this._notifications.value = notifications;
    }

    fun getActiveContests(){
        viewModelScope.launch {
            _contests.value = Resource.Loading
            _contests.value = contestRepository.getHomeContests()
        }
    }

    fun getCurrentTrainingDay(){
        viewModelScope.launch {
            _getCurrentTrainingDay.value = Resource.Loading
            _getCurrentTrainingDay.value = trainingDayRepository.getCurrentTrainingDay()
        }
    }

    fun getNotifications() {
        viewModelScope.launch {
            _getNotifiesResponse.value = Resource.Loading
            _getNotifiesResponse.value = notificationRepository.getNotificationsByJWT()
        }
    }
}
