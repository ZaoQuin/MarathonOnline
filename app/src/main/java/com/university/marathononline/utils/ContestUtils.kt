package com.university.marathononline.utils

import android.content.Context
import androidx.core.content.ContextCompat.getColor;
import com.university.marathononline.R.color.*
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.EContestStatus
import java.time.LocalDateTime

fun getContestStatusColor(context: Context, status: EContestStatus): Int {
    return when (status) {
        EContestStatus.ACTIVE -> getColor(context, dark_main_color)
        EContestStatus.FINISHED -> getColor(context, gray)
        EContestStatus.CANCELLED -> getColor(context, red)
        else -> getColor(context, text_color)
    }
}

fun isStarting(contest: Contest): Boolean{
    return (DateUtils.convertStringToLocalDateTime(contest.startDate!!).isBefore(
        LocalDateTime.now()) ||
            DateUtils.convertStringToLocalDateTime(contest.startDate!!).isEqual(LocalDateTime.now()))
            && contest.status == EContestStatus.ACTIVE
}

fun enableRegister(contest: Contest): Boolean {
    return !DateUtils.convertStringToLocalDateTime(contest.registrationDeadline!!).isBefore(LocalDateTime.now())
}