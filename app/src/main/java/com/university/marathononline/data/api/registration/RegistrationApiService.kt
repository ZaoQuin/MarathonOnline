package com.university.marathononline.data.api.registration

import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.Race
import com.university.marathononline.data.models.Registration
import com.university.marathononline.data.response.RegistrationsResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface RegistrationApiService {
    @POST("/api/v1/registration/race")
    suspend fun saveRaceIntoRegistration(@Body race: Race): RegistrationsResponse

    @POST("/api/v1/registration")
    suspend fun registerForContest(@Body contest: Contest): Registration
}