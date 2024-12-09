package com.university.marathononline.ui.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.university.marathononline.R
import com.university.marathononline.base.BaseFragment
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.auth.AuthApiService
import com.university.marathononline.data.api.contest.ContestApiService
import com.university.marathononline.data.api.race.RaceApiService
import com.university.marathononline.data.models.Race
import com.university.marathononline.data.models.User
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.data.repository.RaceRepository
import com.university.marathononline.databinding.FragmentProfileBinding
import com.university.marathononline.ui.adapter.ProfilePagerAdapter
import com.university.marathononline.ui.view.activity.InformationActivity
import com.university.marathononline.ui.view.activity.RunnerContestActivity
import com.university.marathononline.ui.view.activity.RunnerRewardsActivity
import com.university.marathononline.ui.viewModel.ProfileViewModel
import com.university.marathononline.utils.KEY_CONTESTS
import com.university.marathononline.utils.KEY_REWARDS
import com.university.marathononline.utils.startNewActivity
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
        val apiAuth = retrofitInstance.buildApi(AuthApiService::class.java, token)
        val apiRace = retrofitInstance.buildApi(RaceApiService::class.java, token)
        val apiContest = retrofitInstance.buildApi(ContestApiService::class.java, token)
        return listOf(AuthRepository(apiAuth, userPreferences),
            RaceRepository(apiRace), ContestRepository(apiContest))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getUser()
        viewModel.getMyContest()
        setUpButton()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getUser()
        viewModel.getMyContest()
    }

    private fun setUpButton() {
        binding.informationButton.setOnClickListener {
            val intent = Intent(requireContext(), InformationActivity::class.java)
            startActivity(intent)
        }

        binding.myContest.setOnClickListener {
            val contests = viewModel.contests.value

            if(contests!=null)
                startNewActivity(
                    RunnerContestActivity::class.java,
                    mapOf(
                        KEY_CONTESTS to contests
                    )
                )
        }

        binding.myReward.setOnClickListener{
            val rewards = viewModel.rewards.value

            if(rewards!=null)
                startNewActivity(
                    RunnerRewardsActivity::class.java,
                    mapOf(
                        KEY_REWARDS to rewards
                    )
                )
        }


        Glide.with(this)
            .asGif()
            .load(R.drawable.ic_reward_2_)
            .into(binding.myRewardIcon)


        Glide.with(this)
            .asGif()
            .load(R.drawable.ic_contest_2_)
            .into(binding.myContestIcon)
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

    private fun setUpViewPager(races: List<Race>, user: User) {
        adapter = ProfilePagerAdapter(childFragmentManager, lifecycle, races, user)
        binding.viewPager2.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.getRaceResponse.observe(viewLifecycleOwner) {
            when(it){
                is Resource.Success ->{
                    setUpViewPager(it.value, viewModel.user.value!!)
                    setUpTabLayout()
                }
                is Resource.Failure -> handleApiError(it)
                else -> Unit
            }
        }

        viewModel.getUserResponse.observe(viewLifecycleOwner){
            when(it){
                is Resource.Success -> {
                    viewModel.setUser(it.value)
                    viewModel.getRaces()
                }
                is Resource.Failure -> {
                    handleApiError(it)
                    if(it.errorCode == 500) {
                        Toast.makeText(requireContext(), "Phiên bản làm việc đã hết hạn, vui lòng đăng nhập lại", Toast.LENGTH_LONG).show()
                        logout()
                    }
                }
                else -> Unit
            }
        }

        viewModel.getMyContestResponse.observe(viewLifecycleOwner){
            when(it){
                is Resource.Success -> {
                    binding.myContestsNumber.text = it.value.contests.size.toString()
                    viewModel.setContests(it.value.contests)
                }
                is Resource.Failure -> handleApiError(it)
                else -> Unit
            }
        }

        viewModel.contests.observe(viewLifecycleOwner) {
            viewModel.setRewards(userPreferences.email.toString())
            binding.myRewardsNumber.text = viewModel.rewards.value?.size.toString()
        }
    }
}
