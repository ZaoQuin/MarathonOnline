package com.university.marathononline.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.university.marathononline.R
import com.university.marathononline.base.BaseFragment
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.contest.ContestApiService
import com.university.marathononline.data.api.registration.RegistrationApiService
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.ERegistrationStatus
import com.university.marathononline.data.models.Registration
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.data.repository.RegistrationRepository
import com.university.marathononline.databinding.FragmentContestRegistrationsBinding
import com.university.marathononline.ui.adapter.RegistrationAdapter
import com.university.marathononline.ui.viewModel.ManagementDetailsContestActivityViewModel
import com.university.marathononline.utils.KEY_CONTEST
import handleApiError
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
        registrationAdapter = RegistrationAdapter(contest!!.registrations!!, contest!!,
            onBlockRegistration = ::onBlockRegistration)
        binding.rvRegistrations.adapter = registrationAdapter
    }

    private fun setUpObserve() {
        viewModel.contest.observe(viewLifecycleOwner){
            setRegistrationAdapter()
            setUpUI(it)
        }

        viewModel.blockRegistration.observe(viewLifecycleOwner){
            when(it){
                is Resource.Success -> {
                    Toast.makeText(requireContext(), "Đã chặn ${it.value.runner.fullName}", Toast.LENGTH_SHORT).show()
                    val updateData = registrationAdapter.getCurrentData().map { registration ->
                        if(registration.id == it.value.id) it.value else registration
                    }
                    registrationAdapter.updateData(updateData)
                }
                is Resource.Failure -> handleApiError(it)
                else -> Unit
            }
        }
    }

    private fun onBlockRegistration(registration: Registration) {
        println("Blocked registration: ${registration.runner.fullName}")
        viewModel.block(registration)
    }

    private fun setUpUI(contest: Contest) {
        val participantCount = contest.registrations?.filter { it.status != ERegistrationStatus.PENDING }?.size ?: 0
        binding.tvParticipantCount.text = getString(R.string.contest_participant_count, participantCount.toString())
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
        val apiRegistration = retrofitInstance.buildApi(RegistrationApiService::class.java, token)
        return listOf(
            ContestRepository(apiContest), RegistrationRepository(apiRegistration)
        )
    }
}