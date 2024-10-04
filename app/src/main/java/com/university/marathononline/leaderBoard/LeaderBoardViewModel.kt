package com.university.marathononline.leaderBoard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.university.marathononline.entity.EventHistory
import com.university.marathononline.entity.RaceResult
import com.university.marathononline.entity.User
import java.util.Date

class LeaderBoardViewModel: ViewModel() {
    private val _eventHistories = MutableLiveData<List<EventHistory>>()
    val eventHistories: LiveData<List<EventHistory>> get() = _eventHistories

    private val _users: MutableLiveData<List<User>> = MutableLiveData()
    val users: LiveData<List<User>> get() = _users

    init {
        // Dữ liệu mẫu cho User
        _users.value = listOf(
            User(
                1,
                "Nguyễn Văn A",
                "0123456789",
                Date(),
                "a@example.com",
                "Nam",
                "userA",
                "passwordA"
            ),
            User(2, "Trần Thị B", "0987654321", Date(), "b@example.com", "Nữ", "userB", "passwordB"),
            User(3, "Lê Văn C", "0112233445", Date(), "c@example.com", "Nam", "userC", "passwordC")
        )

        // Dữ liệu mẫu cho EventHistory
        _eventHistories.value = listOf(
            EventHistory(
                id = 1,
                userId = 1,
                eventId = 1,
                raceResults = listOf(
                    RaceResult(1, 1, 1, 5.0f, 1800L,  10.0f, Date()),
                    RaceResult(2, 1, 1, 10.0f, 3600L,  10.0f, Date())
                )
            ),
            EventHistory(
                id = 2,
                userId = 2,
                eventId = 1,
                raceResults = listOf(
                    RaceResult(3, 2, 1, 8.0f, 2400L,  12.0f, Date())
                )
            ),
            EventHistory(
                id = 3,
                userId = 3,
                eventId = 2,
                raceResults = listOf(
                    RaceResult(4, 3, 1, 15.0f, 4500L,  13.0f, Date())
                )
            )
        )
    }
}