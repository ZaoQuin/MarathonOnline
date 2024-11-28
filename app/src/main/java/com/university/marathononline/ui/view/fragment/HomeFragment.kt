package com.university.marathononline.ui.view.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.university.marathononline.base.BaseFragment
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.contest.ContestApiService
import com.university.marathononline.databinding.FragmentHomeBinding
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.ui.viewModel.HomeViewModel
import com.university.marathononline.ui.adapter.EventAdapter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.math.abs

class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>() {

    private lateinit var adapter: EventAdapter
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

        setupAdapter()
        setupViewPager2()
        setupTabLayout()
        observeViewModel()

        handler.postDelayed(runnable, 3000)
    }

    private fun setupAdapter() {
        adapter = EventAdapter(emptyList())
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

    private fun observeViewModel() {
        viewModel.events.observe(viewLifecycleOwner) { events ->
            adapter.updateData(events)
            currentPage = 0
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
        val api = retrofitInstance.buildApi(ContestApiService::class.java, token)
        return listOf(ContestRepository(api))
    }
}
