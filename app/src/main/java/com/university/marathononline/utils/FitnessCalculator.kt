package com.university.marathononline.utils

import com.university.marathononline.data.models.EGender
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

//avgSpeed = km/h
//https://pacompendium.com/running/
//Ngày truy cập: 07/12/2024
fun getMET(avgSpeed: Double): Double{
    return when {
        avgSpeed < 4.18 -> 0.0
        avgSpeed < 5.93 -> 3.3
        avgSpeed < 6.76 -> 6.5
        avgSpeed < 7.72 -> 7.8
        avgSpeed < 8.37 -> 8.5
        avgSpeed < 9.33 -> 9.0
        avgSpeed < 10.14 -> 9.3
        avgSpeed < 10.78 -> 10.5
        avgSpeed < 11.27 -> 11.0
        avgSpeed < 12.07 -> 11.8
        avgSpeed < 12.87 -> 12.0
        avgSpeed < 13.84 -> 12.5
        avgSpeed < 14.48 -> 13.0
        avgSpeed < 14.97 -> 14.8
        avgSpeed < 16.09 -> 14.8
        avgSpeed < 17.7 -> 16.8
        avgSpeed < 19.31 -> 18.5
        avgSpeed < 20.92 -> 19.8
        avgSpeed < 22.53 -> 23.0
        else -> 23.0
    }
}

//Cân nặng giả định dựa trên giới tính và số tuổi
fun getAvgWeightByGenderAndAge(gender: EGender, age: Int): Double{
    return when (gender) {
        EGender.MALE -> when {
            age in 13..18 -> 50.0
            age in 19..40 -> 57.0
            age in 41..60 -> 59.0
            age > 60 -> 58.0
            else -> 0.0
        }
        EGender.FEMALE -> when {
            age in 13..18 -> 45.0
            age in 19..40 -> 49.0
            age in 41..60 -> 51.0
            age > 60 -> 48.0
            else -> 0.0
        }
        else -> 0.0
    }
}

fun getAge(birthday: String): Int{
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val birthDate = LocalDate.parse(birthday, formatter)

    val currentDate = LocalDate.now()

    val period = Period.between(birthDate, currentDate)

    return period.years
}

fun calPace(avgSpeed: Double): Double {
    return 60/avgSpeed?:0.0
}

//kcal
fun calCalogies(avgSpeed: Double, avgWeight: Double, timeTaken: Long): Double{
    return (getMET(avgSpeed) * avgWeight * (timeTaken / 3600f))/1000
}
