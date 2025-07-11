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
import com.university.marathononline.ui.view.activity.SettingActivity
import com.university.marathononline.base.BaseFragment
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.auth.AuthApiService
import com.university.marathononline.data.api.contest.ContestApiService
import com.university.marathononline.data.api.record.RecordApiService
import com.university.marathononline.data.models.Record
import com.university.marathononline.data.models.User
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.data.repository.RecordRepository
import com.university.marathononline.databinding.FragmentProfileBinding
import com.university.marathononline.ui.adapter.ProfilePagerAdapter
import com.university.marathononline.ui.view.activity.InformationActivity
import com.university.marathononline.ui.view.activity.RunnerContestActivity
import com.university.marathononline.ui.view.activity.RunnerRewardsActivity
import com.university.marathononline.ui.viewModel.ProfileViewModel
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
        val apiRecord = retrofitInstance.buildApi(RecordApiService::class.java, token)
        val apiContest = retrofitInstance.buildApi(ContestApiService::class.java, token)
        return listOf(AuthRepository(apiAuth, userPreferences),
            RecordRepository(apiRecord), ContestRepository(apiContest))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpButton()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getUser()
    }

    override fun onStart() {
        super.onStart()
        viewModel.getUser()
    }

    private fun setUpButton() {
        binding.informationButton.setOnClickListener {
            val intent = Intent(requireContext(), InformationActivity::class.java)
            startActivity(intent)
        }

        binding.myContest.setOnClickListener {
            startNewActivity(
                RunnerContestActivity::class.java
            )
        }

        binding.myReward.setOnClickListener{
            startNewActivity(
                RunnerRewardsActivity::class.java
            )
        }


        binding.settingButton.setOnClickListener{
            startNewActivity(SettingActivity::class.java)
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
            1 -> "Tuần"
            2 -> "Tháng"
            else -> "Ngày"
        }
    }

    private fun setUpViewPager(user: User) {
        adapter = ProfilePagerAdapter(childFragmentManager, lifecycle, user)
        binding.viewPager2.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.user.observe(viewLifecycleOwner) {
            val user = viewModel.user.value
            setUpViewPager(user!!)
            setUpTabLayout()
            if(user!!.avatarUrl.isNullOrEmpty()){
                binding.informationButton.setImageResource(R.drawable.example_avatar)
            } else {
                Glide.with(this)
                    .load(user.avatarUrl)
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.example_avatar)
                    .into(binding.informationButton)
            }
        }

        viewModel.getUserResponse.observe(viewLifecycleOwner){
            when(it){
                is Resource.Success -> {
                    viewModel.getMyContest()
                    viewModel.getRecords()
                    viewModel.setUser(it.value)
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
                    val user = viewModel.user.value
                    val rewards = it.value.contests?.flatMap { contest ->
                        contest.registrations!!
                            .filter { registration ->
                                registration.runner.email == user?.email
                            }
                            .flatMap { registration ->
                                registration.rewards ?: emptyList()
                            }
                    }
                    binding.myContestsNumber.text = it.value.contests.size.toString()
                    binding.myRewardsNumber.text = rewards?.size.toString()
                }
                is Resource.Failure -> handleApiError(it)
                else -> Unit
            }
        }
    }
}
