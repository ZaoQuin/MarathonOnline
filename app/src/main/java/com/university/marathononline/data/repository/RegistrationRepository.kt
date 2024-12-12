package com.university.marathononline.data.repository

import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.registration.RegistrationApiService
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.Race
import com.university.marathononline.data.models.Registration

class RegistrationRepository(
    private val api: RegistrationApiService
): BaseRepository() {

    suspend fun saveRaceIntoRegistration(race: Race) = safeApiCall {
        api.saveRaceIntoRegistration(race)
    }

    suspend fun registerForContest(contest: Contest) = safeApiCall {
        api.registerForContest(contest)
    }

    suspend fun block(registration: Registration) = safeApiCall{
        api.block(registration)
    }

    suspend fun prizes(contest: Contest) = safeApiCall {
        api.prizes(contest)
    }
}