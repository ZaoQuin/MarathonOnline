package com.university.marathononline.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.university.marathononline.data.models.Contest
import com.university.marathononline.ui.view.fragment.ContestDetailsFragment
import com.university.marathononline.ui.view.fragment.ContestRegistrationsFragment

class ManagementDetailsContestPagerAdapter (
    fragmentActivity: FragmentActivity,
    private val contest: Contest
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ContestDetailsFragment.newInstance(contest)
            1 -> ContestRegistrationsFragment.newInstance(contest)
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}