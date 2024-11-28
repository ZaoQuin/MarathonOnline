package com.university.marathononline.data.repository

import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.contest.ContestApiService

class ContestRepository(
    private val api: ContestApiService
): BaseRepository(){

    suspend fun getContest() = safeApiCall {
        api.getContests()
    }
}