package com.university.marathononline.data.repository

import android.content.Context
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.notify.NotificationApiService
import com.university.marathononline.data.models.Notification
import com.university.marathononline.data.api.contest.CreateAllNotificationRequest
import com.university.marathononline.data.api.notify.CreateGroupNotificationRequest
import com.university.marathononline.data.api.notify.CreateIndividualNotificationRequest
import com.university.marathononline.data.api.notify.UpdateFCMTokenRequest

class NotificationRepository(
    private val api: NotificationApiService
) : BaseRepository(){

    suspend fun getNotificationsByJWT() = safeApiCall {
        api.getNotificationsByJWT()
    }

    suspend fun addIndividualNotification(request: CreateIndividualNotificationRequest) = safeApiCall {
        api.addIndividualNotification(request)
    }

    suspend fun addAllRunnerNotification(request: CreateAllNotificationRequest) = safeApiCall {
        api.addAllRunnerNotification(request)
    }

    suspend fun addGroupNotification(request: CreateGroupNotificationRequest) = safeApiCall {
        api.addGroupNotification(request)
    }

    suspend fun readNotify(notification: Notification) = safeApiCall {
        api.readNotify(notification)
    }

    suspend fun updateFCMToken(token: String, context: Context) = safeApiCall {
        val request = UpdateFCMTokenRequest.createWithSystemInfo(token, context)
        api.updateFCMToken(request)
    }


    suspend fun markAllAsRead() = safeApiCall {
        api.markAllAsRead()
    }

    suspend fun getUnreadCount() = safeApiCall {
        api.getUnreadCount()
    }
}