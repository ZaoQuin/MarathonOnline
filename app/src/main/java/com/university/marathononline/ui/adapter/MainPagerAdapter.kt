package com.university.marathononline.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.university.marathononline.ui.view.fragment.ContestFragment
import com.university.marathononline.ui.view.fragment.HomeFragment
import com.university.marathononline.ui.view.fragment.NotifyFragment
import com.university.marathononline.ui.view.fragment.ProfileFragment

class MainPagerAdapter(fragment: FragmentActivity): FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> ContestFragment()
            2 -> NotifyFragment()
            3 -> ProfileFragment()
            else -> HomeFragment()
        }
    }
}