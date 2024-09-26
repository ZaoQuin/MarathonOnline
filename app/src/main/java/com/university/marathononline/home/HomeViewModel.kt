package com.university.marathononline.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.university.marathononline.entity.Event
import java.util.Date

class HomeViewModel: ViewModel() {
    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> get() = _events

    init {
        _events.value = listOf(
            Event(1,
                "Cuôc thi 1",
                Date(),
                Date(System.currentTimeMillis() + 86400000),
                "No Desc",
                12000.0,
                50.0),

            Event(2,
                "Cuôc thi 2",
                Date(),
                Date(System.currentTimeMillis() + 86400000),
                "No Desc",
                12000.0,
                50.0),

            Event(3,
                "Cuôc thi 2",
                Date(),
                Date(System.currentTimeMillis() + 86400000),
                "No Desc",
                12000.0,
                50.0),
            Event(4,
                "Cuôc thi 2",
                Date(),
                Date(System.currentTimeMillis() + 86400000),
                "No Desc",
                12000.0,
                50.0),
            Event(5,
                "Cuôc thi 2",
                Date(),
                Date(System.currentTimeMillis() + 86400000),
                "No Desc",
                12000.0,
                50.0)
        )
    }
}