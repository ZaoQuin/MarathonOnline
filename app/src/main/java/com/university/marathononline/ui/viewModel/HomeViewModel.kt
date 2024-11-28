package com.university.marathononline.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.EContestStatus
import com.university.marathononline.data.repository.ContestRepository
import java.math.BigDecimal
import java.time.LocalDateTime

class HomeViewModel(
    private val repository: ContestRepository
) : BaseViewModel(repository) {

    private val _events = MutableLiveData<List<Contest>>()
    val events: LiveData<List<Contest>> get() = _events

    init {
        loadEvents()
    }

    private fun loadEvents() {
        _events.value = listOf(
            Contest(
                id = 1,
                name = "Marathon 2024",
                description = "A marathon event for all runners.",
                distance = 42.195,
                startDate = LocalDateTime.now().plusDays(30),
                endDate = LocalDateTime.now().plusDays(32),
                fee = BigDecimal(50.0),
                maxMembers = 1000,
                status = EContestStatus.ONGOING,
                createDate = LocalDateTime.now(),
                registrationDeadline = LocalDateTime.now().plusDays(15)
            ),
            Contest(
                id = 2,
                name = "5K Charity Run",
                description = "A 5K charity event.",
                distance = 5.0,
                startDate = LocalDateTime.now().plusDays(60),
                endDate = LocalDateTime.now().plusDays(61),
                fee = BigDecimal(25.0),
                maxMembers = 500,
                status = EContestStatus.PENDING,
                createDate = LocalDateTime.now(),
                registrationDeadline = LocalDateTime.now().plusDays(20)
            ),
            Contest(
                id = 3,
                name = "Ultra Marathon",
                description = "An ultra marathon event for experienced runners.",
                distance = 100.0,
                startDate = LocalDateTime.now().plusDays(90),
                endDate = LocalDateTime.now().plusDays(92),
                fee = BigDecimal(120.0),
                maxMembers = 300,
                status = EContestStatus.ONGOING,
                createDate = LocalDateTime.now(),
                registrationDeadline = LocalDateTime.now().plusDays(40)
            ),
            Contest(
                id = 4,
                name = "Sprint Challenge",
                description = "A short sprint race for speed lovers.",
                distance = 0.1,
                startDate = LocalDateTime.now().plusDays(120),
                endDate = LocalDateTime.now().plusDays(121),
                fee = BigDecimal(15.0),
                maxMembers = 200,
                status = EContestStatus.PENDING,
                createDate = LocalDateTime.now(),
                registrationDeadline = LocalDateTime.now().plusDays(50)
            ),
            Contest(
                id = 5,
                name = "10K City Run",
                description = "A 10K race through the city streets.",
                distance = 10.0,
                startDate = LocalDateTime.now().plusDays(45),
                endDate = LocalDateTime.now().plusDays(46),
                fee = BigDecimal(30.0),
                maxMembers = 800,
                status = EContestStatus.ONGOING,
                createDate = LocalDateTime.now(),
                registrationDeadline = LocalDateTime.now().plusDays(15)
            ),
            Contest(
                id = 6,
                name = "Night Run 2024",
                description = "Run through the city at night.",
                distance = 15.0,
                startDate = LocalDateTime.now().plusDays(150),
                endDate = LocalDateTime.now().plusDays(151),
                fee = BigDecimal(40.0),
                maxMembers = 1000,
                status = EContestStatus.ONGOING,
                createDate = LocalDateTime.now(),
                registrationDeadline = LocalDateTime.now().plusDays(60)
            ),
            Contest(
                id = 7,
                name = "Mountain Challenge",
                description = "A challenging mountain marathon for thrill-seekers.",
                distance = 42.195,
                startDate = LocalDateTime.now().plusDays(180),
                endDate = LocalDateTime.now().plusDays(181),
                fee = BigDecimal(75.0),
                maxMembers = 400,
                status = EContestStatus.PENDING,
                createDate = LocalDateTime.now(),
                registrationDeadline = LocalDateTime.now().plusDays(70)
            ),
            Contest(
                id = 8,
                name = "Half Marathon 2024",
                description = "A half marathon event for all runners.",
                distance = 21.0975,
                startDate = LocalDateTime.now().plusDays(200),
                endDate = LocalDateTime.now().plusDays(201),
                fee = BigDecimal(40.0),
                maxMembers = 600,
                status = EContestStatus.ONGOING,
                createDate = LocalDateTime.now(),
                registrationDeadline = LocalDateTime.now().plusDays(80)
            ),
            Contest(
                id = 9,
                name = "Cross Country Race",
                description = "A cross-country race through forests and fields.",
                distance = 30.0,
                startDate = LocalDateTime.now().plusDays(250),
                endDate = LocalDateTime.now().plusDays(251),
                fee = BigDecimal(35.0),
                maxMembers = 500,
                status = EContestStatus.PENDING,
                createDate = LocalDateTime.now(),
                registrationDeadline = LocalDateTime.now().plusDays(100)
            ),
            Contest(
                id = 10,
                name = "Charity Fun Run",
                description = "A fun run event for families and friends.",
                distance = 3.0,
                startDate = LocalDateTime.now().plusDays(280),
                endDate = LocalDateTime.now().plusDays(281),
                fee = BigDecimal(10.0),
                maxMembers = 1000,
                status = EContestStatus.ONGOING,
                createDate = LocalDateTime.now(),
                registrationDeadline = LocalDateTime.now().plusDays(120)
            )
        )
    }
}
