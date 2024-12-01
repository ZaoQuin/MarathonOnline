package com.university.marathononline.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.User
import com.university.marathononline.ui.view.activity.RoleSelectionActivity


fun Activity.finishAndGoBack() {
    finish()
}

fun <A : Activity> Activity.startNewActivity(
    activity: Class<A>,
    clearBackStack: Boolean = false
) {
    Intent(this, activity).also {
        if (clearBackStack) it.flags = NO_BACK_STACK_FLAGS
        startActivity(it)
    }
}

fun <A : Activity> Context.startNewActivity(
    activity: Class<A>,
    clearBackStack: Boolean = false
) {
    Intent(this, activity).also {
        if (clearBackStack) it.flags = NO_BACK_STACK_FLAGS
        this.startActivity(it)
    }
}

fun <A : Activity> Activity.startNewActivity(
    activity: Class<A>,
    data: Map<String, Any> = emptyMap(),
    clearBackStack: Boolean = false
) {
    val intent = Intent(this, activity)
    for ((key, value) in data) {
        when (value) {
            is String -> intent.putExtra(key, value)
            is Int -> intent.putExtra(key, value)
            is Boolean -> intent.putExtra(key, value)
            is Float -> intent.putExtra(key, value)
            is Double -> intent.putExtra(key, value)
            is Long -> intent.putExtra(key, value)
            is User -> intent.putExtra(key, value)
            is Contest -> intent.putExtra(key, value)
            else -> throw IllegalArgumentException("Unsupported data type")
        }
    }

    if (clearBackStack) intent.flags = NO_BACK_STACK_FLAGS

    startActivity(intent)
}

fun <A : Activity> Context.startNewActivity(
    activity: Class<A>,
    data: Map<String, Any> = emptyMap(),
    clearBackStack: Boolean = false
) {
    val intent = Intent(this, activity)
    for ((key, value) in data) {
        when (value) {
            is String -> intent.putExtra(key, value)
            is Int -> intent.putExtra(key, value)
            is Boolean -> intent.putExtra(key, value)
            is Float -> intent.putExtra(key, value)
            is Double -> intent.putExtra(key, value)
            is Long -> intent.putExtra(key, value)
            is User -> intent.putExtra(key, value)
            is Contest -> intent.putExtra(key, value)
            else -> throw IllegalArgumentException("Unsupported data type")
        }
    }

    if (clearBackStack) intent.flags = NO_BACK_STACK_FLAGS

    this.startActivity(intent)
}
