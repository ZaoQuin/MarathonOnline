package com.university.marathononline.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.EContestStatus
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.SORT_BY_ASC
import kotlinx.coroutines.launch

class ContestManagementViewModel(
    private val contestRepository: ContestRepository
): BaseViewModel(listOf(contestRepository)) {
    private val _contests = MutableLiveData<List<Contest>>()
    val contests: LiveData<List<Contest>> get() = _contests

    private val _results = MutableLiveData<List<Contest>>()
    val results: LiveData<List<Contest>> get() = _results

    private val _keySearch = MutableLiveData<String>()
    val keySearch: LiveData<String> get() = _keySearch

    private val _filterStatus = MutableLiveData<EContestStatus>()
    val filterStatus: LiveData<EContestStatus> get() = _filterStatus

    private val _sortType = MutableLiveData<String>()
    val sortType: LiveData<String> get() = _sortType

    private val _getContestByJwtResponse = MutableLiveData<Resource<List<Contest>>>()
    val getContestByJwtResponse: LiveData<Resource<List<Contest>>> get() = _getContestByJwtResponse

    fun getMyContest() {
        viewModelScope.launch {
            _getContestByJwtResponse.value = Resource.Loading
            _getContestByJwtResponse.value = contestRepository.getContestsByJwt()
        }
    }

    fun setContest(contests: List<Contest>) {
        _contests.value = contests
    }
    fun setResult(contests: List<Contest>) {
        _results.value = contests.sortedByDescending { DateUtils.convertStringToLocalDateTime(it.createDate!!) }
    }

    fun setSearchKey(key: String) {
        _keySearch.value = key
    }

    fun setFilterStatus(status: EContestStatus?) {
        _filterStatus.value = status?:null
    }

    fun setSortType(sortType: String?) {
        _sortType.value = sortType?:null
    }

    fun applySearchAndFiltersAndSort() {
        var contests = contests.value ?: emptyList()
        contests = search(contests)
        contests = filter(contests)
        contests = sort(contests)
        _results.value = contests
    }

    fun search(contests: List<Contest>): List<Contest> {
        val query = keySearch.value ?: ""
        return contests.filter {
            it.name?.contains(query, ignoreCase = true) == true ||
                    it.organizer?.fullName?.contains(query, ignoreCase = true) == true
        }
    }

    fun filter(contests: List<Contest>): List<Contest> {
        val status = filterStatus.value
        return if (status == null) {
            contests // Return all contests if no filter is selected
        } else {
            contests.filter { it.status == status } // Apply status filter
        }
    }

    fun sort(contests: List<Contest>): List<Contest> {
        val sortType = sortType.value
        return if (sortType == null) {
            contests // No sorting, return the original list
        } else {
            if (sortType == SORT_BY_ASC) {
                contests.sortedBy { DateUtils.convertStringToLocalDateTime(it.createDate!!) }
            } else {
                contests.sortedByDescending { DateUtils.convertStringToLocalDateTime(it.createDate!!) }
            }
        }
    }
}