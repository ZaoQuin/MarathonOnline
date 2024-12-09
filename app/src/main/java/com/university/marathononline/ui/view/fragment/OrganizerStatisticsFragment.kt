package com.university.marathononline.ui.view.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.university.marathononline.base.BaseFragment
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.auth.AuthApiService
import com.university.marathononline.data.api.contest.ContestApiService
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.EContestStatus
import com.university.marathononline.data.models.EGender
import com.university.marathononline.data.models.ERegistrationStatus
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.databinding.FragmentOrganizerStatisticsBinding
import com.university.marathononline.ui.viewModel.OrganizerStatisticsViewModel
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.KEY_CONTEST
import com.university.marathononline.utils.convertToVND
import com.university.marathononline.utils.formatDistance
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class OrganizerStatisticsFragment : BaseFragment<OrganizerStatisticsViewModel, FragmentOrganizerStatisticsBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private fun observe() {
        viewModel.getContestByJwtResponse.observe(viewLifecycleOwner){
            when (it) {
                is Resource.Success -> {
                    Log.d("OrganizerStatistics", "Success: Contest data received")
                    Log.d("OrganizerStatistics", "Contest Data: ${it.value}")
                    viewModel.setContest(it.value)
                }
                is Resource.Failure -> {
                    Log.e("OrganizerStatistics", "Failure: ${it.errorMessage}")
                    handleApiError(it)
                    if(it.errorCode == 500) {
                        Toast.makeText(requireContext(), "Phiên bản làm việc đã hết hạn, vui lòng đăng nhập lại", Toast.LENGTH_LONG).show()
                        logout()
                    }
                }
                else -> {
                    // Log trường hợp khác (chưa xác định)
                    Log.d("OrganizerStatistics", "Unknown response: $it")
                }
            }
        }

        viewModel.contests.observe(viewLifecycleOwner){
            viewModel.statistics(it)
        }

        viewModel.contestsCount.observe(viewLifecycleOwner) {
            binding.tvContestsCount.text = it.toString()
        }

        viewModel.totalDistance.observe(viewLifecycleOwner) {
            binding.tvDistance.text = formatDistance(it)
        }

        viewModel.totalSteps.observe(viewLifecycleOwner) {
            binding.tvSteps.text = it.toString()
        }

        viewModel.totalTime.observe(viewLifecycleOwner) {
            binding.tvTimeTaken.text = DateUtils.convertSecondsToHHMMSS(it)
        }

        viewModel.fee.observe(viewLifecycleOwner) {
            binding.tvTotalFree.text = convertToVND(it)
        }

        viewModel.genderStatistics.observe(viewLifecycleOwner) {
            updateGenderPieChart(it)
        }

        viewModel.contestStatus.observe(viewLifecycleOwner) {
            updateContestStatusPieChart(it)
        }

        viewModel.activeRegistration.observe(viewLifecycleOwner) {
            val completed = it.count { registration -> registration.status == ERegistrationStatus.COMPLETED }
            val total = it.size
            val completionRate = if (total > 0) (completed.toDouble() / total) * 100 else 0.0
            binding.tvContestCompleted.text = "$completionRate%"
        }
    }

    private fun updateGenderPieChart(genderStatistics: Map<EGender, Int>) {
        val entries = genderStatistics.map {
            PieEntry(it.value.toFloat(), it.key.value )
        }

        val dataSet = PieDataSet(entries, "Giới tính")
        dataSet.colors = listOf(Color.RED, Color.RED)  // Ví dụ màu cho nam và nữ
        val pieData = PieData(dataSet)

        binding.ageContests.data = pieData
        binding.ageContests.invalidate()
    }

    private fun updateContestStatusPieChart(contestStatus: Map<EContestStatus, Int>) {
        val entries = contestStatus?.map {
            PieEntry(it.value.toFloat(), it.key.value)
        }

        val dataSet = PieDataSet(entries, "Trạng thái cuộc thi")
        val colors = listOf(
            Color.LTGRAY,   // PENDING
            Color.GREEN,    // ACTIVE
            Color.YELLOW,   // FINISHED
            Color.RED,      // CANCELLED
            Color.BLUE,     // NOT_APPROVAL
            Color.MAGENTA   // DELETED
        )
        dataSet.colors = colors
        val pieData = PieData(dataSet)

        binding.statusContests.data = pieData
        binding.statusContests.invalidate()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getContest()
        observe()
    }

    override fun getViewModel(): Class<OrganizerStatisticsViewModel> {
        return OrganizerStatisticsViewModel::class.java
    }

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentOrganizerStatisticsBinding {
        return FragmentOrganizerStatisticsBinding.inflate(inflater, container, false)
    }

    override fun getFragmentRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val apiAuth = retrofitInstance.buildApi(AuthApiService::class.java, token)
        val apiContest = retrofitInstance.buildApi(ContestApiService::class.java, token)
        return listOf(AuthRepository(apiAuth, userPreferences), ContestRepository(apiContest))
    }
}
