package com.university.marathononline.utils

import android.content.Context
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getString;
import androidx.core.content.ContextCompat.getColor;
import com.university.marathononline.R.color.*
import com.university.marathononline.R.string.*
import com.university.marathononline.data.models.EContestStatus
import com.university.marathononline.data.models.ERegistrationStatus

fun getContestStatusText(context: Context, status: EContestStatus): String {
    return when (status) {
        EContestStatus.PENDING -> getString(context, contest_status_pending)
        EContestStatus.ACTIVE -> getString(context, contest_status_active)
        EContestStatus.FINISHED -> getString(context, contest_status_finished)
        EContestStatus.CANCELLED -> getString(context, contest_status_cancelled)
        EContestStatus.NOT_APPROVAL -> getString(context, contest_status_not_approval)
        EContestStatus.DELETED -> getString(context, contest_status_deleted)
        else -> getString(context, contest_status_unknown)
    }
}

fun getContestStatusColor(context: Context, status: EContestStatus): Int {
    return when (status) {
        EContestStatus.ACTIVE -> getColor(context, dark_main_color)
        EContestStatus.FINISHED -> getColor(context, gray)
        EContestStatus.CANCELLED -> getColor(context, red)
        else -> getColor(context, text_color)
    }
}

fun updateCompletionStatus(context: Context, textView: TextView, status: ERegistrationStatus) {
    val completed = status == ERegistrationStatus.COMPLETED
    textView.text = if (completed) {
        context.getString(contest_completed)
    } else {
        context.getString(contest_not_completed)
    }

    textView.setTextColor(
        ContextCompat.getColor(
            context,
            if (completed) main_color else red
        )
    )
}