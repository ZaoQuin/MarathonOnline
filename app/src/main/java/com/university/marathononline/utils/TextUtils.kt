package com.university.marathononline.utils

import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.Spinner
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

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

fun EditText.togglePasswordVisibility(drawableVisible: Int, drawableInvisible: Int) {
    val isPasswordVisible = this.transformationMethod !is PasswordTransformationMethod

    if (isPasswordVisible) {
        this.transformationMethod = PasswordTransformationMethod.getInstance()
        this.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableInvisible, 0)
    } else {
        this.transformationMethod = null
        this.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        this.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableVisible, 0)
    }

    this.setSelection(this.text.length)
}

fun Spinner.getIntegerSelectedItem(): Int{
    return selectedItem.toString().toInt()
}

fun convertToVND(amount: BigDecimal): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return formatter.format(amount)
}

fun formatDistance(distance: Double): String {
    return if(distance < 1){
        "${(distance * 1000).toInt()} m"
    } else {
        String.format("%.2f km", distance)
    }
}

fun formatCalogies(calories: Double): String {
    return if(calories < 1){
        String.format("%.2f cal", calories * 1000)
    } else {
        String.format("%.2f kcal", calories)
    }
}

fun formatPace(pace: Double): String {
    return if(pace >= 0){
        String.format("%.2f phút/km", pace)
    } else {
        "Chưa có dữ liệu"
    }
}

fun formatSpeed(speed: Double): String {
    return String.format("%.2f km/h", speed)
}