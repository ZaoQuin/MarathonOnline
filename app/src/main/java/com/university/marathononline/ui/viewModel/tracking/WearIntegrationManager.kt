package com.university.marathononline.ui.viewModel.tracking

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.android.gms.wearable.Wearable
import com.university.marathononline.data.models.WearHealthData
import com.university.marathononline.utils.WearDataReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class WearIntegrationManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {

    private val _wearHealthData = MutableStateFlow<WearHealthData?>(null)
    val wearHealthData: StateFlow<WearHealthData?> = _wearHealthData

    private val _isWearConnected = MutableStateFlow(false)
    val isWearConnected: StateFlow<Boolean> = _isWearConnected

    var onStartRecording: (() -> Unit)? = null
    var onStopRecording: (() -> Unit)? = null
    var onHealthDataUpdate: ((WearHealthData) -> Unit)? = null

    fun initialize() {
        Log.d("WearIntegration", "=== Initializing wear integration ===")

        // Reset connection state
        _isWearConnected.value = false

        // Start the service
        startWearDataService()

        // Wait a moment for service to initialize, then start collecting flows
        coroutineScope.launch {
            delay(1000)
            initializeFlowCollectors()
        }
    }

    private fun startWearDataService() {
        try {
            val serviceIntent = Intent(context, WearDataReceiver::class.java)

            // Use startForegroundService for Android 8+ to ensure service stays alive
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }

            Log.d("WearIntegration", "WearDataReceiver service started")
        } catch (e: Exception) {
            Log.e("WearIntegration", "Failed to start WearDataReceiver service", e)
        }
    }

    private suspend fun initializeFlowCollectors() {
        // Connection state flow
        coroutineScope.launch {
            try {
                Log.d("WearIntegration", "Starting to collect connectionStateFlow...")
                WearDataReceiver.connectionStateFlow.collect { isConnected ->
                    Log.d("WearIntegration", "✓ Connection state changed: $isConnected")
                    _isWearConnected.value = isConnected

                    if (isConnected) {
                        Log.d("WearIntegration", "✓ Wear device connected!")
                    } else {
                        Log.d("WearIntegration", "✗ Wear device disconnected!")
                        _wearHealthData.value = null
                    }
                }
            } catch (e: Exception) {
                Log.e("WearIntegration", "✗ Error collecting connection state", e)
            }
        }

        // Health data flow
        coroutineScope.launch {
            try {
                Log.d("WearIntegration", "Starting to collect wearHealthDataFlow...")
                WearDataReceiver.wearHealthDataFlow.collect { wearData ->
                    Log.d("WearIntegration", "✓ Received wear data: $wearData")
                    _wearHealthData.value = wearData
                    onHealthDataUpdate?.invoke(wearData)
                }
            } catch (e: Exception) {
                Log.e("WearIntegration", "✗ Error collecting wear health data", e)
            }
        }

        // Recording state flow
        coroutineScope.launch {
            try {
                Log.d("WearIntegration", "Starting to collect recordingStateFlow...")
                WearDataReceiver.recordingStateFlow.collect { isRecording ->
                    Log.d("WearIntegration", "✓ Recording state from Wear: $isRecording")

                    if (isRecording) {
                        Log.d("WearIntegration", "Starting recording from Wear")
                        onStartRecording?.invoke()
                    } else {
                        Log.d("WearIntegration", "Stopping recording from Wear")
                        onStopRecording?.invoke()
                    }
                }
            } catch (e: Exception) {
                Log.e("WearIntegration", "✗ Error collecting recording state", e)
            }
        }

        // Periodic connection health check
        coroutineScope.launch {
            while (true) {
                delay(15000) // Check every 15 seconds
                checkConnectionHealth()
            }
        }

        // Initial connection check after flows are set up
        delay(2000)
        manualCheckConnection()
    }

    private fun checkConnectionHealth() {
        try {
            // Restart service if needed
            val serviceIntent = Intent(context, WearDataReceiver::class.java)
            context.startService(serviceIntent)

            // Trigger manual connection check
            manualCheckConnection()

            Log.d("WearIntegration", "Connection health check completed")
        } catch (e: Exception) {
            Log.e("WearIntegration", "Error in connection health check", e)
        }
    }

    private fun manualCheckConnection() {
        coroutineScope.launch {
            try {
                val nodeClient = Wearable.getNodeClient(context)
                val nodes = nodeClient.connectedNodes.await()
                val hasConnection = nodes.isNotEmpty()

                Log.d("WearIntegration", "Manual check found ${nodes.size} connected nodes")
                nodes.forEach { node ->
                    Log.d("WearIntegration", "  - ${node.displayName} (${node.id})")
                }

                // Update connection state if it has changed
                if (hasConnection != _isWearConnected.value) {
                    Log.d("WearIntegration", "Updating connection state: $hasConnection")
                    _isWearConnected.value = hasConnection
                }
            } catch (e: Exception) {
                Log.e("WearIntegration", "Error in manual connection check", e)
                _isWearConnected.value = false
            }
        }
    }

    fun refreshConnection() {
        Log.d("WearIntegration", "Force refreshing wear connection...")
        manualCheckConnection()
    }
}