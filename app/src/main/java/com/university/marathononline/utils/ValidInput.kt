package com.university.marathononline.utils

import android.widget.EditText
import android.widget.TextView

fun isValidPassword(password: String): Boolean {
    val passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*?&#])[A-Za-z\\d@\$!%*?&#]{8,}\$"
    return Regex(passwordPattern).matches(password)
}

fun isValidEmail(email: String): Boolean {
    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
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
        errorText1.text = errorMessage
        errorText2.text = errorMessage
        true
    } else {
        errorText1.text = null
        errorText2.text = null
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