package com.university.marathononline.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    const val DAY_IN_MILLIS = 24 * 60 * 60 * 100
    private const val DATE_FORMAT = "dd MMMM yyyy";

    fun getDaysBetween(startDate: Long, endDate: Long): Long {
        val diffInMillis = endDate - startDate
        return diffInMillis / DAY_IN_MILLIS // Trả về số ngày giữa hai thời điểm
    }

    fun getCurrentDate(): String {
        val currentDate = Date()
        return getFormattedDate(currentDate)
    }

    fun getFormattedDate(date: Date): String {
        val dateFormat = SimpleDateFormat("dd MMMM, yyyy", Locale("vi", "VN"))
        return dateFormat.format(date)
    }
}