package com.university.marathononline.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.university.marathononline.data.models.Contest
import java.util.Date

class ContestViewModel: ViewModel() {
    private val _contests = MutableLiveData<List<Contest>>()
    val contests: LiveData<List<Contest>> get() = _contests

    init {
        _contests.value = listOf(
            Contest(1,
                "Cuộc thi 1",
                Date(),
                Date(System.currentTimeMillis() + 86400000),
                "No Desc",
                12000.0,
                50.0),

            Contest(2,
                "Cuộc thi 2",
                Date(),
                Date(System.currentTimeMillis() + 86400000),
                "No Desc",
                12000.0,
                50.0),

            Contest(3,
                "Cuộc thi 2",
                Date(),
                Date(System.currentTimeMillis() + 86400000),
                "No Desc",
                12000.0,
                50.0),
            Contest(4,
                "Cuộc thi 2",
                Date(),
                Date(System.currentTimeMillis() + 86400000),
                "No Desc",
                12000.0,
                50.0),
            Contest(5,
                "Cuộc thi 2",
                Date(),
                Date(System.currentTimeMillis() + 86400000),
                "No Desc",
                12000.0,
                50.0)
        )
    }
}