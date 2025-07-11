package com.university.marathononline.ui.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.university.marathononline.data.models.Notification
import com.university.marathononline.ui.view.fragment.ContestFragment
import com.university.marathononline.ui.view.fragment.HomeFragment
import com.university.marathononline.ui.view.fragment.ProfileFragment
import com.university.marathononline.ui.view.fragment.TrainingPlanFragment
import com.university.marathononline.utils.KEY_NOTIFICATIONS

class MainPagerAdapter(fragment: FragmentActivity,
                       private val notifications: List<Notification>,): FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> ContestFragment()
            2 -> TrainingPlanFragment()
            3 -> ProfileFragment()
            else -> HomeFragment()
        }

        val fragment = when (position) {
            0 -> HomeFragment()
            1 -> ContestFragment()
            2 -> TrainingPlanFragment()
            3 -> ProfileFragment()
            else -> HomeFragment()
        }
        return fragment
    }
}