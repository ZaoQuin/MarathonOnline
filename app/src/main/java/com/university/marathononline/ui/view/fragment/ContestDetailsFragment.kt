package com.university.marathononline.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.university.marathononline.base.BaseFragment
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.contest.ContestApiService
import com.university.marathononline.data.api.registration.RegistrationApiService
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.EContestStatus
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.data.repository.RegistrationRepository
import com.university.marathononline.databinding.FragmentContestDetailsBinding
import com.university.marathononline.ui.adapter.RewardAdapter
import com.university.marathononline.ui.adapter.RuleAdapter
import com.university.marathononline.ui.viewModel.ManagementDetailsContestActivityViewModel
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.KEY_CONTEST
import com.university.marathononline.utils.KEY_REGISTRATIONS
import com.university.marathononline.utils.convertToVND
import com.university.marathononline.utils.enable
import com.university.marathononline.utils.formatDistance
import com.university.marathononline.utils.visible
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.Serializable
import java.time.LocalDateTime

class ContestDetailsFragment : BaseFragment<ManagementDetailsContestActivityViewModel, FragmentContestDetailsBinding>() {

    companion object {
        fun newInstance(contest: Contest) = ContestDetailsFragment().apply {
            arguments = Bundle().apply {
                putSerializable(KEY_CONTEST, contest)
            }
        }
    }

    private lateinit var ruleAdapter: RuleAdapter
    private lateinit var rewardAdapter: RewardAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val contest = arguments?.getSerializable(KEY_CONTEST) as? Contest
        contest?.let {
            viewModel.apply {
                setContest(it)
                it.rules?.let { it1 ->
                    setRules(it1)
                }
                it.rewards?.let { it1 ->
                    setRewardGroups(it1)
                }
                it.registrationDeadline?.let { it2 ->
                    setDeadlineTime(it2)
                }
            }
        }

        setUpData()
        setRuleAdapter()
        setRewardAdapter()
        setUpLeaderBoard(savedInstanceState)
        setUpObserve()
    }

    private fun setUpObserve() {
        viewModel.rules.observe(viewLifecycleOwner) {
            ruleAdapter.updateData(it)
        }
        viewModel.rewardGroup.observe(viewLifecycleOwner) {
            rewardAdapter.updateData(it)
        }
        viewModel.remainingTime.observe(viewLifecycleOwner) { time ->
            val timeParts = time.split(":")
            binding.apply {
                daysTextView.text = timeParts[0]
                hoursTextView.text = timeParts[1]
                minutesTextView.text = timeParts[2]
                secondsTextView.text = timeParts[3]
            }
        }
        viewModel.deadlineTime.observe(viewLifecycleOwner) {
            viewModel.startCountdown()
        }
    }

    private fun setUpLeaderBoard(savedInstanceState: Bundle?) {
        val registrations = viewModel.contest.value?.registrations
        val fragment = LeaderBoardFragment().apply {
            arguments = Bundle().apply {
                putSerializable(KEY_REGISTRATIONS, registrations as Serializable)
            }
        }

        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, fragment)
                .commit()
        }
    }

    private fun setRuleAdapter() {
        binding.rules.layoutManager = LinearLayoutManager(requireContext())
        ruleAdapter = RuleAdapter(emptyList())
        binding.rules.adapter = ruleAdapter
    }
    private fun setRewardAdapter() {
        binding.rewards.layoutManager = LinearLayoutManager(requireContext())
        rewardAdapter = RewardAdapter(emptyList())
        binding.rewards.adapter = rewardAdapter
    }

    private fun setUpData() {
        viewModel.contest.value?.let {
            binding.apply {
                tvDistance.text = it.distance?.let { it1 -> formatDistance(it1) }
                tvFee.text = it.fee?.let { it1 -> convertToVND(it1) }
                tvMaxMembers.text = if (it.maxMembers == 0) "0" else it.maxMembers.toString()
                contestName.text = it.name
                startDate.text = it.startDate?.let { it1 -> DateUtils.convertToVietnameseDate(it1) }
                endDate.text = it.endDate?.let { it1 -> DateUtils.convertToVietnameseDate(it1) }
                deadlineDate.text = it.registrationDeadline?.let { it1 -> DateUtils.convertToVietnameseDate(it1) }
                createDate.text = it.createDate?.let { it1 -> DateUtils.convertToVietnameseDate(it1) }
                contentDetails.text = it.description
                organizationalName.text = it.organizer?.fullName
                emailOrganizer.text = it.organizer?.email
                sectionLeaderBoard.visible(it.status == EContestStatus.ACTIVE
                        || it.status == EContestStatus.FINISHED)
                status.text = it.status?.value

                if (it.startDate?.let { start ->
                        DateUtils.convertStringToLocalDateTime(start).isBefore(LocalDateTime.now())
                    } == true) {
                    sectionLeaderBoard.visible(it.status == EContestStatus.ACTIVE
                            || it.status == EContestStatus.FINISHED)
                }
            }
        }
    }

    override fun getViewModel() = ManagementDetailsContestActivityViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentContestDetailsBinding =
        FragmentContestDetailsBinding.inflate(inflater, container, false)

    override fun getFragmentRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val apiContest = retrofitInstance.buildApi(ContestApiService::class.java, token)
        val apiRegistration = retrofitInstance.buildApi(RegistrationApiService::class.java, token)
        return listOf(
            ContestRepository(apiContest), RegistrationRepository(apiRegistration)
        )
    }

}