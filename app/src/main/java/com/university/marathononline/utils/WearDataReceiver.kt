package com.university.marathononline.utils

import com.university.marathononline.data.models.WearHealthData
import android.util.Log
import com.google.android.gms.wearable.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.tasks.await

class WearDataReceiver : WearableListenerService() {

    companion object {
        private const val TAG = "WearDataReceiver"

        private val _wearHealthDataFlow = MutableSharedFlow<WearHealthData>(
            replay = 1,
            extraBufferCapacity = 10
        )
        val wearHealthDataFlow: SharedFlow<WearHealthData> = _wearHealthDataFlow

        private val _recordingStateFlow = MutableSharedFlow<Boolean>(
            replay = 1,
            extraBufferCapacity = 5
        )
        val recordingStateFlow: SharedFlow<Boolean> = _recordingStateFlow

        private val _connectionStateFlow = MutableSharedFlow<Boolean>(
            replay = 1,
            extraBufferCapacity = 5
        )
        val connectionStateFlow: SharedFlow<Boolean> = _connectionStateFlow

        private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        private val connectedNodes = mutableSetOf<String>()

        // Add method to get current connection state
        fun getCurrentConnectionState(): Boolean {
            return connectedNodes.isNotEmpty()
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "=== WearDataReceiver service CREATED ===")
        Log.d(TAG, "Service instance: ${this.hashCode()}")
        checkExistingConnections()
    }

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "=== WearDataReceiver onStartCommand ===")
        Log.d(TAG, "Intent: $intent, flags: $flags, startId: $startId")

        // Force connection check when service is explicitly started
        checkExistingConnections()

        return START_STICKY
    }

    private fun checkExistingConnections() {
        serviceScope.launch {
            try {
                val nodeClient = Wearable.getNodeClient(this@WearDataReceiver)
                val nodes = nodeClient.connectedNodes.await()

                Log.d(TAG, "=== Checking existing connections ===")
                Log.d(TAG, "Found ${nodes.size} connected nodes:")

                var hasWearConnection = false
                connectedNodes.clear()

                for (node in nodes) {
                    Log.d(TAG, "  - Node: ${node.displayName} (${node.id})")
                    connectedNodes.add(node.id)
                    hasWearConnection = true
                }

                Log.d(TAG, if (hasWearConnection) "✓ Wear device(s) connected" else "✗ No wear devices connected")
                _connectionStateFlow.emit(hasWearConnection)

            } catch (e: Exception) {
                Log.e(TAG, "Error checking existing connections", e)
                _connectionStateFlow.emit(false)
            }
        }
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)
        Log.d(TAG, "=== onDataChanged called with ${dataEvents.count} events ===")

        // Emit connection state when receiving data
        serviceScope.launch {
            val currentState = _connectionStateFlow.replayCache.lastOrNull() ?: false
            if (!currentState) {
                Log.d(TAG, "✓ Connection detected via data reception")
                _connectionStateFlow.emit(true)
            }
        }

        try {
            dataEvents.forEach { dataEvent ->
                val path = dataEvent.dataItem.uri.path
                val host = dataEvent.dataItem.uri.host

                Log.d(TAG, "Processing data event:")
                Log.d(TAG, "  - Type: ${dataEvent.type}")
                Log.d(TAG, "  - Path: $path")
                Log.d(TAG, "  - Host: $host")
                Log.d(TAG, "  - Full URI: ${dataEvent.dataItem.uri}")

                when (dataEvent.type) {
                    DataEvent.TYPE_CHANGED -> {
                        Log.d(TAG, "Data CHANGED for path: $path")

                        // More flexible path matching
                        if (path?.contains("wear_health_data") == true || path == WearableConstants.DATA_PATH) {
                            Log.d(TAG, "✓ Health data path detected! Processing...")
                            handleWearHealthData(dataEvent.dataItem)
                        } else {
                            Log.w(TAG, "✗ Path mismatch! Expected: ${WearableConstants.DATA_PATH}, Got: $path")
                            // Try to handle anyway if it looks like health data
                            if (path?.contains("health") == true || path?.contains("wear") == true) {
                                Log.d(TAG, "Attempting to process potential health data...")
                                handleWearHealthData(dataEvent.dataItem)
                            }
                        }
                    }
                    DataEvent.TYPE_DELETED -> {
                        Log.d(TAG, "Data DELETED for path: $path")
                    }
                    else -> {
                        Log.d(TAG, "Unknown data event type: ${dataEvent.type}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing data events", e)
        } finally {
            try {
                dataEvents.release()
                Log.d(TAG, "DataEventBuffer released")
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing DataEventBuffer", e)
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        Log.d(TAG, "=== onMessageReceived ===")
        Log.d(TAG, "Message path: ${messageEvent.path}")
        Log.d(TAG, "Message data: ${String(messageEvent.data)}")
        Log.d(TAG, "Source node: ${messageEvent.sourceNodeId}")

        // Emit connection state when receiving messages
        serviceScope.launch {
            val currentState = _connectionStateFlow.replayCache.lastOrNull() ?: false
            if (!currentState) {
                Log.d(TAG, "✓ Connection detected via message reception")
                _connectionStateFlow.emit(true)
            }
        }

        when (messageEvent.path) {
            WearableConstants.START_RECORDING_PATH -> {
                Log.d(TAG, "✓ Received START recording message from wear")
                serviceScope.launch {
                    try {
                        _recordingStateFlow.emit(true)
                        Log.d(TAG, "✓ Recording state emitted: true")
                    } catch (e: Exception) {
                        Log.e(TAG, "✗ Error emitting start recording state", e)
                    }
                }
            }
            WearableConstants.STOP_RECORDING_PATH -> {
                Log.d(TAG, "✓ Received STOP recording message from wear")
                serviceScope.launch {
                    try {
                        _recordingStateFlow.emit(false)
                        Log.d(TAG, "✓ Recording state emitted: false")
                    } catch (e: Exception) {
                        Log.e(TAG, "✗ Error emitting stop recording state", e)
                    }
                }
            }
            else -> {
                Log.w(TAG, "✗ Unknown message path: ${messageEvent.path}")
            }
        }
    }

    private fun handleWearHealthData(dataItem: DataItem) {
        Log.d(TAG, "=== handleWearHealthData ===")
        try {

            Log.d(TAG, "Parsing wear health data from: ${dataItem.uri}")
            val dataMap = DataMapItem.fromDataItem(dataItem).dataMap

            // Log all available keys for debugging
            Log.d(TAG, "Available keys in dataMap: ${dataMap.keySet()}")

            // Try different key patterns in case constants don't match
            val availableKeys = dataMap.keySet()
            Log.d(TAG, "All keys: $availableKeys")

            // Extract data with fallback key names
            val heartRate = getDoubleValue(dataMap, listOf(
                WearableConstants.KEY_HEART_RATE,
                "heart_rate",
                "heartRate",
                "hr"
            ))

            val steps = getIntValue(dataMap, listOf(
                WearableConstants.KEY_STEPS,
                "steps",
                "step_count"
            ))

            val distance = getDoubleValue(dataMap, listOf(
                WearableConstants.KEY_DISTANCE,
                "distance",
                "dist"
            ))

            val speed = getDoubleValue(dataMap, listOf(
                WearableConstants.KEY_SPEED,
                "speed",
                "velocity"
            ))

            val calories = getDoubleValue(dataMap, listOf(
                WearableConstants.KEY_CALORIES,
                "calories",
                "cal"
            ))

            val timestamp = getLongValue(dataMap, listOf(
                WearableConstants.KEY_TIMESTAMP,
                "timestamp",
                "time"
            ), System.currentTimeMillis())

            val isRecording = getBooleanValue(dataMap, listOf(
                WearableConstants.KEY_IS_RECORDING,
                "is_recording",
                "recording"
            ))

            Log.d(TAG, "Extracted data:")
            Log.d(TAG, "  - Heart Rate: $heartRate")
            Log.d(TAG, "  - Steps: $steps")
            Log.d(TAG, "  - Distance: $distance")
            Log.d(TAG, "  - Speed: $speed")
            Log.d(TAG, "  - Calories: $calories")
            Log.d(TAG, "  - Timestamp: $timestamp")
            Log.d(TAG, "  - Is Recording: $isRecording")

            val wearHealthData = WearHealthData(
                heartRate = heartRate,
                steps = steps,
                distance = distance,
                speed = speed,
                calories = calories,
                timestamp = timestamp,
                isRecording = isRecording
            )

            Log.d(TAG, "✓ Created WearHealthData object: $wearHealthData")

            serviceScope.launch {
                try {
                    Log.d(TAG, "Emitting health data to flow...")
                    if (wearHealthData.isRecording) {
                        _wearHealthDataFlow.emit(wearHealthData)
                    } else {
                        Log.d(TAG, "Ignoring wear data: not recording")
                    }
                    Log.d(TAG, "✓ Health data emitted successfully!")
                    Log.d(TAG, "Flow subscriber count: ${_wearHealthDataFlow.subscriptionCount.value}")
                } catch (e: Exception) {
                    Log.e(TAG, "✗ Error emitting wear health data", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error parsing wear health data", e)
            e.printStackTrace()
        }
    }

    // Helper methods to extract data with fallback keys
    private fun getDoubleValue(dataMap: DataMap, keys: List<String>, default: Double = 0.0): Double {
        for (key in keys) {
            if (dataMap.containsKey(key)) {
                return try {
                    dataMap.getDouble(key, default)
                } catch (e: Exception) {
                    // Try as float if double fails
                    try {
                        dataMap.getFloat(key, default.toFloat()).toDouble()
                    } catch (e2: Exception) {
                        Log.w(TAG, "Failed to get double value for key: $key", e2)
                        default
                    }
                }
            }
        }
        return default
    }

    private fun getIntValue(dataMap: DataMap, keys: List<String>, default: Int = 0): Int {
        for (key in keys) {
            if (dataMap.containsKey(key)) {
                return try {
                    dataMap.getInt(key, default)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to get int value for key: $key", e)
                    default
                }
            }
        }
        return default
    }

    private fun getLongValue(dataMap: DataMap, keys: List<String>, default: Long = 0L): Long {
        for (key in keys) {
            if (dataMap.containsKey(key)) {
                return try {
                    dataMap.getLong(key, default)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to get long value for key: $key", e)
                    default
                }
            }
        }
        return default
    }

    private fun getBooleanValue(dataMap: DataMap, keys: List<String>, default: Boolean = false): Boolean {
        for (key in keys) {
            if (dataMap.containsKey(key)) {
                return try {
                    dataMap.getBoolean(key, default)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to get boolean value for key: $key", e)
                    default
                }
            }
        }
        return default
    }

    override fun onPeerConnected(peer: Node) {
        super.onPeerConnected(peer)
        Log.d(TAG, "=== Peer CONNECTED: ${peer.displayName} (${peer.id}) ===")

        connectedNodes.add(peer.id)
        serviceScope.launch {
            Log.d(TAG, "✓ Emitting connection state: true")
            _connectionStateFlow.emit(true)
        }
    }

    override fun onPeerDisconnected(peer: Node) {
        super.onPeerDisconnected(peer)
        Log.d(TAG, "=== Peer DISCONNECTED: ${peer.displayName} (${peer.id}) ===")

        connectedNodes.remove(peer.id)
        serviceScope.launch {
            val hasConnection = connectedNodes.isNotEmpty()
            Log.d(TAG, "✓ Emitting connection state: $hasConnection")
            _connectionStateFlow.emit(hasConnection)
        }
    }

    fun checkConnectionStatus() {
        serviceScope.launch {
            try {
                val nodeClient = Wearable.getNodeClient(this@WearDataReceiver)
                val nodes = nodeClient.connectedNodes.await()
                val hasConnection = nodes.isNotEmpty()

                Log.d(TAG, "Manual connection check: $hasConnection (${nodes.size} nodes)")
                _connectionStateFlow.emit(hasConnection)

                connectedNodes.clear()
                nodes.forEach { connectedNodes.add(it.id) }

            } catch (e: Exception) {
                Log.e(TAG, "Error in manual connection check", e)
                _connectionStateFlow.emit(false)
            }
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "=== WearDataReceiver service DESTROYED ===")
        super.onDestroy()
        serviceScope.cancel()
        connectedNodes.clear()
    }
}