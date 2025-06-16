package com.university.marathononline.ui.adapter

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.R
import com.university.marathononline.databinding.ItemContestRunnerBinding
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.EContestStatus
import com.university.marathononline.data.models.Registration
import com.university.marathononline.ui.components.ContestStatisticsDialog
import com.university.marathononline.ui.view.activity.ContestDetailsActivity
import com.university.marathononline.ui.view.activity.PaymentConfirmationActivity
import com.university.marathononline.utils.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ContestRunnerAdapter(
    private var contests: List<Contest>,
    private val email: String
) : RecyclerView.Adapter<ContestRunnerAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemContestRunnerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var countDownTimer: CountDownTimer? = null

        fun bind(item: Contest, email: String) {
            countDownTimer?.cancel()

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

                userRegistration?.let { registration ->
                    updatePaymentButton(displayState, item, registration)
                } ?: run {
                    btnPayment.visibility = View.GONE
                    tvCountdown.visibility = View.GONE
                }

                contestCardView.setOnClickListener {
                    it.context.startNewActivity(
                        ContestDetailsActivity::class.java,
                        mapOf(KEY_CONTEST_ID to item.id)
                    )
                }

                statisticsContest.visibility =
                    if (displayState.showLeaderboard) View.VISIBLE else View.GONE
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

                when (contest.status) {
                    EContestStatus.ACTIVE -> {
                        tvContestStatus.setTextColor(
                            ContextCompat.getColor(context, R.color.success_green)
                        )
                        statusIndicator.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.success_green)
                    }

                    EContestStatus.FINISHED -> {
                        tvContestStatus.setTextColor(
                            ContextCompat.getColor(context, R.color.warning_orange)
                        )
                        statusIndicator.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.warning_orange)
                    }

                    EContestStatus.COMPLETED -> {
                        tvContestStatus.setTextColor(ContextCompat.getColor(context, R.color.gray))
                        statusIndicator.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.gray)
                    }

                    else -> {
                        tvContestStatus.text = "Không xác định"
                        tvContestStatus.setTextColor(
                            ContextCompat.getColor(context, R.color.disabled_gray)
                        )
                        statusIndicator.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.disabled_gray)
                    }
                }
            }
        }

        private fun updateContestDates(contest: Contest) {
            binding.apply {
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
                    processBarValue.setTextColor(
                        ContextCompat.getColor(itemView.context, textColor)
                    )

                    val backgroundColor = when {
                        progressPercentage >= 100 -> R.color.success_green
                        progressPercentage >= 75 -> R.color.main_color
                        progressPercentage >= 50 -> R.color.warning_orange
                        else -> R.color.main_color
                    }
                    processBarValue.backgroundTintList =
                        ContextCompat.getColorStateList(itemView.context, backgroundColor)

                } else {
                    progressSection.visibility = View.GONE
                }
            }
        }

        private fun updateCompletionStatus(displayState: ContestUserStatusManager.ContestDisplayState) {
            binding.apply {
                val context = itemView.context

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

                tvCompletionStatus.text = statusText
                tvCompletionStatus.setTextColor(ContextCompat.getColor(context, statusColor))

                displayState.statusMessage?.let { message ->
                    tvCompletionStatus.text = "$statusText\n$message"
                }
            }
        }

        private fun startCountdownTimer(
            deadlineMillis: Long,
            countdownView: TextView,
            paymentButton: Button
        ) {
            val context = itemView.context
            val now = System.currentTimeMillis()
            val remainingMillis = deadlineMillis - now

            Log.d("ContestAdapter", "Starting countdown - Remaining: $remainingMillis ms")

            if (remainingMillis > 0) {
                countdownView.visibility = View.VISIBLE

                countDownTimer = object : CountDownTimer(remainingMillis, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val hours = millisUntilFinished / (1000 * 60 * 60)
                        val minutes = (millisUntilFinished / (1000 * 60)) % 60
                        val seconds = (millisUntilFinished / 1000) % 60

                        val timeStr = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                        countdownView.text = "⏳ Còn lại: $timeStr"

                        val color = when {
                            millisUntilFinished <= 60 * 60 * 1000 -> R.color.error_red
                            millisUntilFinished <= 6 * 60 * 60 * 1000 -> R.color.warning_orange
                            else -> R.color.black
                        }
                        countdownView.setTextColor(ContextCompat.getColor(context, color))

                        Log.d("ContestAdapter", "Countdown tick: $timeStr")
                    }

                    override fun onFinish() {
                        countdownView.text = "⛔ Hết hạn thanh toán"
                        countdownView.setTextColor(ContextCompat.getColor(context, R.color.disabled_gray))

                        paymentButton.visibility = View.GONE

                        Log.d("ContestAdapter", "Countdown finished")
                    }
                }.start()
            } else {
                countdownView.visibility = View.VISIBLE
                countdownView.text = "⛔ Hết hạn thanh toán"
                countdownView.setTextColor(ContextCompat.getColor(context, R.color.disabled_gray))
                paymentButton.visibility = View.GONE

                Log.d("ContestAdapter", "Payment deadline already passed")
            }
        }

        private fun updatePaymentButton(
            displayState: ContestUserStatusManager.ContestDisplayState,
            contest: Contest,
            registration: Registration
        ) {
            binding.apply {
                val context = itemView.context
                val countdownView = tvCountdown
                val paymentButton = btnPayment

                Log.d("ContestAdapter", "UpdatePaymentButton - User status: ${displayState.userStatus}")

                val shouldShowPaymentButton = when (displayState.userStatus) {
                    ContestUserStatusManager.UserContestStatus.REGISTERED_UNPAID,
                    ContestUserStatusManager.UserContestStatus.PAYMENT_FAILED -> true
                    else -> false
                }

                Log.d("ContestAdapter", "Should show payment button: $shouldShowPaymentButton")

                if (shouldShowPaymentButton) {
                    paymentButton.visibility = View.VISIBLE

                    when (displayState.userStatus) {
                        ContestUserStatusManager.UserContestStatus.REGISTERED_UNPAID -> {
                            paymentButton.text = "💳 Thanh toán"
                            paymentButton.backgroundTintList =
                                ContextCompat.getColorStateList(context, R.color.main_color)
                            paymentButton.setTextColor(ContextCompat.getColor(context, R.color.white))
                        }

                        ContestUserStatusManager.UserContestStatus.PAYMENT_FAILED -> {
                            paymentButton.text = "🔄 Thanh toán lại"
                            paymentButton.backgroundTintList =
                                ContextCompat.getColorStateList(context, R.color.error_red)
                            paymentButton.setTextColor(ContextCompat.getColor(context, R.color.white))
                        }

                        else -> {
                            paymentButton.text = "Thanh toán"
                            paymentButton.backgroundTintList =
                                ContextCompat.getColorStateList(context, R.color.main_color)
                            paymentButton.setTextColor(ContextCompat.getColor(context, R.color.white))
                        }
                    }

                    try {
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
                        val createdAt = LocalDateTime.parse(registration.registrationDate, formatter)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime()

                        val contestStartAt = DateUtils.convertStringToLocalDateTime(contest.startDate)
                        val deadline24h = createdAt.plusHours(24)
                        val finalDeadline = if (deadline24h.isBefore(contestStartAt)) deadline24h else contestStartAt
                        val deadlineMillis = finalDeadline.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                        startCountdownTimer(deadlineMillis, countdownView, paymentButton)

                    } catch (e: Exception) {
                        Log.e("ContestAdapter", "Error calculating payment deadline", e)
                        countdownView.visibility = View.VISIBLE
                        countdownView.text = "❌ Lỗi tính toán thời gian"
                        countdownView.setTextColor(ContextCompat.getColor(context, R.color.error_red))
                    }

                    paymentButton.setOnClickListener {
                        handlePaymentClick(contest)
                    }

                } else {
                    paymentButton.visibility = View.GONE
                    countdownView.visibility = View.GONE

                    countDownTimer?.cancel()

                    Log.d("ContestAdapter", "Payment button hidden")
                }
            }
        }

        private fun handlePaymentClick(contest: Contest) {
            binding.root.context.startNewActivity(
                PaymentConfirmationActivity::class.java,
                mapOf(KEY_CONTEST to contest)
            )
        }

        fun cleanup() {
            countDownTimer?.cancel()
            countDownTimer = null
            Log.d("ContestAdapter", "ViewHolder cleanup - CountDownTimer cancelled")
        }

        private data class StatusInfo(
            val icon: String,
            val text: String,
            val textColor: Int
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemContestRunnerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(contests[position], email)
    }

    override fun getItemCount(): Int = contests.size

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.cleanup()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newContest: List<Contest>) {
        contests = newContest
        notifyDataSetChanged()
    }

    fun cleanup() {
        Log.d("ContestAdapter", "Adapter cleanup called")
    }
}