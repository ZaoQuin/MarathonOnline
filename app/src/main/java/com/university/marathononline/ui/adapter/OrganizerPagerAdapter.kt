package com.university.marathononline.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.university.marathononline.ui.view.fragment.ContestManagementFragment
import com.university.marathononline.ui.view.fragment.OrganizerHomeFragment
import com.university.marathononline.ui.view.fragment.OrganizerStatisticsFragment
import com.university.marathononline.ui.view.fragment.ProfileFragment

class OrganizerPagerAdapter (fragment: FragmentActivity): FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OrganizerHomeFragment()
            1 -> ContestManagementFragment()
            2 -> OrganizerStatisticsFragment()
            3 -> ProfileFragment()
            else -> OrganizerHomeFragment()
        }
    }
}