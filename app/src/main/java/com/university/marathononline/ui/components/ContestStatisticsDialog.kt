package com.university.marathononline.ui.components

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.university.marathononline.R
import com.university.marathononline.data.models.Contest
import com.university.marathononline.databinding.DialogContestStatisticsBinding
import com.university.marathononline.ui.adapter.RaceStatisticsAdapter
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.formatDistance
import com.university.marathononline.utils.getContestStatusColor
import com.university.marathononline.utils.getContestStatusText

class ContestStatisticsDialog(
    context: Context,
    private val contest: Contest,
    private val email: String
) : Dialog(context) {

    private lateinit var binding: DialogContestStatisticsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogContestStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val context = context

        // Contest Info
        binding.tvContestName.text = contest.name
        binding.tvContestStatus.apply {
            text = getContestStatusText(context, contest.status!!)
            setTextColor(getContestStatusColor(context, contest.status!!))
        }

        val startDate = contest.startDate?.let { contest.startDate }
        val endDate = contest.endDate?.let { contest.endDate }
        // User Registration Info
        val registration = contest.registrations?.find { it.runner.email == email }

        registration?.let {
            val currentDistance = it.races.sumOf { race -> race.distance }
            val contestDistance = contest.distance
            val ratio = (currentDistance / contestDistance!!) * 100

            binding.tvContestDatesRegister.text = DateUtils.convertToVietnameseDate(registration.registrationDate)
            binding.tvCompletionStatus.text = context.getString(R.string.contest_completed)
            binding.processBar.progress = ratio.toInt()
            binding.processBarValue.text = "${formatDistance(currentDistance)}/${formatDistance(contestDistance)}"


            val raceAdapter = RaceStatisticsAdapter(it.races ?: emptyList())
            binding.recyclerViewContests.layoutManager = LinearLayoutManager(context)
            binding.recyclerViewContests.adapter = raceAdapter
        }

        // Close button listener
        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }
}