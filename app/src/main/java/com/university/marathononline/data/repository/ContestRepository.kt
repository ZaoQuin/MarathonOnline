package com.university.marathononline.data.repository

import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.contest.ContestApiService
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.request.CreateContestRequest

class ContestRepository(
    private val api: ContestApiService
): BaseRepository(){

    suspend fun getContests() = safeApiCall {
        api.getContests()
    }

    suspend fun getHomeContests() = safeApiCall {
        api.getHomeContests()
    }

    suspend fun getByRunner() = safeApiCall {
        api.getByRunner()
    }

    suspend fun getById(id: Long) = safeApiCall {
        api.getById(id)
    }

    suspend fun addContest(request: CreateContestRequest) = safeApiCall {
        api.addContest(request)
    }

    suspend fun updateContest(contest: Contest) = safeApiCall {
        api.updateContest(contest)
    }

    suspend fun getContestsByJwt() = safeApiCall {
        api.getContestsByJwt()
    }

    suspend fun deleteById(contestId: Long) = safeApiCall {
        api.deleteById(contestId)
    }

    suspend fun cancel(contest: Contest) = safeApiCall {
        api.cancel(contest)
    }
}