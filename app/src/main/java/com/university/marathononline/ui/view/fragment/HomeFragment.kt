package com.university.marathononline.ui.view.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.university.marathononline.R
import com.university.marathononline.base.BaseFragment
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.auth.AuthApiService
import com.university.marathononline.data.api.contest.ContestApiService
import com.university.marathononline.data.api.notify.NotificationApiService
import com.university.marathononline.data.api.training.TrainingDayApiService
import com.university.marathononline.data.models.Notification
import com.university.marathononline.data.models.TrainingDay
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.databinding.FragmentHomeBinding
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.data.repository.NotificationRepository
import com.university.marathononline.data.repository.TrainingDayRepository
import com.university.marathononline.firebase.MyFirebaseMessagingService
import com.university.marathononline.ui.adapter.ContestAdapter
import com.university.marathononline.ui.view.activity.NotificationsActivity
import com.university.marathononline.ui.view.activity.RecordActivity
import com.university.marathononline.ui.viewModel.HomeViewModel
import com.university.marathononline.utils.ACTION_NEW_NOTIFICATION
import com.university.marathononline.utils.ACTION_UPDATE_BADGE
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.KEY_CONTESTS
import com.university.marathononline.utils.KEY_NOTIFICATION_DATA
import com.university.marathononline.utils.KEY_TRAINING_DAY
import com.university.marathononline.utils.startNewActivity
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.math.abs

class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>() {

    private lateinit var adapter: ContestAdapter
    private val handler = Handler(Looper.getMainLooper())
    private var currentPage = 0
    private var badgeDrawable: BadgeDrawable? = null

    private lateinit var notificationReceiver: BroadcastReceiver
    private lateinit var badgeUpdateReceiver: BroadcastReceiver

    private val runnable = object : Runnable {
        override fun run() {
            val itemCount = adapter.itemCount
            if (itemCount > 0) {
                currentPage = (currentPage + 1) % itemCount
                binding.viewPager2.setCurrentItem(currentPage, true)
            }
            handler.postDelayed(this, 3000)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.userFullNameText.text = runBlocking { userPreferences.fullName.first() }

        setupAdapter()
        setupViewPager2()
        setupTabLayout()
        setupNotifyButton()
        setupNotificationReceivers()
        initializeUI()
        observeViewModel()

        viewModel.getActiveContests()
        viewModel.getCurrentTrainingDay()
        viewModel.getNotifications()

        handler.postDelayed(runnable, 3000)
    }

    private fun setupNotificationReceivers() {
        notificationReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.getSerializableExtra(KEY_NOTIFICATION_DATA)?.let { notification ->
                    if (notification is Notification) {
                        val currentNotifications = viewModel.notifications.value?.toMutableList() ?: mutableListOf()

                        val existingIndex = currentNotifications.indexOfFirst { it.id == notification.id }
                        if (existingIndex == -1) {
                            currentNotifications.add(0, notification)
                            viewModel.setNotifications(currentNotifications)
                            updateBadge(currentNotifications)
                        }
                    }
                }
            }
        }

        badgeUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                viewModel.getNotifications()
            }
        }

        val notificationFilter = IntentFilter(ACTION_NEW_NOTIFICATION)
        val badgeFilter = IntentFilter(ACTION_UPDATE_BADGE)

        LocalBroadcastManager.getInstance(requireContext()).apply {
            registerReceiver(notificationReceiver, notificationFilter)
            registerReceiver(badgeUpdateReceiver, badgeFilter)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(notificationReceiver, notificationFilter, Context.RECEIVER_NOT_EXPORTED)
            requireContext().registerReceiver(badgeUpdateReceiver, badgeFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            requireContext().registerReceiver(notificationReceiver, notificationFilter)
            requireContext().registerReceiver(badgeUpdateReceiver, badgeFilter)
        }
    }

    @OptIn(ExperimentalBadgeUtils::class)
    private fun updateBadge(notifications: List<Notification>) {
        val unreadCount = notifications.count { it.isRead == false }

        badgeDrawable?.let { badge ->
            BadgeUtils.detachBadgeDrawable(badge, binding.notifyButton)
        }

        if (unreadCount > 0) {
            badgeDrawable = BadgeDrawable.create(requireContext()).apply {
                isVisible = true
                number = unreadCount
                backgroundColor = resources.getColor(R.color.red, null)
            }
            badgeDrawable?.let { badge ->
                BadgeUtils.attachBadgeDrawable(badge, binding.notifyButton)
            }
        }
    }

    private fun initializeUI(){
        binding.apply {
            notifyButton.setOnClickListener{ navigationNotifications()}
        }
    }

    private fun navigationNotifications(){
        val notifications = viewModel.notifications.value ?: emptyList()
        startNewActivity(
            NotificationsActivity::class.java,
            mapOf(KEY_CONTESTS to notifications)
        )
    }

    private fun setupNotifyButton(){
        updateBadge(emptyList())
    }

    private fun setupAdapter() {
        adapter = ContestAdapter(emptyList())
        binding.viewPager2.adapter = adapter
    }

    private fun setupViewPager2() {
        binding.viewPager2.apply {
            offscreenPageLimit = 3
            clipToPadding = false
            clipChildren = false
            getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            setPageTransformer(createCompositePageTransformer())
        }
    }

    private fun createCompositePageTransformer(): CompositePageTransformer {
        return CompositePageTransformer().apply {
            addTransformer(MarginPageTransformer(40))
            addTransformer { page, position ->
                val scaleFactor = 1 - abs(position)
                page.scaleY = 0.85f + scaleFactor * 0.15f
            }
        }
    }

    private fun setUpTrainingDayUI(trainingDay: TrainingDay){
        binding.itemTodayTraining.apply {
            root.visibility = View.VISIBLE
            tvWeek.text = trainingDay.week.toString()
            tvDay.text = trainingDay.dayOfWeek.toString()
            tvDateTime.text = DateUtils.convertToVietnameseDate(trainingDay.dateTime)
            tvSessionDetails.text = trainingDay.session.notes
            btnStartTraining.setOnClickListener{
                startNewActivity(RecordActivity::class.java,
                    mapOf(KEY_TRAINING_DAY to trainingDay))
            }
        }
    }

    private fun setupTabLayout() {
        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { _, _ -> }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    if (it.position != binding.viewPager2.currentItem) {
                        binding.viewPager2.setCurrentItem(it.position, true)
                        currentPage = it.position
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    @OptIn(ExperimentalBadgeUtils::class)
    private fun observeViewModel() {
        viewModel.getCurrentTrainingDay.observe(viewLifecycleOwner) {
            when(it){
                is Resource.Success -> {
                    setUpTrainingDayUI(it.value)
                }
                is Resource.Failure -> {
                    handleApiError(it)
                }
                else -> Unit
            }
        }

        viewModel.contests.observe(viewLifecycleOwner) {
            when(it){
                is Resource.Success -> {
                    adapter.updateData(it.value.contests)
                }
                is Resource.Failure -> {
                    handleApiError(it)
                    if(it.errorCode == 500) {
                        Toast.makeText(requireContext(), "Phiên bản làm việc đã hết hạn, vui lòng đăng nhập lại", Toast.LENGTH_LONG).show()
                        logout()
                    }
                }
                else -> Unit
            }
        }

        viewModel.getNotifiesResponse.observe(viewLifecycleOwner) {
            when(it){
                is Resource.Success -> {
                    viewModel.setNotifications(it.value)
                    updateBadge(it.value)
                }
                is Resource.Failure -> handleApiError(it)
                else -> Unit
            }
        }

        viewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            updateBadge(notifications)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(runnable)

        try {
            LocalBroadcastManager.getInstance(requireContext()).apply {
                unregisterReceiver(notificationReceiver)
                unregisterReceiver(badgeUpdateReceiver)
            }
            requireContext().apply {
                unregisterReceiver(notificationReceiver)
                unregisterReceiver(badgeUpdateReceiver)
            }
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error unregistering receivers: ${e.message}")
        }
    }

    override fun getViewModel() = HomeViewModel::class.java

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentHomeBinding.inflate(inflater, container, false)

    override fun getFragmentRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val contestApi = retrofitInstance.buildApi(ContestApiService::class.java, token)
        val authApi = retrofitInstance.buildApi(AuthApiService::class.java, token)
        val trainingDayApi = retrofitInstance.buildApi(TrainingDayApiService::class.java, token)
        val notifyApi = retrofitInstance.buildApi(NotificationApiService::class.java, token)
        return listOf(ContestRepository(contestApi),
            AuthRepository(authApi, userPreferences),
            TrainingDayRepository(trainingDayApi),
            NotificationRepository(notifyApi))
    }
}