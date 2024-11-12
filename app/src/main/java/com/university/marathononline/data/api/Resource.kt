package com.university.marathononline.data.api

import okhttp3.ResponseBody
import java.io.IOException

sealed class Resource<out T> {
    data class Success<out T>(val value: T): Resource<T>()
    data class Failure<out T>(
        val isNetworkError: Boolean,
        val errorCode: Int?,
        val errorBody: ResponseBody?
    ) : Resource<T>() {

        fun getErrorMessage(): String {
            return try {
                val errorMessage = errorBody?.string()?.takeIf { it.isNotBlank() }
                    ?: "Unknown error occurred."

                if (errorMessage == "Unknown error occurred.") {
                    logUnknownError()
                }

                errorMessage
            } catch (e: IOException) {
                logIOExceptionError(e)
                "Error reading response body."
            }
        }

        private fun logUnknownError() {
            println("Error: The response body is empty or null.")
            println("Error Code: $errorCode")
            println("Is Network Error: $isNetworkError")
        }

        private fun logIOExceptionError(e: IOException) {
            println("IOException: ${e.message}")
            println("Error Code: $errorCode")
            println("Is Network Error: $isNetworkError")
        }
    }


    object Loading: Resource<Nothing> ()
}