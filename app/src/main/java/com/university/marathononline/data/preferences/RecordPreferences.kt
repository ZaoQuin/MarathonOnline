package com.university.marathononline.data.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.preferencesKey
import androidx.datastore.preferences.remove
import com.university.marathononline.data.models.ERecordSource
import com.university.marathononline.data.api.record.CreateRecordRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

class RecordPreferences(context: Context) {

    private val applicationContext = context.applicationContext
    private val dataStore: DataStore<Preferences> = applicationContext.createDataStore(
        name = "record_preferences"
    )

    suspend fun saveRecord(record: CreateRecordRequest) {
        dataStore.edit { preferences ->
            preferences[KEY_STEPS] = record.steps
            preferences[KEY_DISTANCE] = record.distance.toFloat()
            preferences[KEY_AVG_SPEED] = record.avgSpeed.toFloat()
            preferences[KEY_HEART_RATE] = record.heartRate.toFloat()
            preferences[KEY_START_TIME] = record.startTime
            preferences[KEY_END_TIME] = record.endTime
            preferences[KEY_SOURCE] = record.source.name
            Log.d("RecordPreferences", "Saved record: steps=${record.steps}, distance=${record.distance}")
        }
    }

    // Xóa dữ liệu record khỏi DataStore
    suspend fun clearRecord() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_STEPS)
            preferences.remove(KEY_DISTANCE)
            preferences.remove(KEY_AVG_SPEED)
            preferences.remove(KEY_HEART_RATE)
            preferences.remove(KEY_START_TIME)
            preferences.remove(KEY_END_TIME)
            preferences.remove(KEY_SOURCE)
            Log.d("RecordPreferences", "Cleared record data")
        }
    }

    // Lấy dữ liệu record từ DataStore dưới dạng Flow<CreateRecordRequest?>
    val record: Flow<CreateRecordRequest?>
        get() = dataStore.data.map { preferences ->
            try {
                if (preferences.contains(KEY_STEPS)) {
                    CreateRecordRequest(
                        steps = preferences[KEY_STEPS] ?: 0,
                        distance = preferences[KEY_DISTANCE]?.toDouble() ?: 0.0,
                        avgSpeed = preferences[KEY_AVG_SPEED]?.toDouble() ?: 0.0,
                        heartRate = preferences[KEY_HEART_RATE]?.toDouble() ?: 0.0,
                        startTime = preferences[KEY_START_TIME] ?: LocalDateTime.now().toString(),
                        endTime = preferences[KEY_END_TIME] ?: LocalDateTime.now().toString(),
                        source = preferences[KEY_SOURCE]?.let { ERecordSource.valueOf(it) }
                            ?: ERecordSource.DEVICE
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("RecordPreferences", "Error retrieving record", e)
                clearRecord()
                null
            }
        }

    companion object {
        private val KEY_STEPS = preferencesKey<Int>("record_steps")
        private val KEY_DISTANCE = preferencesKey<Float>("record_distance")
        private val KEY_AVG_SPEED = preferencesKey<Float>("record_avg_speed")
        private val KEY_HEART_RATE = preferencesKey<Float>("record_heart_rate")
        private val KEY_START_TIME = preferencesKey<String>("record_start_time")
        private val KEY_END_TIME = preferencesKey<String>("record_end_time")
        private val KEY_SOURCE = preferencesKey<String>("record_source")
    }
}