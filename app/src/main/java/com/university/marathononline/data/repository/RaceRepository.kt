package com.university.marathononline.data.repository

import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.race.RaceApiService
import com.university.marathononline.data.request.CreateRaceRequest

class RaceRepository(
    private val api: RaceApiService
): BaseRepository() {

    suspend fun addRaceAndSaveIntoRegistration(race: CreateRaceRequest) = safeApiCall {
        api.addRaceAndSaveIntoRegistration(race)
    }
}