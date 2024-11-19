package com.university.marathononline.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    const val DAY_IN_MILLIS = 24 * 60 * 60 * 100
    private const val DATE_FORMAT = "dd MMMM yyyy";
    private const val DATE_FORMAT_BD = "d M yyyy";

    fun getDaysBetween(startDate: Long, endDate: Long): Long {
        val diffInMillis = endDate - startDate
        return diffInMillis / DAY_IN_MILLIS // Trả về số ngày giữa hai thời điểm
    }

    fun getCurrentDate(): String {
        val currentDate = Date()
        return getFormattedDate(currentDate)
    }

    fun getFormattedDate(date: Date): String {
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale("vi", "VN"))
        return dateFormat.format(date)
    }

    fun convertStringToDate(day: String, month: String, year: String): Date? {
        return try {
            val formattedDay = String.format("%02d", day.toInt())
            val formattedMonth = String.format("%02d", month.toInt())

            val dateString = "$year-$formattedMonth-$formattedDay"

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.parse(dateString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun convertToDateString(day: Int, month: Int, year: Int): String? {
        return try {
            val formattedDay = String.format("%02d", day.toInt())
            val formattedMonth = String.format("%02d", month.toInt())

            val dateString = "$year-$formattedMonth-$formattedDay"

            dateString
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun convertToDayMonthYear(dateString: String): Triple<Int, Int, Int>? {
        return try {
            val parts = dateString.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val day = parts[2].toInt()
            Triple(day, month, year)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}