package com.university.marathononline.base

import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.auth.AuthApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

abstract class BaseRepository {
    suspend fun <T> safeApiCall (
        apiCall: suspend () -> T
    ): Resource<T>{
        return withContext(Dispatchers.IO) {
            try {
                Resource.Success(apiCall.invoke())
            } catch (throwable: Throwable) {
                when(throwable){
                    is HttpException -> {
                        Resource.Failure(true, throwable.code(), throwable.response()?.errorBody())
                    }
                    else -> {
                        Resource.Failure(true, null, null)
                    }
                }
            }

        }
    }

    suspend fun logout(api: AuthApiService) = safeApiCall {
        api.logout()
    }
}
