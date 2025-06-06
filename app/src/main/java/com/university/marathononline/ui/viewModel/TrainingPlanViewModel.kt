package com.university.marathononline.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.models.SingleTrainingPlan
import com.university.marathononline.data.models.TrainingDay
import com.university.marathononline.data.models.TrainingFeedback
import com.university.marathononline.data.models.TrainingPlan
import com.university.marathononline.data.repository.TrainingDayRepository
import com.university.marathononline.data.repository.TrainingFeedbackRepository
import com.university.marathononline.data.repository.TrainingPlanRepository
import com.university.marathononline.data.request.InputTrainingPlanRequest
import com.university.marathononline.data.response.PageResponse
import com.university.marathononline.data.response.StringResponse
import kotlinx.coroutines.launch

class TrainingPlanViewModel (
    private val repository: TrainingPlanRepository,
    private val trainingDayRepository: TrainingDayRepository,
    private val trainingFeedbackRepository: TrainingFeedbackRepository
): BaseViewModel(listOf(repository, trainingDayRepository)) {

    private val _currentTrainingPlan: MutableLiveData<TrainingPlan> = MutableLiveData()
    val currentTrainingPlan: LiveData<TrainingPlan> get() = _currentTrainingPlan


    private val _currentTrainingDays: MutableLiveData<List<TrainingDay>> = MutableLiveData()
    val currentTrainingDays: LiveData<List<TrainingDay>> get() = _currentTrainingDays

    private val _getTrainingPlans:
            MutableLiveData<Resource<PageResponse<SingleTrainingPlan>>> = MutableLiveData()
    val getTrainingPlans:
            LiveData<Resource<PageResponse<SingleTrainingPlan>>> = _getTrainingPlans.distinctUntilChanged()

    private val _getCurrentTrainingPlan: MutableLiveData<Resource<TrainingPlan>> = MutableLiveData()
    val getCurrentTrainingPlan: LiveData<Resource<TrainingPlan>> = _getCurrentTrainingPlan.distinctUntilChanged()

    private val _createTrainingPlan: MutableLiveData<Resource<TrainingPlan>> = MutableLiveData()
    val createTrainingPlan: LiveData<Resource<TrainingPlan>> = _createTrainingPlan.distinctUntilChanged()

    private val _resetTrainingDay: MutableLiveData<Resource<StringResponse>> = MutableLiveData()
    val resetTrainingDay: LiveData<Resource<StringResponse>> = _resetTrainingDay.distinctUntilChanged()

    private val _getById: MutableLiveData<Resource<TrainingPlan>> = MutableLiveData()
    val getById: LiveData<Resource<TrainingPlan>> = _getById.distinctUntilChanged()

    private val _submitFeedback: MutableLiveData<Resource<TrainingFeedback>> = MutableLiveData()
    val submitFeedback: LiveData<Resource<TrainingFeedback>> = _submitFeedback.distinctUntilChanged()

    private val _getFeedback: MutableLiveData<Resource<TrainingFeedback>> = MutableLiveData()
    val getFeedback: LiveData<Resource<TrainingFeedback>> = _getFeedback.distinctUntilChanged()

    fun getCompletedTrainingPlans(page: Int, size: Int, startDate: String?, endDate: String?){
        viewModelScope.launch {
            _getTrainingPlans.value = Resource.Loading
            _getTrainingPlans.value = repository.getCompletedPlans(page, size, startDate, endDate);
        }
    }

    fun getArchivedTrainingPlans(page: Int, size: Int, startDate: String?, endDate: String?){
        viewModelScope.launch {
            _getTrainingPlans.value = Resource.Loading
            _getTrainingPlans.value = repository.getArchivedPlans(page, size, startDate, endDate);
        }
    }

    fun getCurrentTrainingPlan(){
        viewModelScope.launch {
            _getCurrentTrainingPlan.value = Resource.Loading
            _getCurrentTrainingPlan.value = repository.getCurrentTrainingPlan();
        }
    }

    fun createTrainingPlan(request: InputTrainingPlanRequest){
        viewModelScope.launch {
            _createTrainingPlan.value = Resource.Loading
            _createTrainingPlan.value = repository.generateTrainingPlan(request);
        }
    }

    fun getById(id: Long){
        viewModelScope.launch {
            println("Fetching plan with ID: $id")
            _getById.value = Resource.Loading
            _getById.value = repository.getTrainingPlanById(id);
        }
    }

    fun setCurrentTrainingPlan(trainingPlan: TrainingPlan){
        _currentTrainingPlan.value = trainingPlan
        _currentTrainingDays.value = trainingPlan.trainingDays
    }

    fun submitTrainingFeedback(trainingDayId: Long, feedback: TrainingFeedback) {
        viewModelScope.launch {
            _submitFeedback.value = Resource.Loading
            _submitFeedback.value = trainingFeedbackRepository.submitFeedback(trainingDayId, feedback)
        }
    }

    // Optional: Method to get feedback for a specific training day
    fun getTrainingDayFeedback(trainingDayId: Long) {
        viewModelScope.launch {
            _getFeedback.value = Resource.Loading
            _getFeedback.value = trainingFeedbackRepository.getFeedback(trainingDayId)
        }
    }
}
