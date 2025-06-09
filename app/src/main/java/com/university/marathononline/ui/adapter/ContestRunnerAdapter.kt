package com.university.marathononline.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.R
import com.university.marathononline.R.string.*
import com.university.marathononline.databinding.ItemContestRunnerBinding
import com.university.marathononline.data.models.Contest
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

                // Find user registration
                val userRegistration = item.registrations?.find { it.runner.email == email }

                // Create status manager
                val statusManager = ContestUserStatusManager(item, userRegistration)
                val displayState = statusManager.getDisplayState()
                val progressInfo = statusManager.getProgressInfo()

                // Set contest name
                tvContestName.text = item.name

                // Set contest status with color
                tvContestStatus.apply {
                    text = item.status?.value ?: "Không xác định"
                    setTextColor(getContestStatusColor(context, item.status))
                }

                // Set dates
                val startDate = item.startDate?.let { DateUtils.convertToVietnameseDate(it) }
                val endDate = item.endDate?.let { DateUtils.convertToVietnameseDate(it) }

                tvContestDatesStart.text = context.getString(
                    contest_start_date,
                    startDate ?: context.getString(date_not_available)
                )
                tvContestDatesEnd.text = context.getString(
                    contest_end_date,
                    endDate ?: context.getString(date_not_available)
                )

                // Update progress section visibility and data
                updateProgressSection(displayState, progressInfo)

                // Update completion status
                updateCompletionStatus(displayState)

                // Update payment button
                updatePaymentButton(displayState, item)

                // Set click listeners
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

        private fun updateProgressSection(
            displayState: ContestUserStatusManager.ContestDisplayState,
            progressInfo: Triple<Double, Double, Int>
        ) {
            binding.apply {
                if (displayState.showProgress) {
                    // Show progress section
                    processBar.visibility = View.VISIBLE
                    processBarValue.visibility = View.VISIBLE

                    // Update progress bar
                    processBar.progress = progressInfo.third
                    processBarValue.text = "${formatDistance(progressInfo.first)}/${formatDistance(progressInfo.second)}"
                } else {
                    // Hide progress section
                    processBar.visibility = View.GONE
                    processBarValue.visibility = View.GONE
                }
            }
        }

        private fun updateCompletionStatus(displayState: ContestUserStatusManager.ContestDisplayState) {
            binding.tvCompletionStatus.apply {
                // Set status text
                text = when (displayState.userStatus) {
                    ContestUserStatusManager.UserContestStatus.NOT_REGISTERED -> "Trạng thái: Chưa đăng ký"
                    ContestUserStatusManager.UserContestStatus.REGISTERED_UNPAID -> "Trạng thái: Chưa thanh toán"
                    ContestUserStatusManager.UserContestStatus.PAYMENT_FAILED -> "Trạng thái: Thanh toán thất bại"
                    ContestUserStatusManager.UserContestStatus.PAYMENT_PENDING -> "Trạng thái: Đang xử lý thanh toán"
                    ContestUserStatusManager.UserContestStatus.REGISTERED_ACTIVE -> "Trạng thái: Đang tham gia"
                    ContestUserStatusManager.UserContestStatus.REGISTERED_BLOCKED -> "Trạng thái: Bị chặn"
                    ContestUserStatusManager.UserContestStatus.REGISTRATION_COMPLETED -> "Trạng thái: Đã hoàn thành"
                    ContestUserStatusManager.UserContestStatus.CONTEST_EXPIRED -> "Trạng thái: Đã kết thúc"
                    ContestUserStatusManager.UserContestStatus.REGISTRATION_FULL -> "Trạng thái: Hết slot đăng ký"
                    ContestUserStatusManager.UserContestStatus.REGISTRATION_CLOSED -> "Trạng thái: Hết hạn đăng ký"
                    ContestUserStatusManager.UserContestStatus.CONTEST_NOT_AVAILABLE -> "Trạng thái: thái không khả dụng"

                }

                // Set status color based on status
                val statusColor = when (displayState.userStatus) {
                    ContestUserStatusManager.UserContestStatus.REGISTERED_ACTIVE -> R.color.light_main_color
                    ContestUserStatusManager.UserContestStatus.REGISTRATION_COMPLETED -> R.color.main_color
                    ContestUserStatusManager.UserContestStatus.REGISTERED_BLOCKED,
                    ContestUserStatusManager.UserContestStatus.PAYMENT_FAILED -> R.color.red
                    ContestUserStatusManager.UserContestStatus.PAYMENT_PENDING -> R.color.partial_complete_color
                    ContestUserStatusManager.UserContestStatus.REGISTERED_UNPAID -> R.color.gray
                    ContestUserStatusManager.UserContestStatus.CONTEST_EXPIRED,
                    ContestUserStatusManager.UserContestStatus.REGISTRATION_CLOSED,
                    ContestUserStatusManager.UserContestStatus.REGISTRATION_FULL -> R.color.gray
                    else -> R.color.disabled_gray
                }

                setTextColor(ContextCompat.getColor(context, statusColor))

                // Show additional status message if available
                displayState.statusMessage?.let { message ->
                    // You might want to show this in a separate TextView or as a tooltip
                    // For now, we'll append it to the existing status
                    text = "$text\n$message"
                }
            }
        }

        private fun updatePaymentButton(
            displayState: ContestUserStatusManager.ContestDisplayState,
            contest: Contest
        ) {
            binding.btnPayment.apply {
                // Show payment button only for specific unpaid statuses
                val shouldShowPaymentButton = when (displayState.userStatus) {
                    ContestUserStatusManager.UserContestStatus.REGISTERED_UNPAID,
                    ContestUserStatusManager.UserContestStatus.PAYMENT_FAILED -> true
                    else -> false
                }

                if (shouldShowPaymentButton) {
                    visibility = View.VISIBLE
                    text = when (displayState.userStatus) {
                        ContestUserStatusManager.UserContestStatus.REGISTERED_UNPAID -> "Thanh toán"
                        ContestUserStatusManager.UserContestStatus.PAYMENT_FAILED -> "Thanh toán lại"
                        else -> "Thanh toán"
                    }

                    // Set button appearance based on status
                    val (backgroundColor, textColor) = when (displayState.userStatus) {
                        ContestUserStatusManager.UserContestStatus.PAYMENT_FAILED -> {
                            Pair(R.color.red, R.color.white)
                        }
                        else -> {
                            Pair(R.color.main_color, R.color.white)
                        }
                    }

                    backgroundTintList = ContextCompat.getColorStateList(context, backgroundColor)
                    setTextColor(ContextCompat.getColor(context, textColor))

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