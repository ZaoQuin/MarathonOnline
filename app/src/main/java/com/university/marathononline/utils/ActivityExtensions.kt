package com.university.marathononline.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.Reward
import com.university.marathononline.data.models.User


fun Activity.finishAndGoBack() {
    finish()
}



fun <A : Activity> Fragment.startNewActivity(
    activity: Class<A>,
    clearBackStack: Boolean = false
) {
    Intent(requireContext(), activity).also {
        if (clearBackStack) it.flags = NO_BACK_STACK_FLAGS
        startActivity(it)
    }
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
            is Reward -> intent.putExtra(key, value)
            is List<*> -> {
                when {
                    value.isEmpty() -> intent.putExtra(key, ArrayList<Any>())
                    value[0] is Contest -> {
                        @Suppress("UNCHECKED_CAST")
                        intent.putExtra(key, ArrayList(value as List<Contest>))
                    }
                    value[0] is Reward -> {
                        @Suppress("UNCHECKED_CAST")
                        intent.putExtra(key, ArrayList(value as List<Reward>))
                    }
                    else -> throw IllegalArgumentException("Unsupported list type: ${value::class.java}")
                }
            }
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
            is Reward -> intent.putExtra(key, value)
            is List<*> -> {
                when {
                    value.isEmpty() -> intent.putExtra(key, ArrayList<Any>())
                    value[0] is Contest -> {
                        @Suppress("UNCHECKED_CAST")
                        intent.putExtra(key, ArrayList(value as List<Contest>))
                    }
                    value[0] is Reward -> {
                        @Suppress("UNCHECKED_CAST")
                        intent.putExtra(key, ArrayList(value as List<Reward>))
                    }
                    else -> throw IllegalArgumentException("Unsupported list type: ${value::class.java}")
                }
            }
            else -> throw IllegalArgumentException("Unsupported data type")
        }
    }

    if (clearBackStack) intent.flags = NO_BACK_STACK_FLAGS

    this.startActivity(intent)
}


fun <A : Activity> Fragment.startNewActivity(
    activity: Class<A>,
    data: Map<String, Any> = emptyMap(),
    clearBackStack: Boolean = false
) {
    val intent = Intent(requireContext(), activity)
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
            is Reward -> intent.putExtra(key, value)
            is List<*> -> {
                when {
                    value.isEmpty() -> intent.putExtra(key, ArrayList<Any>())
                    value[0] is Contest -> {
                        @Suppress("UNCHECKED_CAST")
                        intent.putExtra(key, ArrayList(value as List<Contest>))
                    }
                    value[0] is Reward -> {
                        @Suppress("UNCHECKED_CAST")
                        intent.putExtra(key, ArrayList(value as List<Reward>))
                    }
                    else -> throw IllegalArgumentException("Unsupported list type: ${value::class.java}")
                }
            }
            else -> throw IllegalArgumentException("Unsupported data type")
        }
    }

    if (clearBackStack) intent.flags = NO_BACK_STACK_FLAGS

    startActivity(intent)
}
