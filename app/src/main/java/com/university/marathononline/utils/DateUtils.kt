package com.university.marathononline.utils

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
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

    fun getFormattedDate(dateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") // Định dạng bạn muốn
        return dateTime.format(formatter)
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

    fun getDateFromText(dateString: String): Date? {// Lấy giá trị chuỗi từ TextView

        // Định dạng ngày cần sử dụng để chuyển đổi
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Cập nhật định dạng ngày nếu cần

        return try {
            // Chuyển chuỗi thành đối tượng Date
            dateFormat.parse(dateString)
        } catch (e: Exception) {
            e.printStackTrace()
            null  // Nếu có lỗi, trả về null
        }
    }

    fun getCurrentMonthYear(): String {
        val calendar = Calendar.getInstance()
        val month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) ?: ""
        val year = calendar.get(Calendar.YEAR)
        return "$month $year"
    }

    fun getFormattedMonthYear(month: Int, year: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.YEAR, year)
        val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) ?: ""
        return "$monthName năm $year"
    }

    fun getFormattedYear(year: Int): String  {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        return "năm $year"
    }
}