package com.university.marathononline.utils

import android.util.Log
import com.university.marathononline.data.request.CreateRecordRequest
import java.time.format.DateTimeParseException

object RecordValidator {
    private const val TAG = "RecordValidator"

    // Minimum thresholds for valid data
    private const val MIN_STEPS = 0
    private const val MIN_DISTANCE = 0.0
    private const val MIN_AVG_SPEED = 0.0
    private const val MIN_HEART_RATE = 0.0
    private const val MAX_HEART_RATE = 220.0 // Maximum reasonable heart rate

    /**
     * Checks if a CreateRecordRequest is valid
     * @param recordRequest The record to validate
     * @return true if the record is valid, false otherwise
     */
    fun isValidRecord(recordRequest: CreateRecordRequest): Boolean {
        try {
            // Check mandatory non-zero values for steps, distance, and avgSpeed
            if (recordRequest.steps <= MIN_STEPS) {
                Log.w(TAG, "Invalid record: Steps (${recordRequest.steps}) must be greater than $MIN_STEPS")
                return false
            }

            if (recordRequest.distance <= MIN_DISTANCE) {
                Log.w(TAG, "Invalid record: Distance (${recordRequest.distance}) must be greater than $MIN_DISTANCE")
                return false
            }

            if (recordRequest.avgSpeed <= MIN_AVG_SPEED) {
                Log.w(TAG, "Invalid record: Average speed (${recordRequest.avgSpeed}) must be greater than $MIN_AVG_SPEED")
                return false
            }

            // Check heart rate range
            if (recordRequest.heartRate < MIN_HEART_RATE || recordRequest.heartRate > MAX_HEART_RATE) {
                Log.w(TAG, "Invalid record: Heart rate (${recordRequest.heartRate}) outside valid range ($MIN_HEART_RATE - $MAX_HEART_RATE)")
                return false
            }

            // Check time format and validity
            try {
                val startDateTime = DateUtils.parseDateTimeStringToLocalDateTime(recordRequest.startTime)
                val endDateTime = DateUtils.parseDateTimeStringToLocalDateTime(recordRequest.endTime)

                if (startDateTime == null || endDateTime == null) {
                    Log.w(TAG, "Invalid record: Failed to parse startTime (${recordRequest.startTime}) or endTime (${recordRequest.endTime})")
                    return false
                }

                // Ensure startTime is before endTime
                if (!startDateTime.isBefore(endDateTime)) {
                    Log.w(TAG, "Invalid record: startTime (${recordRequest.startTime}) is not before endTime (${recordRequest.endTime})")
                    return false
                }
            } catch (e: DateTimeParseException) {
                Log.w(TAG, "Invalid record: Incorrect time format - ${e.message}")
                return false
            }

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error validating record: ${e.message}", e)
            return false
        }
    }

    /**
     * Filters a list of records, keeping only valid ones
     * @param records The list of records to filter
     * @return A list of valid records
     */
    fun filterValidRecords(records: List<CreateRecordRequest>): List<CreateRecordRequest> {
        return records.filter { record ->
            isValidRecord(record).also { isValid ->
                if (!isValid) {
                    Log.d(TAG, "Skipping invalid record: steps=${record.steps}, distance=${record.distance}, " +
                            "avgSpeed=${record.avgSpeed}, heartRate=${record.heartRate}, " +
                            "startTime=${record.startTime}, endTime=${record.endTime}")
                }
            }
        }
    }
}