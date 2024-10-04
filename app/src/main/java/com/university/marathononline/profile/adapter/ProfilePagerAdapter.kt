package com.university.marathononline.profile.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.university.marathononline.profile.statistics.WeeklyStatisticsFragment
import com.university.marathononline.profile.statistics.MonthlyStatisticsFragment
import com.university.marathononline.profile.statistics.YearlyStatisticsFragment

class ProfilePagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle){
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> MonthlyStatisticsFragment()
            2 -> YearlyStatisticsFragment()
            else -> WeeklyStatisticsFragment()
        }
    }
}