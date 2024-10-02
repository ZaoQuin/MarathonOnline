package com.university.marathononline.notify

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.university.marathononline.entity.Notify
import java.util.Date

class NotifyViewModel: ViewModel() {
    private val _notifies = MutableLiveData<List<Notify>>()
    val notifies: LiveData<List<Notify>> get() = _notifies

    init {
        _notifies.value = listOf(
            Notify(
                id = 1,
                title = "New Marathon Event",
                content = "Join us for the marathon event next week.",
                timeStamp = Date()
            ),
            Notify(
                id = 2,
                title = "Training Tips",
                content = "Check out the latest training tips to improve your performance.",
                timeStamp = Date()
            ),
            Notify(
                id = 3,
                title = "Update on Rewards",
                content = "The reward system has been updated. Check the details on the app.",
                timeStamp = Date()
            ),

            Notify(
                id = 4,
                title = "New Marathon Event",
                content = "Join us for the marathon event next week.",
                timeStamp = Date()
            ),
            Notify(
                id = 5,
                title = "Training Tips",
                content = "Check out the latest training tips to improve your performance.",
                timeStamp = Date()
            ),
            Notify(
                id = 6,
                title = "Update on Rewards",
                content = "The reward system has been updated. Check the details on the app.",
                timeStamp = Date()
            ),
            Notify(
                id = 7,
                title = "New Marathon Event",
                content = "Join us for the marathon event next week.",
                timeStamp = Date()
            ),
            Notify(
                id = 8,
                title = "Training Tips",
                content = "Check out the latest training tips to improve your performance.",
                timeStamp = Date()
            ),
            Notify(
                id = 9,
                title = "Update on Rewards",
                content = "The reward system has been updated. Check the details on the app.",
                timeStamp = Date()
            )
        )
    }
}