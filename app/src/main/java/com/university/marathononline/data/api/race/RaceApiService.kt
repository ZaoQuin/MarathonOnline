package com.university.marathononline.data.api.race

import com.university.marathononline.data.models.Race
import com.university.marathononline.data.request.CreateRaceRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface RaceApiService {
    @POST("/api/v1/race")
    suspend fun addRaceAndSaveIntoRegistration(@Body race: CreateRaceRequest): Race
}