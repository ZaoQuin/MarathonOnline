package com.university.marathononline.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.models.Contest
import com.university.marathononline.utils.DateUtils

class RunnerContestsViewModel(
): BaseViewModel(listOf()) {
    private val _contests = MutableLiveData<List<Contest>>()
    val contests: LiveData<List<Contest>> get() = _contests

    fun setContests(contests: List<Contest>){
        val sortedContests = contests.sortedBy {
            it.startDate?.let { it1 -> DateUtils.convertStringToLocalDateTime(it1) }
        }
        _contests.value = sortedContests
    }
}