package com.university.marathononline.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.models.Feedback
import com.university.marathononline.data.repository.FeedbackRepository
import com.university.marathononline.data.repository.RecordRepository
import com.university.marathononline.data.response.StringResponse
import com.university.marathononline.data.models.Record
import kotlinx.coroutines.launch

class FeedBackViewModel(
    private val feedbackRepository: FeedbackRepository,
    private val recordRepository: RecordRepository
): BaseViewModel(listOf(feedbackRepository)) {

    private val _feedbacks = MutableLiveData<Resource<List<Feedback>>>()
    val feedbacks: LiveData<Resource<List<Feedback>>> = _feedbacks

    private val _createFeedbackResult = MutableLiveData<Resource<Feedback>>()
    val createFeedbackResult: LiveData<Resource<Feedback>> = _createFeedbackResult

    private val _deleteFeedbackResult = MutableLiveData<Resource<StringResponse>>()
    val deleteFeedbackResult: LiveData<Resource<StringResponse>> = _deleteFeedbackResult

    private val _getRecord = MutableLiveData<Resource<Record>>()
    val getRecord: LiveData<Resource<Record>> = _getRecord

    private val _getById = MutableLiveData<Resource<Feedback>>()
    val getById: LiveData<Resource<Feedback>> get() = _getById

    fun loadFeedbacksByRecord(recordId: Long) {
        viewModelScope.launch {
            _feedbacks.value = Resource.Loading
            _feedbacks.value = feedbackRepository.getFeedbacksByRecord(recordId)
        }
    }

    fun createFeedback(recordId: Long, message: String) {
        viewModelScope.launch {
            _createFeedbackResult.value = Resource.Loading
            _createFeedbackResult.value = feedbackRepository.createFeedback(recordId, message)
        }
    }

    fun deleteFeedback(feedbackId: Long) {
        viewModelScope.launch {
            _deleteFeedbackResult.value = Resource.Loading
            _deleteFeedbackResult.value = feedbackRepository.deleteFeedback(feedbackId)
        }
    }

    fun getRecord(id: Long) {
        viewModelScope.launch {
            _getRecord.value = Resource.Loading
            _getRecord.value = recordRepository.getById(id)
        }
    }

    fun getById(feedbackId: Long) {
        viewModelScope.launch {
            _getById.value  = Resource.Loading
            _getById.value = feedbackRepository.getById(feedbackId)
        }
    }
}