package com.university.marathononline.data.request

import android.content.Context
import android.provider.Settings
import com.google.gson.annotations.SerializedName

data class UpdateFCMTokenRequest(
    @SerializedName("fcmToken")
    val fcmToken: String,

    @SerializedName("deviceType")
    val deviceType: String = "ANDROID",

    @SerializedName("deviceId")
    val deviceId: String? = null,

    @SerializedName("appVersion")
    val appVersion: String? = null
) {
    companion object {
        fun getAppVersion(context: Context): String {
            return try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                packageInfo.versionName ?: "Unknown"
            } catch (e: Exception) {
                "Unknown"
            }
        }
        fun createWithSystemInfo(fcmToken: String, context: Context): UpdateFCMTokenRequest {
            val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            val appVersion = getAppVersion(context)
            return UpdateFCMTokenRequest(
                fcmToken = fcmToken,
                deviceId = deviceId,
                appVersion = appVersion
            )
        }
    }
}