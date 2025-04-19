package com.university.marathononline.ui.view.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
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
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.databinding.FragmentHomeBinding
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.data.repository.NotificationRepository
import com.university.marathononline.ui.adapter.ContestAdapter
import com.university.marathononline.ui.view.activity.NotificationsActivity
import com.university.marathononline.ui.view.activity.RunnerContestActivity
import com.university.marathononline.ui.viewModel.HomeViewModel
import com.university.marathononline.utils.KEY_CONTESTS
import com.university.marathononline.utils.KEY_NOTIFICATIONS
import com.university.marathononline.utils.startNewActivity
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.math.abs

class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>() {

    private lateinit var adapter: ContestAdapter
    private val handler = Handler(Looper.getMainLooper())
    private var currentPage = 0

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
        viewModel.getActiveContests()

        setupAdapter()
        setupViewPager2()
        setupTabLayout()
        setupNotifyButton()
        initializeUI()
        observeViewModel()

        handler.postDelayed(runnable, 3000)
    }

    private fun initializeUI(){
        binding.apply {
            notifyButton.setOnClickListener{ navigationNotifications()}
        }
    }

    private fun navigationNotifications(){
        val notifications = viewModel.notifications.value?: emptyList()

        if(notifications!=null)
            startNewActivity(
                NotificationsActivity::class.java,
                mapOf(
                    KEY_CONTESTS to notifications
                )
            )
    }

    private fun setupNotifyButton(){
        viewModel.getNotifications();
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
        viewModel.contests.observe(viewLifecycleOwner) {
            Log.d("ContestFragment", it.toString())
            when(it){
                is Resource.Success -> {
                    adapter.updateData(it.value.contests)
                }
                is Resource.Failure -> {
                    handleApiError(it)
                    it.fetchErrorMessage()
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
                    viewModel.setNotifications(it.value);
                    val unreadCount = it.value.filter { notification -> !notification.isRead!! }.size
                    val badgeDrawable = BadgeDrawable.create(requireContext()).apply {
                        isVisible = unreadCount >= 0
                        number = unreadCount
                        backgroundColor = resources.getColor(R.color.red, null)
                    }
                    BadgeUtils.attachBadgeDrawable(badgeDrawable, binding.notifyButton)
                }
                is Resource.Failure -> handleApiError(it)
                else -> Unit
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(runnable)
    }

    override fun getViewModel() = HomeViewModel::class.java

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentHomeBinding.inflate(inflater, container, false)

    override fun getFragmentRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val contestApi = retrofitInstance.buildApi(ContestApiService::class.java, token)
        val authApi = retrofitInstance.buildApi(AuthApiService::class.java, token)
        val notifyApi = retrofitInstance.buildApi(NotificationApiService::class.java, token)
        return listOf(ContestRepository(contestApi),
            AuthRepository(authApi, userPreferences),
            NotificationRepository(notifyApi))
    }
}
