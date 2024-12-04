package com.university.marathononline.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.data.response.GetContestsResponse
import kotlinx.coroutines.launch

class SearchViewModel(
    private val contestRepository: ContestRepository
): BaseViewModel(listOf(contestRepository)) {
    private val _getContestReponse = MutableLiveData<Resource<GetContestsResponse>> ()
    val getContestReponse: LiveData<Resource<GetContestsResponse>> get() = _getContestReponse

    private val _contests = MutableLiveData<List<Contest>>()
    val contests: LiveData<List<Contest>> get() = _contests

    fun getActiveContests(){
        viewModelScope.launch {
            _getContestReponse.value = Resource.Loading
            _getContestReponse.value = contestRepository.getContests()
        }
    }

    fun setContests(contests: List<Contest>) {
        _contests.value = contests
    }

    fun search(query: String) {
        val filteredList = contests.value?.filter {
            it.name?.contains(query, ignoreCase = true) == true ||
                    it.organizer?.fullName?.contains(query, ignoreCase = true) == true
        }
        _results.value = filteredList?: emptyList()
    }

    private val _results = MutableLiveData<List<Contest>> ()
    val results: LiveData<List<Contest>> get() = _results

}