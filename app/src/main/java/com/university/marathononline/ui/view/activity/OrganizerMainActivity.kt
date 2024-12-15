package com.university.marathononline.ui.view.activity

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.university.marathononline.R
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.auth.AuthApiService
import com.university.marathononline.data.api.notify.NotificationApiService
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.data.repository.NotificationRepository
import com.university.marathononline.databinding.ActivityOrganizerMainBinding
import com.university.marathononline.ui.adapter.OrganizerPagerAdapter
import com.university.marathononline.ui.viewModel.MainViewModel
import com.university.marathononline.utils.startNewActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class OrganizerMainActivity : BaseActivity<MainViewModel, ActivityOrganizerMainBinding>() {

    private lateinit var adapter: OrganizerPagerAdapter

    private var handlerAnimation = Handler()
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = OrganizerPagerAdapter(this)

        setUpViewPager()
        setUpBottomNavView()
        setUpRecordButton()
        setUpAnimation()

        observe()
    }

    override fun getViewModel(): Class<MainViewModel> = MainViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater): ActivityOrganizerMainBinding {
        return ActivityOrganizerMainBinding.inflate(inflater)
    }

    override fun getActivityRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val api = retrofitInstance.buildApi(NotificationApiService::class.java, token)
        return listOf(NotificationRepository(api))
    }

    private fun setUpRecordButton() {
        binding.btnAddContest.setOnClickListener{
            startNewActivity(AddContestActivity::class.java)
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
    }

    private fun setUpBottomNavView() {
        binding.bottomNavView.background = null
        binding.bottomNavView.menu.getItem(2).isEnabled = false

        binding.bottomNavView.setOnItemSelectedListener { options ->
            when (options.itemId) {
                R.id.tabOrganizerHome -> viewModel.onNavOptionSelected(0)
                R.id.tabManagementContest -> viewModel.onNavOptionSelected(1)
                R.id.tabOrganizerStatistics -> viewModel.onNavOptionSelected(2)
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
}
