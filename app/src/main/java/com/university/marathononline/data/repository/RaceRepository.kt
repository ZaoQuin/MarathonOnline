package com.university.marathononline.data.repository

import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.race.RaceApiService

class RaceRepository(
    private val api: RaceApiService
): BaseRepository() {
}