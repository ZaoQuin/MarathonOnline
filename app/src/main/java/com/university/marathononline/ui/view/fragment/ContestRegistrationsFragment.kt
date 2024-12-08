package com.university.marathononline.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.university.marathononline.R
import com.university.marathononline.base.BaseFragment
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.contest.ContestApiService
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.databinding.FragmentContestRegistrationsBinding
import com.university.marathononline.ui.adapter.RegistrationAdapter
import com.university.marathononline.ui.viewModel.ManagementDetailsContestActivityViewModel
import com.university.marathononline.utils.KEY_CONTEST
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class ContestRegistrationsFragment : BaseFragment<ManagementDetailsContestActivityViewModel, FragmentContestRegistrationsBinding>() {

    private lateinit var registrationAdapter: RegistrationAdapter

    companion object {
        fun newInstance(contest: Contest) = ContestRegistrationsFragment().apply {
            arguments = Bundle().apply {
                putSerializable(KEY_CONTEST, contest)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contest = arguments?.getSerializable(KEY_CONTEST) as? Contest
        contest?.let {
            viewModel.apply {
                setContest(it)
            }
        }
        setUpObserve()
    }

    private fun setRegistrationAdapter() {
        val contest = viewModel.contest.value
        binding.rvRegistrations.layoutManager = LinearLayoutManager(requireContext())
        registrationAdapter = RegistrationAdapter(contest!!.registrations!!, contest!!)
        binding.rvRegistrations.adapter = registrationAdapter
    }

    private fun setUpObserve() {
        viewModel.contest.observe(viewLifecycleOwner){
            setRegistrationAdapter()
            setUpUI(it)
        }
    }

    private fun setUpUI(contest: Contest) {
        binding.tvParticipantCount.text = getString(R.string.contest_participant_count, contest.registrations!!.size.toString())
    }

    override fun getViewModel() = ManagementDetailsContestActivityViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentContestRegistrationsBinding =
        FragmentContestRegistrationsBinding.inflate(inflater, container, false)

    override fun getFragmentRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val apiContest = retrofitInstance.buildApi(ContestApiService::class.java, token)
        return listOf(
            ContestRepository(apiContest)
        )
    }
}