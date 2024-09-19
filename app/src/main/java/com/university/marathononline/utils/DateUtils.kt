package com.university.marathononline.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    private const val DATE_FORMAT = "dd MMMM yyyy";

    fun getCurrentDate(): String {
        val currentDate = Date()
        return getFormattedDate(currentDate)
    }

    fun getFormattedDate(date: Date): String {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("vi", "VN"))
        return dateFormat.format(date)
    }
}