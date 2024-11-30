package com.university.marathononline.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.EContestStatus
import java.math.BigDecimal
import java.time.LocalDateTime

class SearchViewModel: ViewModel() {
    private val _results = MutableLiveData<List<Contest>> ()
    val results: LiveData<List<Contest>> get() = _results

    init {
        _results.value = listOf(
            Contest(
                id = 1,
                name = "Marathon 2024",
                description = "A marathon event for all runners.",
                distance = 42.195,
                startDate = LocalDateTime.now().plusDays(30).toString(),
                endDate = LocalDateTime.now().plusDays(32).toString(),
                fee = BigDecimal(50.0),
                maxMembers = 1000,
                status = EContestStatus.ACTIVE,
                createDate = LocalDateTime.now().toString(),
                registrationDeadline = LocalDateTime.now().plusDays(15).toString()
            ),
            Contest(
                id = 2,
                name = "5K Charity Run",
                description = "A 5K charity event.",
                distance = 5.0,
                startDate = LocalDateTime.now().plusDays(60).toString(),
                endDate = LocalDateTime.now().plusDays(61).toString(),
                fee = BigDecimal(25.0),
                maxMembers = 500,
                status = EContestStatus.PENDING,
                createDate = LocalDateTime.now().toString(),
                registrationDeadline = LocalDateTime.now().plusDays(20).toString()
            ),
            Contest(
                id = 3,
                name = "Ultra Marathon",
                description = "An ultra marathon event for experienced runners.",
                distance = 100.0,
                startDate = LocalDateTime.now().plusDays(90).toString(),
                endDate = LocalDateTime.now().plusDays(92).toString(),
                fee = BigDecimal(120.0),
                maxMembers = 300,
                status = EContestStatus.ACTIVE,
                createDate = LocalDateTime.now().toString(),
                registrationDeadline = LocalDateTime.now().plusDays(40).toString()
            ),
            Contest(
                id = 4,
                name = "Sprint Challenge",
                description = "A short sprint race for speed lovers.",
                distance = 0.1,
                startDate = LocalDateTime.now().plusDays(120).toString(),
                endDate = LocalDateTime.now().plusDays(121).toString(),
                fee = BigDecimal(15.0),
                maxMembers = 200,
                status = EContestStatus.PENDING,
                createDate = LocalDateTime.now().toString(),
                registrationDeadline = LocalDateTime.now().plusDays(50).toString()
            ),
            Contest(
                id = 5,
                name = "10K City Run",
                description = "A 10K race through the city streets.",
                distance = 10.0,
                startDate = LocalDateTime.now().plusDays(45).toString(),
                endDate = LocalDateTime.now().plusDays(46).toString(),
                fee = BigDecimal(30.0),
                maxMembers = 800,
                status = EContestStatus.ACTIVE,
                createDate = LocalDateTime.now().toString(),
                registrationDeadline = LocalDateTime.now().plusDays(15).toString()
            ),
            Contest(
                id = 6,
                name = "Night Run 2024",
                description = "Run through the city at night.",
                distance = 15.0,
                startDate = LocalDateTime.now().plusDays(150).toString(),
                endDate = LocalDateTime.now().plusDays(151).toString(),
                fee = BigDecimal(40.0),
                maxMembers = 1000,
                status = EContestStatus.ACTIVE,
                createDate = LocalDateTime.now().toString(),
                registrationDeadline = LocalDateTime.now().plusDays(60).toString()
            ),
            Contest(
                id = 7,
                name = "Mountain Challenge",
                description = "A challenging mountain marathon for thrill-seekers.",
                distance = 42.195,
                startDate = LocalDateTime.now().plusDays(180).toString(),
                endDate = LocalDateTime.now().plusDays(181).toString(),
                fee = BigDecimal(75.0),
                maxMembers = 400,
                status = EContestStatus.PENDING,
                createDate = LocalDateTime.now().toString(),
                registrationDeadline = LocalDateTime.now().plusDays(70).toString()
            ),
            Contest(
                id = 8,
                name = "Half Marathon 2024",
                description = "A half marathon event for all runners.",
                distance = 21.0975,
                startDate = LocalDateTime.now().plusDays(200).toString(),
                endDate = LocalDateTime.now().plusDays(201).toString(),
                fee = BigDecimal(40.0),
                maxMembers = 600,
                status = EContestStatus.ACTIVE,
                createDate = LocalDateTime.now().toString(),
                registrationDeadline = LocalDateTime.now().plusDays(80).toString()
            ),
            Contest(
                id = 9,
                name = "Cross Country Race",
                description = "A cross-country race through forests and fields.",
                distance = 30.0,
                startDate = LocalDateTime.now().plusDays(250).toString(),
                endDate = LocalDateTime.now().plusDays(251).toString(),
                fee = BigDecimal(35.0),
                maxMembers = 500,
                status = EContestStatus.PENDING,
                createDate = LocalDateTime.now().toString(),
                registrationDeadline = LocalDateTime.now().plusDays(100).toString()
            ),
            Contest(
                id = 10,
                name = "Charity Fun Run",
                description = "A fun run event for families and friends.",
                distance = 3.0,
                startDate = LocalDateTime.now().plusDays(280).toString(),
                endDate = LocalDateTime.now().plusDays(281).toString(),
                fee = BigDecimal(10.0),
                maxMembers = 1000,
                status = EContestStatus.ACTIVE,
                createDate = LocalDateTime.now().toString(),
                registrationDeadline = LocalDateTime.now().plusDays(120).toString()
            )
        )
    }
}