package com.university.marathononline.data.repository

import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.notify.NotificationApiService
import com.university.marathononline.data.models.Notification
import com.university.marathononline.data.request.CreateAllNotificationRequest
import com.university.marathononline.data.request.CreateGroupNotificationRequest
import com.university.marathononline.data.request.CreateIndividualNotificationRequest

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
}