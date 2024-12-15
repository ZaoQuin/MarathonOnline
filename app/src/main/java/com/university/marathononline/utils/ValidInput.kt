package com.university.marathononline.utils

import android.widget.EditText
import android.widget.TextView
import java.time.LocalDateTime

fun isValidPassword(password: String): Boolean {
    val passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*?&#])[A-Za-z\\d@\$!%*?&#]{8,}\$"
    return Regex(passwordPattern).matches(password)
}

fun isValidEmail(email: String): Boolean {
    val emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    return email.matches(emailPattern.toRegex())
}

fun isValidPhoneNumber(phoneNumber: String): Boolean {
    val regex = "^[+]?[0-9]{10,13}\$".toRegex()
    return phoneNumber.matches(regex)
}

fun EditText.isValidPassword(errorTextView: TextView, errorText: String): Boolean {
    return if (isValidPassword(getString())){
        errorTextView.text = null
        true
    } else {
        errorTextView.text = errorText
        false
    }
}

fun EditText.isPhoneNumber(errorTextView: TextView, errorText: String): Boolean {
    return if (isValidPhoneNumber(getString())){
        errorTextView.text = null
        true
    } else {
        errorTextView.text = errorText
        false
    }
}



fun EditText.isNonNegative(errorTextView: TextView, errorText: String): Boolean{
    val number = text.toString().toFloatOrNull()
    return if(number!! > 0) {
        errorTextView.text = errorText
        true
    } else {
        errorTextView.text = errorText
        false
    }
}

fun EditText.validateField(errorTextView: TextView, errorMessage: String): Boolean {
    return !this.isEmpty(errorTextView, errorMessage)
}


fun EditText.isEmail(errorTextView: TextView, errorText: String): Boolean{
    return if (isValidEmail(getString())){
        errorTextView.text = null
        true
    } else {
        errorTextView.text = errorText
        false
    }
}

fun isMatch(text1: EditText, text2: EditText, errorText1: TextView, errorText2: TextView, errorMessage: String): Boolean {
    return if (compareTextValues(text1, text2)) {
        errorText1.text = null
        errorText2.text = null
        true
    } else {
        errorText1.text = errorMessage
        errorText2.text = errorMessage
        false
    }
}

fun EditText.isEmpty(errorTextView: TextView, errorMessage: String): Boolean {
    return if (getString().isEmpty()) {
        errorTextView.text = errorMessage
        true
    } else {
        errorTextView.text = null
        false
    }
}

fun isValueNull(valueIsNull: Boolean, errorTextView: TextView, errorMessage: String): Boolean{
    return if(valueIsNull){
        errorTextView.text = errorMessage
        true
    } else {
        errorTextView.text = null
        false
    }
}

fun isABeforeB(startDate: LocalDateTime, endDate: LocalDateTime, errorTextView: TextView, errorMessage: String): Boolean {
    return if (startDate != null && endDate != null && !startDate.isBefore(endDate)) {
        errorTextView.text = errorMessage
        false
    } else {
        errorTextView.text = null
        true
    }
}