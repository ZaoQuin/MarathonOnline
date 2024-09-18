package com.university.marathononline

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.university.marathononline.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: MainTabAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = MainTabAdapter(this)

        setUpViewPager()
        setUpBottomNavView()

        viewModel.selectedPage.observe(this, Observer { tab ->
            binding.viewPager2.currentItem = tab
            binding.bottomNavView.menu
                .getItem(if (tab < 2) tab else tab + 1).isChecked = true
        })
    }

    private fun setUpBottomNavView() {
        binding.bottomNavView.background = null
        binding.bottomNavView.menu.getItem(2).isEnabled = false

        binding.bottomNavView.setOnItemSelectedListener { options ->
            when (options.itemId) {
                R.id.tabHome -> viewModel.onNavOptionSelected(0)
                R.id.tabLeaderBoard -> viewModel.onNavOptionSelected(1)
                R.id.tabNotify -> viewModel.onNavOptionSelected(2)
                R.id.tabSetting -> viewModel.onNavOptionSelected(3)
                else -> false
            }
        }
    }

    private fun setUpViewPager() {
        binding.viewPager2.adapter = adapter
        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.onPageSelected(position)
            }
        })
    }
}
