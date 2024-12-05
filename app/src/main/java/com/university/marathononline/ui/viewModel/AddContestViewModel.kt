package com.university.marathononline.ui.viewModel

import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.repository.ContestRepository

class AddContestViewModel(
    private val contestRepository: ContestRepository
): BaseViewModel(listOf(contestRepository)) {
}