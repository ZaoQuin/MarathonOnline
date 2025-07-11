package com.university.marathononline.ui.components

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.university.marathononline.utils.getTotalDistance
import com.university.marathononline.utils.visible

class ContestStatisticsDialog(
    context: Context,
    private val contest: Contest,
    private val email: String,
    private val isManager: Boolean,
    private val onBlockRegistration: ((Registration) -> Unit)? = null
) : Dialog(context) {

    private lateinit var binding: DialogContestStatisticsBinding
    private var currentRegistration: Registration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DialogContestStatisticsBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        setupDialog()
        setupContent()
        setupButtons()
    }

    private fun setupDialog() {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCancelable(true)
        setCanceledOnTouchOutside(true)
    }

    private fun setupContent() {
        binding.tvDialogTitle?.text = "Thống Kê Cuộc Thi"

        binding.tvContestName.text = contest.name
        binding.tvContestStatus.apply {
            text = contest.status?.value ?: "Không rõ"
            contest.status?.let {
                setTextColor(getContestStatusColor(context, it))
            }
        }

        currentRegistration = contest.registrations?.find { it.runner.email == email }

        currentRegistration?.let { registration ->
            setupRegistrationDetails(registration)
            setupRecordsList(registration)
            setupManagerActions(registration)
        } ?: run {
            setupNotRegisteredState()
        }
    }

    private fun setupRegistrationDetails(registration: Registration) {
        val currentDistance = registration.getTotalDistance()
        val contestDistance = contest.distance ?: 0.0
        val ratio = if (contestDistance > 0) (currentDistance / contestDistance) * 100 else 0.0

        binding.tvContestDatesRegister.text = context.getString(
            R.string.registration_register_date,
            DateUtils.convertToVietnameseDate(registration.registrationDate)
        )

        binding.tvCompletionStatus.text = context.getString(
            R.string.registration_status,
            registration.status.value
        )

        binding.processBar.progress = ratio.toInt()
        binding.processBarValue.text = "${formatDistance(currentDistance)}/${formatDistance(contestDistance)}"

        binding.tvTotalRecords?.text = "Tổng số bản ghi: ${registration.records.size}"
        binding.tvCompletionPercentage?.text = "Hoàn thành: ${String.format("%.1f", ratio)}%"

        setupApprovalStatistics(registration.records)
    }

    private fun setupApprovalStatistics(recordList: List<com.university.marathononline.data.models.Record>) {
        val approvedCount = recordList.count { it.approval?.approvalStatus == com.university.marathononline.data.models.ERecordApprovalStatus.APPROVED }
        val pendingCount = recordList.count { it.approval?.approvalStatus == com.university.marathononline.data.models.ERecordApprovalStatus.PENDING }
        val rejectedCount = recordList.count { it.approval?.approvalStatus == com.university.marathononline.data.models.ERecordApprovalStatus.REJECTED }
        val noApprovalCount = recordList.count { it.approval == null }

        binding.tvApprovalStatistics?.apply {
            text = "Trạng thái duyệt: Đã duyệt($approvedCount), Chờ duyệt($pendingCount), Từ chối($rejectedCount), Chưa xét($noApprovalCount)"
            visibility = View.VISIBLE
        }
    }

    private fun setupRecordsList(registration: Registration) {
        val recordList = registration.records ?: emptyList()
        val recordAdapter = RecordStatisticsAdapter(recordList)

        binding.recyclerViewContests.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recordAdapter
            // Add some styling similar to the feedback dialog
            visibility = if (recordList.isNotEmpty()) View.VISIBLE else View.GONE
        }

        // Show empty state message if no records
        if (recordList.isEmpty()) {
            binding.tvEmptyRecords?.apply {
                text = "Chưa có bản ghi nào"
                visibility = View.VISIBLE
            }
        }
    }

    private fun setupManagerActions(registration: Registration) {
        if (isManager) {
            binding.reportButton.apply {
                visible(true)
                setOnClickListener {
                    showBlockConfirmationDialog(registration)
                }
            }
        } else {
            binding.reportButton.visibility = View.GONE
        }
    }

    private fun setupNotRegisteredState() {
        binding.tvCompletionStatus.text = "Chưa đăng ký tham gia"
        binding.processBar.progress = 0
        binding.processBarValue.text = "0/0"
        binding.recyclerViewContests.visibility = View.GONE
        binding.tvEmptyRecords?.apply {
            text = "Bạn chưa đăng ký tham gia cuộc thi này"
            visibility = View.VISIBLE
        }
        binding.reportButton.visibility = View.GONE
        binding.tvApprovalStatistics?.visibility = View.GONE
    }

    private fun setupButtons() {
        // Close button styling similar to TrainingFeedbackDialog
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        // If it's view-only mode (similar to TrainingFeedbackDialog),
        // we can adjust button styling
        if (!isManager) {
            binding.btnClose.apply {
                text = "Đóng"
                layoutParams = layoutParams.apply {
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                }
            }
        }
    }

    private fun showBlockConfirmationDialog(registration: Registration) {
        val builder = AlertDialog.Builder(context, R.style.AlertDialogTheme)
        builder.setTitle("Chặn Người Dùng")
        builder.setMessage("Bạn có chắc chắn muốn chặn người dùng ${registration.runner.fullName} không?")

        builder.setPositiveButton("Xác Nhận") { dialog, _ ->
            onBlockRegistration?.invoke(registration)
            dialog.dismiss()
            dismiss()
        }

        builder.setNegativeButton("Hủy Bỏ") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = builder.create()

        // Apply same styling as main dialog
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
    }
}