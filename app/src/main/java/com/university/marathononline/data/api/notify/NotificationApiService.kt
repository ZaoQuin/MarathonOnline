package com.university.marathononline.data.api.notify

import com.university.marathononline.data.models.Notification
import retrofit2.http.GET

interface NotificationApiService {

    @GET("/api/v1/notification/my-notify")
    suspend fun getNotificationsByJWT(): List<Notification>
}