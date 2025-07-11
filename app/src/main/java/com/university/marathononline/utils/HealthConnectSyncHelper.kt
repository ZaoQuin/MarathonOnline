package com.university.marathononline.utils

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.university.marathononline.data.models.ERecordSource
import com.university.marathononline.data.api.record.CreateRecordRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object HealthConnectSyncHelper {

    private const val TAG = "HealthConnectSync"

    // Định nghĩa các permissions cần thiết
    private val REQUIRED_PERMISSIONS = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class)
    )

    /**
     * Kiểm tra xem Health Connect có khả dụng không
     */
    fun isHealthConnectAvailable(context: Context): Boolean {
        val availabilityStatus = HealthConnectClient.getSdkStatus(context)
        Log.d(TAG, "Health Connect availability status: $availabilityStatus")
        return availabilityStatus == HealthConnectClient.SDK_AVAILABLE
    }

    /**
     * Kiểm tra xem đã có đủ permissions chưa
     */
    suspend fun hasRequiredPermissions(context: Context): Boolean {
        return try {
            val healthConnectClient = HealthConnectClient.getOrCreate(context)
            val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
            REQUIRED_PERMISSIONS.all { it in grantedPermissions }
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi kiểm tra permissions", e)
            false
        }
    }

    /**
     * Đồng bộ dữ liệu từ Health Connect và chuyển đổi thành CreateRecordRequest
     */
    suspend fun syncData(context: Context, startTime: LocalDateTime, onComplete: (Boolean, List<CreateRecordRequest>?) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                // Kiểm tra trạng thái đồng bộ
                val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val isSyncEnabled = prefs.getBoolean("sync_health_connect_enabled", false)
                if (!isSyncEnabled) {
                    Log.d(TAG, "Đồng bộ bị tắt trong cài đặt")
                    withContext(Dispatchers.Main) { onComplete(false, null) }
                    return@withContext
                }

                if (!isHealthConnectAvailable(context)) {
                    Log.d(TAG, "Health Connect không khả dụng")
                    withContext(Dispatchers.Main) { onComplete(false, null) }
                    return@withContext
                }

                val healthConnectClient = HealthConnectClient.getOrCreate(context)

                if (!hasRequiredPermissions(context)) {
                    Log.d(TAG, "Không có permissions Health Connect - bỏ qua đồng bộ")
                    withContext(Dispatchers.Main) { onComplete(false, null) }
                    return@withContext
                }

                Log.d(TAG, "Bắt đầu đồng bộ dữ liệu từ Health Connect")
                val endInstant = Instant.now()
                val startInstant = startTime.atZone(ZoneId.systemDefault()).toInstant()
                val timeRangeFilter = TimeRangeFilter.between(startInstant, endInstant)

                val recordRequests = processHealthData(healthConnectClient, timeRangeFilter, startInstant, endInstant)

                Log.d(TAG, "Hoàn thành đồng bộ dữ liệu Health Connect")
                Log.d(TAG, "Số record: ${recordRequests.size}")

                withContext(Dispatchers.Main) { onComplete(true, recordRequests) }
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi đồng bộ dữ liệu Health Connect", e)
                withContext(Dispatchers.Main) { onComplete(false, null) }
            }
        }
    }

    /**
     * Format Instant thành string theo format API yêu cầu
     */
    private fun formatInstantForAPI(instant: Instant): String? {
        return try {
            // Check if Instant is within valid LocalDateTime range
            if (instant.isBefore(Instant.parse("0000-01-01T00:00:00Z")) ||
                instant.isAfter(Instant.parse("9999-12-31T23:59:59Z"))) {
                Log.w(TAG, "Instant out of valid range: $instant")
                return null
            }
            val localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
            // Sử dụng format ISO phù hợp với API
            localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting Instant: $instant", e)
            null
        }
    }

    /**
     * Xử lý và tổng hợp dữ liệu từ Health Connect
     */
    private suspend fun processHealthData(
        healthConnectClient: HealthConnectClient,
        timeRangeFilter: TimeRangeFilter,
        startTime: Instant,
        endTime: Instant
    ): List<CreateRecordRequest> {
        val recordRequests = mutableListOf<CreateRecordRequest>()

        try {
            // Fetch Steps data
            val stepsRequest = ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = timeRangeFilter
            )
            val stepsResponse = healthConnectClient.readRecords(stepsRequest)
            Log.d(TAG, "Number of Steps records: ${stepsResponse.records.size}")

            // Fetch Distance data
            val distanceRequest = ReadRecordsRequest(
                recordType = DistanceRecord::class,
                timeRangeFilter = timeRangeFilter
            )
            val distanceResponse = healthConnectClient.readRecords(distanceRequest)
            Log.d(TAG, "Number of Distance records: ${distanceResponse.records.size}")

            // Fetch Heart Rate data
            val heartRateRequest = ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = timeRangeFilter
            )
            val heartRateResponse = healthConnectClient.readRecords(heartRateRequest)
            Log.d(TAG, "Number of Heart Rate records: ${heartRateResponse.records.size}")

            // Group data by time range
            val timeRangeRecords = mutableMapOf<String, CreateRecordRequest>()

            // Process Steps
            stepsResponse.records.forEach { stepRecord ->
                val startTimeStr = formatInstantForAPI(stepRecord.startTime)
                val endTimeStr = formatInstantForAPI(stepRecord.endTime)

                if (startTimeStr != null && endTimeStr != null) {
                    val key = "${stepRecord.startTime}-${stepRecord.endTime}"
                    val record = timeRangeRecords.getOrPut(key) {
                        CreateRecordRequest(
                            steps = 0,
                            distance = 0.0,
                            avgSpeed = 0.0,
                            heartRate = 0.0,
                            startTime = startTimeStr,
                            endTime = endTimeStr,
                            source = ERecordSource.THIRD
                        )
                    }
                    record.steps = stepRecord.count.toInt()
                } else {
                    Log.w(TAG, "Skipping step record with invalid time: ${stepRecord.startTime} - ${stepRecord.endTime}")
                }
            }

            // Process Distance
            distanceResponse.records.forEach { distanceRecord ->
                val startTimeStr = formatInstantForAPI(distanceRecord.startTime)
                val endTimeStr = formatInstantForAPI(distanceRecord.endTime)

                if (startTimeStr != null && endTimeStr != null) {
                    val key = "${distanceRecord.startTime}-${distanceRecord.endTime}"
                    val record = timeRangeRecords.getOrPut(key) {
                        CreateRecordRequest(
                            steps = 0,
                            distance = 0.0,
                            avgSpeed = 0.0,
                            heartRate = 0.0,
                            startTime = startTimeStr,
                            endTime = endTimeStr,
                            source = ERecordSource.THIRD
                        )
                    }
                    record.distance = distanceRecord.distance.inKilometers
                    val durationInSeconds = ChronoUnit.SECONDS.between(distanceRecord.startTime, distanceRecord.endTime)
                    record.avgSpeed = if (durationInSeconds > 0) {
                        (distanceRecord.distance.inMeters / durationInSeconds) * 3.6
                    } else {
                        0.0
                    }
                } else {
                    Log.w(TAG, "Skipping distance record with invalid time: ${distanceRecord.startTime} - ${distanceRecord.endTime}")
                }
            }

            // Process Heart Rate
            heartRateResponse.records.forEach { heartRateRecord ->
                val startTimeStr = formatInstantForAPI(heartRateRecord.startTime)
                val endTimeStr = formatInstantForAPI(heartRateRecord.endTime)

                if (startTimeStr != null && endTimeStr != null) {
                    val key = "${heartRateRecord.startTime}-${heartRateRecord.endTime}"
                    val record = timeRangeRecords.getOrPut(key) {
                        CreateRecordRequest(
                            steps = 0,
                            distance = 0.0,
                            avgSpeed = 0.0,
                            heartRate = 0.0,
                            startTime = startTimeStr,
                            endTime = endTimeStr,
                            source = ERecordSource.THIRD
                        )
                    }
                    val samples = heartRateRecord.samples
                    if (samples.isNotEmpty()) {
                        record.heartRate = samples.map { it.beatsPerMinute }.average()
                    }
                } else {
                    Log.w(TAG, "Skipping heart rate record with invalid time: ${heartRateRecord.startTime} - ${heartRateRecord.endTime}")
                }
            }

            recordRequests.addAll(timeRangeRecords.values)

            // Log các record để debug
            recordRequests.forEach { record ->
                Log.d(TAG, "CreateRecordRequest: startTime=${record.startTime}, endTime=${record.endTime}, steps=${record.steps}, distance=${record.distance}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error processing Health Connect data", e)
        }

        return recordRequests
    }

    /**
     * Lấy dữ liệu Health Connect cho ngày hôm nay
     */
    suspend fun getTodayHealthData(context: Context, onComplete: (Boolean, List<CreateRecordRequest>?) -> Unit) {
        val now = Instant.now()
        val startOfDay = now.atZone(ZoneId.systemDefault())
            .toLocalDate()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()

        withContext(Dispatchers.IO) {
            try {
                if (!isHealthConnectAvailable(context) || !hasRequiredPermissions(context)) {
                    Log.d(TAG, "Health Connect không khả dụng hoặc thiếu permissions")
                    withContext(Dispatchers.Main) { onComplete(false, null) }
                    return@withContext
                }

                val healthConnectClient = HealthConnectClient.getOrCreate(context)
                val timeRangeFilter = TimeRangeFilter.between(startOfDay, now)

                val recordRequests = processHealthData(healthConnectClient, timeRangeFilter, startOfDay, now)

                Log.d(TAG, "Lấy dữ liệu hôm nay thành công: ${recordRequests.size} records")
                withContext(Dispatchers.Main) { onComplete(true, recordRequests) }
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi lấy dữ liệu Health Connect hôm nay", e)
                withContext(Dispatchers.Main) { onComplete(false, null) }
            }
        }
    }

    /**
     * Method để đồng bộ dữ liệu theo yêu cầu (manual sync)
     */
    fun manualSync(context: Context, startTime: LocalDateTime, onComplete: (Boolean, List<CreateRecordRequest>?) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            syncData(context, startTime, onComplete)
        }
    }

    /**
     * Lấy thống kê tổng quan từ Health Connect
     */
    suspend fun getHealthSummary(context: Context): HealthSummary? {
        return try {
            if (!isHealthConnectAvailable(context) || !hasRequiredPermissions(context)) {
                Log.d(TAG, "Không thể lấy health summary - Health Connect không khả dụng hoặc thiếu permissions")
                return null
            }

            val healthConnectClient = HealthConnectClient.getOrCreate(context)
            val endTime = Instant.now()
            val startTime = endTime.minus(1, ChronoUnit.DAYS) // Dữ liệu 24h gần nhất
            val timeRangeFilter = TimeRangeFilter.between(startTime, endTime)

            var totalSteps = 0L
            var totalDistance = 0.0
            var avgHeartRate = 0.0

            // Lấy tổng steps
            val stepsRequest = ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = timeRangeFilter
            )
            val stepsResponse = healthConnectClient.readRecords(stepsRequest)
            totalSteps = stepsResponse.records.sumOf { it.count }

            // Lấy tổng distance
            val distanceRequest = ReadRecordsRequest(
                recordType = DistanceRecord::class,
                timeRangeFilter = timeRangeFilter
            )
            try {
                val distanceResponse = healthConnectClient.readRecords(distanceRequest)
                totalDistance = distanceResponse.records.sumOf { it.distance.inMeters }
                Log.d(TAG, "Tổng distance: $totalDistance meters, Số bản ghi: ${distanceResponse.records.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi đọc DistanceRecord: ${e.message}", e)
            }

            // Lấy trung bình heart rate
            val heartRateRequest = ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = timeRangeFilter
            )
            val heartRateResponse = healthConnectClient.readRecords(heartRateRequest)
            val allHeartRates = heartRateResponse.records.flatMap { it.samples }
            if (allHeartRates.isNotEmpty()) {
                avgHeartRate = allHeartRates.map { it.beatsPerMinute }.average()
            }

            Log.d(TAG, "Số mẫu heart rate: ${allHeartRates.size}")
            HealthSummary(totalSteps, totalDistance, avgHeartRate)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi lấy health summary", e)
            null
        }
    }

    /**
     * Data class cho health summary
     */
    data class HealthSummary(
        val totalSteps: Long,
        val totalDistanceMeters: Double,
        val avgHeartRate: Double
    )
}