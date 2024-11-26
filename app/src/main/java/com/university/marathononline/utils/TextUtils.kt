package com.university.marathononline.utils

import android.widget.EditText
import android.widget.Spinner

fun compareTextValues(editText1: EditText, editText2: EditText): Boolean {
    val value1 = editText1.text.toString().trim()
    val value2 = editText2.text.toString().trim()
    return value1 == value2
}

fun areAllFieldsFilled(vararg editTexts: EditText): Boolean {
    return editTexts.all { it.text.toString().trim().isNotEmpty() }
}

fun EditText.getString(): String{
    return text.toString().trim();
}

fun EditText.getInt(): Int{
    return text.toString().toInt();
}

fun Spinner.getIntegerSelectedItem(): Int{
    return selectedItem.toString().toInt()
}