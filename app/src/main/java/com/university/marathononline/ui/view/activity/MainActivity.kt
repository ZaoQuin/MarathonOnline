package com.university.marathononline.ui.view.activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.university.marathononline.R
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.notify.NotificationApiService
import com.university.marathononline.data.api.record.RecordApiService
import com.university.marathononline.data.repository.NotificationRepository
import com.university.marathononline.data.repository.RecordRepository
import com.university.marathononline.data.request.CreateRecordRequest
import com.university.marathononline.databinding.ActivityMainBinding
import com.university.marathononline.ui.adapter.MainPagerAdapter
import com.university.marathononline.ui.viewModel.MainViewModel
import com.university.marathononline.utils.HealthConnectSyncHelper
import com.university.marathononline.utils.NotificationUtils
import com.university.marathononline.utils.RecordValidator
import com.university.marathononline.utils.startNewActivity
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime

class MainActivity: BaseActivity<MainViewModel, ActivityMainBinding>() {

    companion object {
        const val TAG = "Marathon Online"
        private const val REQUEST_NOTIFICATION_PERMISSION = 100
    }

    private lateinit var adapter: MainPagerAdapter
    private var handlerAnimation = Handler()
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                setupFirebase()
            } else {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATION_PERMISSION)
            }
        }

        FirebaseApp.initializeApp(this)
        NotificationUtils.createNotificationChannels(this)
        FirebaseMessaging.getInstance().subscribeToTopic("marathon_general")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Subscribed to general topic")
                } else {
                    Log.e(TAG, "Failed to subscribe to general topic", task.exception)
                }
            }

        adapter = MainPagerAdapter(this, emptyList())

        setUpViewPager()
        setUpBottomNavView()
        setUpRecordButton()
        setUpAnimation()

        observe()

        setupHealthConnectSync()
    }

    private fun setupHealthConnectSync() {
        Log.d(TAG, "Kiểm tra Health Connect...")
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isSyncEnabled = prefs.getBoolean("sync_health_connect_enabled", false)

        if (HealthConnectSyncHelper.isHealthConnectAvailable(this) && isSyncEnabled) {
            Log.d(TAG, "Health Connect khả dụng và đồng bộ được bật, thực hiện đồng bộ")
            performHealthConnectSync()
        } else {
            Log.d(TAG, "Health Connect không khả dụng hoặc đồng bộ bị tắt")
        }
    }

    private fun performHealthConnectSync() {
        lifecycleScope.launch {
            val startTime = userPreferences.lastSyncTime.first()?: LocalDateTime.now().minusDays(1)
            userPreferences.updateLastSyncTime()
            HealthConnectSyncHelper.syncData(this@MainActivity, startTime) { success, recordRequests ->
                if (success && recordRequests != null) {
                    val validRecords = RecordValidator.filterValidRecords(recordRequests)
                    Log.d(TAG, "Đồng bộ Health Connect thành công, số record hợp lệ: ${validRecords.size}")
                    validRecords.forEach { recordRequest ->
                        handleHealthData(recordRequest)
                    }
                    if (validRecords.isEmpty()) {
                        Log.w(TAG, "Không có record hợp lệ nào sau khi lọc")
                    } else {
                        viewModel.syncRecords(validRecords)
                    }
                } else {
                    Log.d(TAG, "Đồng bộ Health Connect thất bại hoặc không có quyền")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isSyncEnabled = prefs.getBoolean("sync_health_connect_enabled", false)

        if (HealthConnectSyncHelper.isHealthConnectAvailable(this) && isSyncEnabled) {
            Log.d(TAG, "App resumed - Thực hiện đồng bộ Health Connect")
            performHealthConnectSync()
        } else {
            Log.d(TAG, "App resumed - Bỏ qua đồng bộ Health Connect do không khả dụng hoặc bị tắt")
        }
    }

    private fun setupFirebase() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d(TAG, "FCM Registration Token: $token")
            sendTokenToServer(token)
        }

        FirebaseMessaging.getInstance().subscribeToTopic("marathon_updates")
            .addOnCompleteListener { task ->
                val msg = if (task.isSuccessful) {
                    "Subscribed to marathon_updates topic"
                } else {
                    "Subscribe to marathon_updates failed"
                }
                Log.d(TAG, msg)
            }
    }

    private fun sendTokenToServer(token: String) {
        viewModel.updateFCMToken(token, this@MainActivity)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult called for requestCode=$requestCode")

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission granted")
                setupFirebase()
            } else {
                Log.d(TAG, "Notification permission denied")
            }
        }
    }

    override fun getViewModel(): Class<MainViewModel> = MainViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }

    override fun getActivityRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val apiNotification = retrofitInstance.buildApi(NotificationApiService::class.java, token)
        val apiRecord = retrofitInstance.buildApi(RecordApiService::class.java, token)
        return listOf(NotificationRepository(apiNotification), RecordRepository(apiRecord))
    }

    private fun setUpRecordButton() {
        binding.btnRecord.setOnClickListener {
            startNewActivity(RecordActivity::class.java)
        }
    }

    private fun setUpAnimation() {
        runnable = object : Runnable {
            val animation = binding.animationBtnRecord
            val animation2 = binding.animation2BtnRecord

            override fun run() {
                binding.animationBtnRecord.animate().scaleX(2f).scaleY(2f).alpha(0f)
                    .setDuration(1000)
                    .withEndAction {
                        animation.scaleX = 1f
                        animation.scaleY = 1f
                        animation.alpha = 1f
                    }
                binding.animation2BtnRecord.animate().scaleX(2f).scaleY(2f).alpha(0f)
                    .setDuration(700)
                    .withEndAction {
                        animation2.scaleX = 1f
                        animation2.scaleY = 1f
                        animation2.alpha = 1f
                    }

                handlerAnimation.postDelayed(this, 1500)
            }
        }

        startPulse()
    }

    private fun startPulse() {
        handlerAnimation.post(runnable)
    }

    private fun observe() {
        viewModel.selectedPage.observe(this, Observer { page ->
            binding.viewPager2.currentItem = page
            binding.bottomNavView.menu
                .getItem(if (page < 2) page else page + 1).isChecked = true
        })

        viewModel.updateFCMToken.observe(this) {
            when (it) {
                is Resource.Success -> Log.d(TAG, "Token update successful")
                is Resource.Failure -> {
                    Log.e(TAG, "Error while updating token")
                    handleApiError(it)
                }
                else -> Unit
            }
        }

        viewModel.syncRecords.observe(this){

            when (it) {
                is Resource.Success -> Toast.makeText(this@MainActivity, it.value.message, Toast.LENGTH_SHORT).show()
                is Resource.Failure -> {
                    Log.e(TAG, "Error while sync record")
                    handleApiError(it)
                    println(TAG + "Sync Data Error V" + it.fetchErrorMessage())
                }
                else -> Unit
            }
        }
    }

    private fun setUpBottomNavView() {
        binding.bottomNavView.background = null
        binding.bottomNavView.menu.getItem(2).isEnabled = false

        binding.bottomNavView.setOnItemSelectedListener { options ->
            when (options.itemId) {
                R.id.tabHome -> viewModel.onNavOptionSelected(0)
                R.id.tabContest -> viewModel.onNavOptionSelected(1)
                R.id.tabTrain -> viewModel.onNavOptionSelected(2)
                R.id.tabProfile -> viewModel.onNavOptionSelected(3)
                else -> false
            }
        }
    }

    private fun setUpViewPager() {
        binding.viewPager2.adapter = adapter
        binding.viewPager2.isUserInputEnabled = false

        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.onPageSelected(position)
            }
        })
    }

    private fun handleHealthData(recordRequest: CreateRecordRequest) {
        Log.d(TAG, "Xử lý dữ liệu Health Connect:")
        Log.d(TAG, "Steps: ${recordRequest.steps}")
        Log.d(TAG, "Distance: ${recordRequest.distance} km")
        Log.d(TAG, "Avg Speed: ${recordRequest.avgSpeed} km/h")
        Log.d(TAG, "Heart Rate: ${recordRequest.heartRate} BPM")
        Log.d(TAG, "Time Range: ${recordRequest.startTime} - ${recordRequest.endTime}")
    }

    private fun getTodayHealthData() {
        lifecycleScope.launch {
            val startTime = userPreferences.lastSyncTime.first()?: LocalDateTime.now().minusDays(1)
            userPreferences.updateLastSyncTime()
            HealthConnectSyncHelper.syncData(this@MainActivity, startTime) { success, recordRequests ->
                if (success && recordRequests != null) {
                    if (recordRequests.isEmpty()) {
                        Log.w(TAG, "Không có bản ghi nào đáp ứng điều kiện (Steps, Distance, Avg Speed)")
                        Toast.makeText(this@MainActivity, "Không có dữ liệu hợp lệ từ Health Connect", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d(TAG, "Đồng bộ Health Connect thành công, số record: ${recordRequests.size}")
                        recordRequests.forEach { recordRequest ->
                            handleHealthData(recordRequest)
                        }
                    }
                } else {
                    Log.d(TAG, "Đồng bộ Health Connect thất bại hoặc không có quyền")
                }
            }
        }
    }
}