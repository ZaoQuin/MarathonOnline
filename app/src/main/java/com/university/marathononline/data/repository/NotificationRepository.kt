package com.university.marathononline.data.repository

import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.notify.NotificationApiService

class NotificationRepository(
    private val api: NotificationApiService
) : BaseRepository(){
    suspend fun getNotificationsByJWT() = safeApiCall {
        api.getNotificationsByJWT()
    }
}