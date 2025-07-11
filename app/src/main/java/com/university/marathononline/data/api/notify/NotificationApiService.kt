package com.university.marathononline.data.api.notify

import com.university.marathononline.data.models.Notification
import com.university.marathononline.data.api.contest.CreateAllNotificationRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface NotificationApiService {

    @GET("/api/v1/notification/my-notify")
    suspend fun getNotificationsByJWT(): List<Notification>

    @POST("/api/v1/notification/individual")
    suspend fun addIndividualNotification(@Body request: CreateIndividualNotificationRequest): Notification

    @POST("/api/v1/notification/all")
    suspend fun addAllRunnerNotification(@Body request: CreateAllNotificationRequest): List<Notification>

    @POST("/api/v1/notification/group")
    suspend fun addGroupNotification(@Body request: CreateGroupNotificationRequest): List<Notification>

    @PUT("/api/v1/notification/readed")
    suspend fun readNotify(@Body notification: Notification): Notification

    @POST("/api/v1/notification/fcm-token")
    suspend fun updateFCMToken(@Body request: UpdateFCMTokenRequest): Any

    @PUT("/api/v1/notification/mark-all-read")
    suspend fun markAllAsRead(): List<Notification>

    @GET("/api/v1/notification/unread-count")
    suspend fun getUnreadCount(): Int
}