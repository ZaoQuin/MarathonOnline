package com.university.marathononline.ui.view.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayoutMediator
import com.university.marathononline.base.BaseFragment
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.contest.ContestApiService
import com.university.marathononline.data.api.race.RaceApiService
import com.university.marathononline.data.models.Race
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.data.repository.RaceRepository
import com.university.marathononline.databinding.FragmentProfileBinding
import com.university.marathononline.ui.adapter.ProfilePagerAdapter
import com.university.marathononline.ui.view.activity.InformationActivity
import com.university.marathononline.ui.viewModel.ProfileViewModel
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class ProfileFragment : BaseFragment<ProfileViewModel, FragmentProfileBinding>() {

    private lateinit var adapter: ProfilePagerAdapter

    override fun getViewModel(): Class<ProfileViewModel> = ProfileViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentProfileBinding = FragmentProfileBinding.inflate(inflater, container, false)

    override fun getFragmentRepositories():  List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val apiRace = retrofitInstance.buildApi(RaceApiService::class.java, token)
        val apiContest = retrofitInstance.buildApi(ContestApiService::class.java, token)
        return listOf(RaceRepository(apiRace), ContestRepository(apiContest))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getRaces()
        viewModel.getMyContest()
        setUpButton()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getRaces()
        viewModel.getMyContest()
    }

    private fun setUpButton() {
        binding.informationButton.setOnClickListener {
            val intent = Intent(requireContext(), InformationActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setUpTabLayout() {
        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            tab.text = getTitle(position)
        }.attach()
    }

    private fun getTitle(position: Int): String {
        return when (position) {
            1 -> "Tháng"
            2 -> "Năm"
            else -> "Ngày"
        }
    }

    private fun setUpViewPager(races: List<Race>) {
        adapter = ProfilePagerAdapter(childFragmentManager, lifecycle, races)
        binding.viewPager2.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.getRaceResponse.observe(viewLifecycleOwner) {
            when(it){
                is Resource.Success ->{
                    setUpViewPager(it.value)
                    setUpTabLayout()
                }
                is Resource.Failure -> handleApiError(it)
                else -> Unit
            }
        }

        viewModel.getMyContestResponse.observe(viewLifecycleOwner){
            Log.e("GetMyContest", it.toString())
            when(it){
                is Resource.Success -> {
                    binding.myContestsNumber.text = it.value.contests.size.toString()
                }
                is Resource.Failure -> handleApiError(it)
                else -> Unit
            }
        }
    }
}
