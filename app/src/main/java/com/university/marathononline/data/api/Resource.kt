package com.university.marathononline.data.api

import android.util.Log
import okhttp3.ResponseBody
import java.io.IOException

sealed class Resource<out T> {
    data class Success<out T>(val value: T): Resource<T>()
    data class Failure<out T>(
        val isNetworkError: Boolean,
        val errorCode: Int?,
        val errorBody: ResponseBody?,
        val errorMessage: String? = null
    ) : Resource<T>() {

        fun fetchErrorMessage(): String {
            return try {
                val parsedMessage = errorBody?.string()?.takeIf { it.isNotBlank() }
                    ?: "Unknown error occurred."

                if (parsedMessage == "Unknown error occurred.") {
                    logUnknownError()
                }

                parsedMessage
            } catch (e: IOException) {
                logIOExceptionError(e)
                "Error reading response body."
            }
        }

        private fun logUnknownError() {
            Log.e("ResourceError", "Error: The response body is empty or null.")
            Log.e("ResourceError", "Error Code: $errorCode")
            Log.e("ResourceError", "Is Network Error: $isNetworkError")
        }

        private fun logIOExceptionError(e: IOException) {
            Log.e("ResourceError", "IOException: ${e.message}")
            Log.e("ResourceError", "Error Code: $errorCode")
            Log.e("ResourceError", "Is Network Error: $isNetworkError")
            Log.e("ResourceError", "Error Message: $errorMessage")
        }
    }



    object Loading: Resource<Nothing> ()
}