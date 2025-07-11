package com.university.marathononline.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    const val DAY_IN_MILLIS = 24 * 60 * 60 * 100
    private const val DATE_FORMAT = "dd MMMM yyyy"
    private const val DATE_FORMAT_BD = "d M yyyy"

    private val DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val SIMPLE_DATE_FORMAT = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val VIETNAMESE_DATE_FORMATTER = DateTimeFormatter.ofPattern("'Ngày' dd 'tháng' MM 'năm' yyyy", Locale("vi", "VN"))
    private val VIETNAMESE_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("'Ngày' dd 'tháng' MM 'năm' yyyy, HH:mm:ss", Locale("vi", "VN"))
    private val API_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    private val RECORD_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun isValidIsoDateTime(dateString: String): Boolean {
        return try {
            LocalDateTime.parse(dateString, ISO_DATE_TIME_FORMATTER)
            true
        } catch (e: DateTimeParseException) {
            false
        }
    }

    fun formatDateString(input: String): String {
        return try {
            val dateTime = LocalDateTime.parse(input) // input phải đúng định dạng ISO (vd: "2024-06-10T14:30:00")
            dateTime.format(DEFAULT_DATE_FORMATTER)
        } catch (e: Exception) {
            "" // hoặc xử lý lỗi tuỳ theo yêu cầu
        }
    }

    // Parse "yyyy-MM-dd HH:mm:ss" to LocalDateTime
    fun parseDateTimeStringToLocalDateTime(dateTimeStr: String): LocalDateTime? {
        return try {
            LocalDateTime.parse(dateTimeStr, API_DATE_TIME_FORMATTER)
        } catch (e: DateTimeParseException) {
            Log.e("DateUtils", "Invalid date format: $dateTimeStr", e)
            null
        }
    }

    // Existing methods...
    fun getDaysBetween(startDate: Long, endDate: Long): Long {
        val diffInMillis = endDate - startDate
        return diffInMillis / DAY_IN_MILLIS
    }

    fun getCurrentDate(): String {
        val currentDate = Date()
        return getFormattedDate(currentDate)
    }

    fun getFormattedDate(date: Date): String {
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale("vi", "VN"))
        return dateFormat.format(date)
    }

    fun formatDate(date: Date): String {
        return SIMPLE_DATE_FORMAT.format(date)
    }

    fun getCurrentDateString(): String {
        return formatDate(Date())
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
            val formattedDay = String.format("%02d", day)
            val formattedMonth = String.format("%02d", month)
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

    fun getDateFromText(dateString: String): Date? {
        return try {
            SIMPLE_DATE_FORMAT.parse(dateString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
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

    fun getFormattedYear(year: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        return "năm $year"
    }

    fun convertStringToLocalDateTime(dateString: String): LocalDateTime {
        return try {
            val correctedDateString = if (dateString.length > 23) {
                dateString.substring(0, 23)
            } else {
                dateString
            }
            LocalDateTime.parse(correctedDateString, ISO_DATE_TIME_FORMATTER)
        } catch (e: DateTimeParseException) {
            Log.e("DateTimeParse", "Invalid date format: $dateString", e)
            LocalDateTime.MIN
        }
    }

    fun convertDateToLocalDate(date: Date): LocalDate {
        return date.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    fun parseStringToLocalDate(dateString: String): LocalDate? {
        return try {
            val date = SIMPLE_DATE_FORMAT.parse(dateString)
            date?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
        } catch (e: Exception) {
            null
        }
    }

    fun addDaysToDate(dateString: String, days: Int): String {
        try {
            val date = SIMPLE_DATE_FORMAT.parse(dateString) ?: return dateString
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.add(Calendar.DAY_OF_MONTH, days)
            return SIMPLE_DATE_FORMAT.format(calendar.time)
        } catch (e: Exception) {
            return dateString
        }
    }

    fun isDateInRange(dateString: String, startDateString: String, endDateString: String): Boolean {
        try {
            val currentDate = parseStringToLocalDate(dateString) ?: return false
            val startDate = LocalDateTime.parse(startDateString, ISO_DATE_TIME_FORMATTER).toLocalDate()
            val endDate = LocalDateTime.parse(endDateString, ISO_DATE_TIME_FORMATTER).toLocalDate()
            return !currentDate.isBefore(startDate) && !currentDate.isAfter(endDate)
        } catch (e: Exception) {
            return false
        }
    }

    fun convertSecondsToHHMMSS(seconds: Long): String {
        val duration = Duration.ofSeconds(seconds)
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        val secs = duration.seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    fun convertToVietnameseDate(dateString: String): String {
        val dateTime = convertStringToLocalDateTime(dateString)
        return dateTime.format(VIETNAMESE_DATE_FORMATTER)
    }

    fun formatLocalDateTimeStrToDateTimeString(dateString: String): String {
        val localDateTime = convertStringToLocalDateTime(dateString)
        return localDateTime.format(RECORD_DATE_TIME_FORMATTER)
    }

    fun convertToVietnameseDateTime(dateString: String): String {
        val dateTime = convertStringToLocalDateTime(dateString)
        return dateTime.format(VIETNAMESE_DATETIME_FORMATTER)
    }

    fun formatTrainingDayString(
        trainingDay: com.university.marathononline.data.models.TrainingDay
    ): String {
        return try {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val currentDate = LocalDate.now()
            val trainingDateTime = LocalDateTime.parse(trainingDay.dateTime)
            val trainingDate = trainingDateTime.toLocalDate()
            val diffDays = ChronoUnit.DAYS.between(currentDate, trainingDate).toInt()
            val prefix = when (diffDays) {
                -1 -> "Hôm qua"
                0 -> "Hôm nay"
                1 -> "Ngày mai"
                else -> "Ngày thứ ${trainingDay.dayOfWeek} tuần ${trainingDay.week}"
            }
            "$prefix, ${trainingDate.format(formatter)}"
        } catch (e: Exception) {
            "Lỗi: ${e.message}"
        }
    }

    fun parseLocalDateTimeStr(dateTimeStr: String): LocalDate {
        return try {
            val dateTime = LocalDateTime.parse(dateTimeStr, ISO_DATE_TIME_FORMATTER)
            dateTime.toLocalDate()
        } catch (e: Exception) {
            LocalDate.now()
        }
    }

    fun formatToApiDateTimeString(dateTime: LocalDateTime?): String? {
        return dateTime?.format(API_DATE_TIME_FORMATTER)
    }

    fun isToday(trainingDateTimeStr: String): Boolean {
        return try {
            val trainingDate = LocalDateTime.parse(trainingDateTimeStr).toLocalDate()
            trainingDate == LocalDate.now()
        } catch (e: Exception) {
            false
        }
    }

    fun getDurationBetween(dateTimeStart: String, dateTimeEnd: String): Duration {
        return try {
            val start = LocalDateTime.parse(dateTimeStart, ISO_DATE_TIME_FORMATTER)
            val end = LocalDateTime.parse(dateTimeEnd, ISO_DATE_TIME_FORMATTER)
            Duration.between(start, end)
        } catch (e: Exception) {
            Log.e("DateUtils", "Invalid date string: $dateTimeStart or $dateTimeEnd", e)
            Duration.ZERO
        }
    }

    fun convertIntToHHMMSS(seconds: Int): String {
        val duration = Duration.ofSeconds(seconds.toLong())
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        val secs = duration.seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    fun convertLongToHHMMSS(seconds: Long): String {
        val duration = Duration.ofSeconds(seconds)
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        val secs = duration.seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }
}