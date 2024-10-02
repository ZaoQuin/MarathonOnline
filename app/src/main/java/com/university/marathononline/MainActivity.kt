package com.university.marathononline

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.university.marathononline.contest.contestDetails.ContestDetailsActivity
import com.university.marathononline.databinding.ActivityMainBinding
import com.university.marathononline.record.RecordActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: MainPagerAdapter

    private var handlerAnimation = Handler()
    private var statusAnimation = false
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = MainPagerAdapter(this)

        setUpViewPager()
        setUpBottomNavView()
        setUpRecordButton()
        setUpAnimation()

        observe()
    }

    private fun setUpRecordButton() {
        binding.btnRecord.setOnClickListener{
            val intent = Intent(binding.root.context, RecordActivity::class.java)
            binding.root.context.startActivity(intent)
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
                R.id.tabHome -> viewModel.onNavOptionSelected(0)
                R.id.tabContest -> viewModel.onNavOptionSelected(1)
                R.id.tabNotify -> viewModel.onNavOptionSelected(2)
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
