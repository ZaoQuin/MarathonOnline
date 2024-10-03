package com.university.marathononline.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.university.marathononline.profile.information.InformationActivity
import com.university.marathononline.databinding.FragmentProfileBinding
import com.university.marathononline.profile.adapter.ProfilePagerAdapter

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel: ProfileViewModel by activityViewModels()
    private lateinit var adapter: ProfilePagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        setUpViewPager()
        setUpTabLayout()
        setUpButton()

        return binding.root
    }

    private fun setUpButton() {
        binding.informationButton.setOnClickListener{
            val intent = Intent(binding.root.context, InformationActivity::class.java)
            binding.root.context.startActivity(intent)
        }
    }

    private fun setUpTabLayout() {
        TabLayoutMediator(binding.tabLayout, binding.viewPager2) {
            tab, position -> tab.text = getTitle(position)
        }.attach()
    }

    private fun getTitle(position: Int): String? {
        return when (position){
            1 -> "Tháng"
            2 -> "Năm"
            else -> "Tuần"
        }
    }

    private fun setUpViewPager() {
        adapter = ProfilePagerAdapter(childFragmentManager, lifecycle)
        binding.viewPager2.adapter = adapter
    }
}