package com.university.marathononline.ui.components

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.university.marathononline.R
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.Registration
import com.university.marathononline.databinding.DialogContestStatisticsBinding
import com.university.marathononline.ui.adapter.RecordStatisticsAdapter
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.formatDistance
import com.university.marathononline.utils.getContestStatusColor
import com.university.marathononline.utils.visible

class ContestStatisticsDialog(
    context: Context,
    private val contest: Contest,
    private val email: String,
    private val isManager: Boolean,
    private val onBlockRegistration: ((Registration) -> Unit)?
) : Dialog(context) {

    private lateinit var binding: DialogContestStatisticsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogContestStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val context = context

        binding.tvContestName.text = contest.name
        binding.tvContestStatus.apply {
            text = contest.status?.value
            setTextColor(getContestStatusColor(context, contest.status!!))
        }

        val registration = contest.registrations?.find { it.runner.email == email }
        registration?.let {
            val currentDistance = it.records.sumOf { record -> record.distance }
            val contestDistance = contest.distance
            val ratio = (currentDistance / contestDistance!!) * 100

            binding.tvContestDatesRegister.text =context.getString(R.string.registration_register_date,  DateUtils.convertToVietnameseDate(registration.registrationDate))
            binding.tvCompletionStatus.text = context.getString(R.string.registration_status, registration.status.value)
            binding.processBar.progress = ratio.toInt()
            binding.processBarValue.text = "${formatDistance(currentDistance)}/${formatDistance(contestDistance)}"

            val recordAdapter = RecordStatisticsAdapter(it.records ?: emptyList())
            binding.recyclerViewContests.layoutManager = LinearLayoutManager(context)
            binding.recyclerViewContests.adapter = recordAdapter

            if(isManager){
                binding.reportButton.visible(true)
                binding.reportButton.setOnClickListener {
                    showBlockConfirmationDialog(context, registration)
                }
            }
        }

        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    private fun showBlockConfirmationDialog(context: Context, item: Registration){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Chặn người dùng")
        builder.setMessage("Bạn có chắc chắn muốn chặn người dùng này không?")
        builder.setPositiveButton("Xác nhận") { dialog, _ ->
            onBlockRegistration?.let { it(item) }
            dialog.dismiss()
            dismiss()
        }
        builder.setNegativeButton("Trở lại") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }
}