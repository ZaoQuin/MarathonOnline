package com.university.marathononline.data.repository

import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.registration.RegistrationApiService
import com.university.marathononline.data.models.Race

class RegistrationRepository(
    private val api: RegistrationApiService
): BaseRepository() {

    suspend fun saveRaceIntoRegistration(race: Race) = safeApiCall {
        api.saveRaceIntoRegistration(race)
    }
}