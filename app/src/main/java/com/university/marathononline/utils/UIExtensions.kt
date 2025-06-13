package com.university.marathononline.utils

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.university.marathononline.R

fun AppCompatActivity.setDoneIconColor(editText: EditText) {
    val doneIcon = editText.compoundDrawablesRelative[2]
    doneIcon?.let {
        DrawableCompat.setTint(it, ContextCompat.getColor(this, R.color.main_color))
        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, it, null)
    }
}

fun AppCompatActivity.getMessage(id: Int): String {
    return ContextCompat.getString(this, id)
}

fun AppCompatActivity.validateNormalEditText(text: EditText, error: TextView) {
    if (!text.isEmpty(error, getMessage(R.string.error_field_required))) {
        setDoneIconColor(text)
    }
}

fun adapterSpinner(min: Int, max: Int, context: Context, reversed: Boolean = false): ArrayAdapter<String> {
    val arrange = (min..max).map { it.toString() }.let { if (reversed) it.reversed() else it }
    val adapter = ArrayAdapter(context, R.layout.spinner_item, arrange)
    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
    return adapter
}