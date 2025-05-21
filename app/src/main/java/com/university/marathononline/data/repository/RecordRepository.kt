package com.university.marathononline.data.repository

import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.record.RecordApiService
import com.university.marathononline.data.request.CreateRecordRequest

class RecordRepository(
    private val api: RecordApiService
): BaseRepository() {

    suspend fun addRecordAndSaveIntoRegistration(record: CreateRecordRequest) = safeApiCall {
        api.addRecordAndSaveIntoRegistration(record)
    }

    suspend fun getByRunner() = safeApiCall {
        api.getByRunner()
    }
}