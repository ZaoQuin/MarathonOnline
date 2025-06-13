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

    suspend fun getByRunner(startDate: String?, endDate: String?) = safeApiCall {
        api.getByRunner(startDate, endDate)
    }

    suspend fun sync(records: List<CreateRecordRequest>) = safeApiCall {
        api.sync(records)
    }

    suspend fun getById(id: Long) = safeApiCall {
        api.getById(id)
    }
}