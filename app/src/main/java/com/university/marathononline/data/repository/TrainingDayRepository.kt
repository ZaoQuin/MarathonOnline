package com.university.marathononline.data.repository

import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.training.TrainingDayApiService
import com.university.marathononline.data.models.Record

class TrainingDayRepository(
    private val api: TrainingDayApiService
) : BaseRepository() {
    suspend fun getCurrentTrainingDay() = safeApiCall {
        api.getCurrentTrainingDay()
    }

    suspend fun saveRecordIntoTrainingDay(record: Record) = safeApiCall {
        api.saveRecordIntoTrainingDay(record)
    }

    suspend fun resetTrainingDay() = safeApiCall {
        api.resetTrainingDay()
    }
}