package com.university.marathononline.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.R
import com.university.marathononline.databinding.ItemContestRunnerBinding
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.EContestStatus
import com.university.marathononline.ui.components.ContestStatisticsDialog
import com.university.marathononline.ui.view.activity.ContestDetailsActivity
import com.university.marathononline.ui.view.activity.PaymentConfirmationActivity
import com.university.marathononline.utils.*

class ContestRunnerAdapter(
    private var contests: List<Contest>,
    private val email: String
) : RecyclerView.Adapter<ContestRunnerAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemContestRunnerBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Contest, email: String) {
            binding.apply {
                val context = itemView.context

                val userRegistration = item.registrations?.find { it.runner.email == email }

                val statusManager = ContestUserStatusManager(item, userRegistration)
                val displayState = statusManager.getDisplayState()
                val progressInfo = statusManager.getProgressInfo()

                tvContestName.text = item.name

                updateContestStatus(item)

                updateContestDates(item)

                updateProgressSection(displayState, progressInfo)

                updateCompletionStatus(displayState)

                updatePaymentButton(displayState, item)

                contestCardView.setOnClickListener {
                    it.context.startNewActivity(
                        ContestDetailsActivity::class.java,
                        mapOf(KEY_CONTEST to item)
                    )
                }

                // Statistics button - only show if leaderboard is available
                statisticsContest.visibility = if (displayState.showLeaderboard) View.VISIBLE else View.GONE
                statisticsContest.setOnClickListener {
                    val dialog = ContestStatisticsDialog(context, item, email, false, null)
                    dialog.show()
                }
            }
        }

        private fun updateContestStatus(contest: Contest) {
            binding.apply {
                val context = itemView.context
                tvContestStatus.text = contest.status.value

                // Set status text and colors
                when (contest.status) {
                    EContestStatus.ACTIVE -> {
                        tvContestStatus.setTextColor(ContextCompat.getColor(context, R.color.success_green))
                        statusIndicator.backgroundTintList = ContextCompat.getColorStateList(context, R.color.success_green)
                    }
                    EContestStatus.FINISHED -> {
                        tvContestStatus.setTextColor(ContextCompat.getColor(context, R.color.warning_orange))
                        statusIndicator.backgroundTintList = ContextCompat.getColorStateList(context, R.color.warning_orange)
                    }
                    EContestStatus.COMPLETED -> {
                        tvContestStatus.setTextColor(ContextCompat.getColor(context, R.color.gray))
                        statusIndicator.backgroundTintList = ContextCompat.getColorStateList(context, R.color.gray)
                    }
                    else -> {
                        tvContestStatus.text = "Không xác định"
                        tvContestStatus.setTextColor(ContextCompat.getColor(context, R.color.disabled_gray))
                        statusIndicator.backgroundTintList = ContextCompat.getColorStateList(context, R.color.disabled_gray)
                    }
                }
            }
        }

        private fun updateContestDates(contest: Contest) {
            binding.apply {
                val context = itemView.context

                val startDate = contest.startDate?.let { DateUtils.convertToVietnameseDate(it) }
                val endDate = contest.endDate?.let { DateUtils.convertToVietnameseDate(it) }

                tvContestDatesStart.text = "Bắt đầu: ${startDate ?: "Chưa xác định"}"
                tvContestDatesEnd.text = "Kết thúc: ${endDate ?: "Chưa xác định"}"
            }
        }

        private fun updateProgressSection(
            displayState: ContestUserStatusManager.ContestDisplayState,
            progressInfo: Triple<Double, Double, Int>
        ) {
            binding.apply {
                if (displayState.showProgress) {
                    progressSection.visibility = View.VISIBLE

                    processBar.progress = progressInfo.third

                    val currentKm = formatDistance(progressInfo.first)
                    val totalKm = formatDistance(progressInfo.second)
                    processBarValue.text = "$currentKm/$totalKm"

                    val progressPercentage = progressInfo.third
                    val textColor = when {
                        progressPercentage >= 100 -> R.color.white
                        progressPercentage >= 75 -> R.color.white
                        progressPercentage >= 50 -> R.color.white
                        else -> R.color.white
                    }
                    processBarValue.setTextColor(ContextCompat.getColor(itemView.context, textColor))

                    val backgroundColor = when {
                        progressPercentage >= 100 -> R.color.success_green
                        progressPercentage >= 75 -> R.color.main_color
                        progressPercentage >= 50 -> R.color.warning_orange
                        else -> R.color.main_color
                    }
                    processBarValue.backgroundTintList = ContextCompat.getColorStateList(itemView.context, backgroundColor)

                } else {
                    progressSection.visibility = View.GONE
                }
            }
        }

        private fun updateCompletionStatus(displayState: ContestUserStatusManager.ContestDisplayState) {
            binding.apply {
                val context = itemView.context

                // Set status icon and text based on user status
                val (statusIcon, statusText, statusColor) = when (displayState.userStatus) {
                    ContestUserStatusManager.UserContestStatus.NOT_REGISTERED ->
                        StatusInfo("❌", "Chưa đăng ký", R.color.disabled_gray)

                    ContestUserStatusManager.UserContestStatus.REGISTERED_UNPAID ->
                        StatusInfo("💳", "Chưa thanh toán", R.color.warning_orange)

                    ContestUserStatusManager.UserContestStatus.PAYMENT_FAILED ->
                        StatusInfo("❌", "Thanh toán thất bại", R.color.error_red)

                    ContestUserStatusManager.UserContestStatus.PAYMENT_PENDING ->
                        StatusInfo("⏳", "Đang xử lý thanh toán", R.color.warning_orange)

                    ContestUserStatusManager.UserContestStatus.REGISTERED_ACTIVE ->
                        StatusInfo("✅", "Đang tham gia", R.color.success_green)

                    ContestUserStatusManager.UserContestStatus.REGISTERED_BLOCKED ->
                        StatusInfo("🚫", "Bị chặn", R.color.error_red)

                    ContestUserStatusManager.UserContestStatus.REGISTRATION_COMPLETED ->
                        StatusInfo("🏆", "Đã hoàn thành", R.color.gold_text)

                    ContestUserStatusManager.UserContestStatus.CONTEST_EXPIRED ->
                        StatusInfo("⏰", "Đã kết thúc", R.color.gray)

                    ContestUserStatusManager.UserContestStatus.REGISTRATION_FULL ->
                        StatusInfo("👥", "Hết slot đăng ký", R.color.error_red)

                    ContestUserStatusManager.UserContestStatus.REGISTRATION_CLOSED ->
                        StatusInfo("🔒", "Hết hạn đăng ký", R.color.gray)
                }

                binding.statusIcon.text = statusIcon
                tvCompletionStatus.text = statusText
                tvCompletionStatus.setTextColor(ContextCompat.getColor(context, statusColor))

                displayState.statusMessage?.let { message ->
                    tvCompletionStatus.text = "$statusText\n$message"
                }
            }
        }

        private fun updatePaymentButton(
            displayState: ContestUserStatusManager.ContestDisplayState,
            contest: Contest
        ) {
            binding.btnPayment.apply {
                val context = itemView.context

                val shouldShowPaymentButton = when (displayState.userStatus) {
                    ContestUserStatusManager.UserContestStatus.REGISTERED_UNPAID,
                    ContestUserStatusManager.UserContestStatus.PAYMENT_FAILED -> true
                    else -> false
                }

                if (shouldShowPaymentButton) {
                    visibility = View.VISIBLE

                    when (displayState.userStatus) {
                        ContestUserStatusManager.UserContestStatus.REGISTERED_UNPAID -> {
                            text = "💳 Thanh toán"
                            backgroundTintList = ContextCompat.getColorStateList(context, R.color.main_color)
                            setTextColor(ContextCompat.getColor(context, R.color.white))
                        }
                        ContestUserStatusManager.UserContestStatus.PAYMENT_FAILED -> {
                            text = "🔄 Thanh toán lại"
                            backgroundTintList = ContextCompat.getColorStateList(context, R.color.error_red)
                            setTextColor(ContextCompat.getColor(context, R.color.white))
                        }
                        else -> {
                            text = "Thanh toán"
                            backgroundTintList = ContextCompat.getColorStateList(context, R.color.main_color)
                            setTextColor(ContextCompat.getColor(context, R.color.white))
                        }
                    }

                    setOnClickListener {
                        handlePaymentClick(contest)
                    }
                } else {
                    visibility = View.GONE
                }
            }
        }

        private fun handlePaymentClick(contest: Contest) {
            binding.root.context.startNewActivity(
                PaymentConfirmationActivity::class.java,
                mapOf(KEY_CONTEST to contest)
            )
        }

        // Data class for status information
        private data class StatusInfo(
            val icon: String,
            val text: String,
            val textColor: Int
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContestRunnerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(contests[position], email)
    }

    override fun getItemCount(): Int = contests.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newContest: List<Contest>) {
        contests = newContest
        notifyDataSetChanged()
    }
}