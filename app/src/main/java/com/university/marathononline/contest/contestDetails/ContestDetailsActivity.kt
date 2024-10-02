package com.university.marathononline.contest.contestDetails

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.university.marathononline.R
import com.university.marathononline.databinding.ActivityContestDetailsBinding

class ContestDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContestDetailsBinding
    private val viewModel: ContestDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityContestDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpTabLayout()
        setUpScrollView()
        setUpBackButton()
    }

    private fun setUpBackButton() {
        binding.buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun setUpScrollView() {
        binding.scrollView.viewTreeObserver.addOnScrollChangedListener {
            val scrollY = binding.scrollView.scrollY
            val sectionDetailsTop = getTopRelativeToParent(binding.sectionDetails)
            val rewardsTop = getTopRelativeToParent(binding.sectionRewards)
            val regulationsTop = getTopRelativeToParent(binding.sectionRegulations)
            val organizationalTop = getTopRelativeToParent(binding.sectionOrganizational)

            when {
                scrollY >= organizationalTop -> {
                    viewModel.selectTab(3)
                }
                scrollY >= regulationsTop -> {
                    viewModel.selectTab(2)
                }
                scrollY >= rewardsTop -> {
                    viewModel.selectTab(1)
                }
                scrollY >= sectionDetailsTop -> {
                    viewModel.selectTab(0)
                }
            }
        }
    }

    private fun getTopRelativeToParent(view: View): Int {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        return location[1] - binding.scrollView.top
    }

    private fun setUpTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewModel.selectTab(tab?.position ?: 0)
                when (tab?.position) {
                    0 -> binding.scrollView.smoothScrollTo(0, binding.sectionDetails.top)
                    1 -> binding.scrollView.smoothScrollTo(0, binding.sectionRewards.top)
                    2 -> binding.scrollView.smoothScrollTo(0, binding.sectionRegulations.top)
                    3 -> binding.scrollView.smoothScrollTo(0, binding.sectionOrganizational.top)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
}