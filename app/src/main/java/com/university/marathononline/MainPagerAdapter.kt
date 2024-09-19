package com.university.marathononline

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.university.marathononline.home.HomeFragment
import com.university.marathononline.leaderBoard.LeaderBoardFragment
import com.university.marathononline.notify.NotifyFragment
import com.university.marathononline.profile.ProfileFragment

class MainPagerAdapter(fragment: FragmentActivity): FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> LeaderBoardFragment()
            2 -> NotifyFragment()
            3 -> ProfileFragment()
            else -> HomeFragment()
        }
    }
}