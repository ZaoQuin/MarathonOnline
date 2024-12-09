package com.university.marathononline.ui.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.university.marathononline.data.models.Race
import com.university.marathononline.data.models.User
import com.university.marathononline.ui.view.fragment.DailyStatisticsFragment
import com.university.marathononline.ui.view.fragment.MonthlyStatisticsFragment
import com.university.marathononline.ui.view.fragment.WeeklyStatisticsFragment
import com.university.marathononline.utils.KEY_RACES
import com.university.marathononline.utils.KEY_USER

class ProfilePagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val races: List<Race>,
    private val user: User
) :
    FragmentStateAdapter(fragmentManager, lifecycle){
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            1 -> WeeklyStatisticsFragment()
            2 -> MonthlyStatisticsFragment()
            else -> DailyStatisticsFragment()
        }
        fragment.arguments = Bundle().apply {
            putSerializable(KEY_RACES, ArrayList(races))
            putSerializable(KEY_USER, user)
        }
        return fragment
    }
}