package com.university.marathononline.ui.view.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

// Health Connect imports
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.HeartRateRecord
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.databinding.ActivitySettingBinding
import com.university.marathononline.ui.viewModel.SettingViewModel
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.HealthConnectSyncHelper
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

class SettingActivity: BaseActivity<SettingViewModel, ActivitySettingBinding>()  {

    private val PREFS_NAME = "app_prefs"
    private val KEY_SYNC_ENABLED = "sync_health_connect_enabled"

    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(this) }


    private val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
    )

    private val requestPermissionLauncher = registerForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        if (granted.containsAll(permissions)) {
            saveSyncPreference(true)
            Toast.makeText(this, "Đã cấp quyền thành công", Toast.LENGTH_SHORT).show()
        } else {
            saveSyncPreference(false)
            Toast.makeText(this, "Vui lòng cấp đầy đủ quyền trong Health Connect", Toast.LENGTH_LONG).show()
            openHealthConnectApp()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setUp()
    }

    private fun setUp(){
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isSyncEnabled = prefs.getBoolean(KEY_SYNC_ENABLED, false)
        binding.apply{
            switchSyncHealthConnect.apply {
                isChecked = isSyncEnabled
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        // Yêu cầu cấp quyền nếu chưa có
                        lifecycleScope.launch {
                            checkAndRequestPermissions()
                        }
                    } else {
                        saveSyncPreference(false)
                        Toast.makeText(this@SettingActivity, "Đã tắt đồng bộ", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            lifecycleScope.launch {
                val time = userPreferences.lastSyncTime.first().toString()
                binding.lastSyncTime.text = DateUtils.convertToVietnameseDateTime(time)
            }
        }
    }

    override fun getViewModel() = SettingViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) = ActivitySettingBinding.inflate(inflater)

    override fun getActivityRepositories(): List<BaseRepository> {
        return listOf()
    }

    override fun onResume() {
        super.onResume()
        if (binding.switchSyncHealthConnect.isChecked) {
            lifecycleScope.launch {
                checkPermissionsStatus()
                val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
                Log.d("SettingActivity", "Quyền đã cấp: $grantedPermissions")
                if (hasRequiredPermissions()) {
                    val startTime = userPreferences.lastSyncTime.first()?: LocalDateTime.now().minusDays(1)
                    userPreferences.updateLastSyncTime()
                    HealthConnectSyncHelper.manualSync(this@SettingActivity, startTime) { success, recordRequest ->
                        if (success && recordRequest != null) {
                            Toast.makeText(this@SettingActivity, "Đồng bộ thử nghiệm thành công", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@SettingActivity, "Không thể đồng bộ dữ liệu", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private suspend fun hasRequiredPermissions(): Boolean {
        val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
        return permissions.all { it in grantedPermissions }
    }

    private suspend fun checkAndRequestPermissions() {
        try {
            // Check if Health Connect is available
            val availability = HealthConnectClient.getSdkStatus(this)
            if (availability != HealthConnectClient.SDK_AVAILABLE) {
                binding.switchSyncHealthConnect.isChecked = false
                Toast.makeText(this, "Health Connect không khả dụng", Toast.LENGTH_LONG).show()
                return
            }

            val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
            if (grantedPermissions.containsAll(permissions)) {
                // Already have all permissions
                saveSyncPreference(true)
                Toast.makeText(this, "Đã bật đồng bộ", Toast.LENGTH_SHORT).show()
            } else {
                // Need to request permissions - open Health Connect settings
                openHealthConnectSettings()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            binding.switchSyncHealthConnect.isChecked = false
            Toast.makeText(this, "Lỗi khi kiểm tra quyền: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun checkPermissionsStatus() {
        try {
            val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
            if (!grantedPermissions.containsAll(permissions)) {
                // Permissions were revoked
                binding.switchSyncHealthConnect.isChecked = false
                saveSyncPreference(false)
                Toast.makeText(this, "Quyền Health Connect đã bị thu hồi, đồng bộ đã tắt", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Lỗi khi kiểm tra quyền Health Connect", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openHealthConnectSettings() {
        try {
            requestPermissionLauncher.launch(permissions)
        } catch (e: Exception) {
            e.printStackTrace()
            openHealthConnectApp()
        }
    }

    private fun openHealthConnectApp() {
        try {
            val intent = packageManager.getLaunchIntentForPackage("com.google.android.apps.healthdata")
            if (intent != null) {
                startActivity(intent)
                Toast.makeText(this, "Vui lòng cấp quyền trong ứng dụng Health Connect", Toast.LENGTH_LONG).show()
            } else {
                // Health Connect not installed, redirect to Play Store
                val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.healthdata"))
                startActivity(playStoreIntent)
                Toast.makeText(this, "Vui lòng cài đặt Health Connect", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            binding.switchSyncHealthConnect.isChecked = false
            Toast.makeText(this, "Không thể mở Health Connect", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveSyncPreference(enabled: Boolean) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SYNC_ENABLED, enabled).apply()
    }
}