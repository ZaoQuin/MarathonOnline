package com.university.marathononline.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.R
import com.university.marathononline.databinding.ItemContestRunnerBinding
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.EContestStatus
import com.university.marathononline.data.models.ERegistrationStatus
import com.university.marathononline.ui.view.activity.ContestDetailsActivity
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.KEY_CONTEST
import com.university.marathononline.utils.startNewActivity

class ContestRunnerAdapter(private var contests: List<Contest>, private val email: String) : RecyclerView.Adapter<ContestRunnerAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemContestRunnerBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Contest, email: String) {
            binding.apply {
                tvContestName.text = item.name ?: "Không có tên"

                tvContestStatus.apply {
                    text = when (item.status) {
                        EContestStatus.PENDING -> "Chờ duyệt"
                        EContestStatus.ACTIVE -> "Đang hoạt động"
                        EContestStatus.FINISHED -> "Đã kết thúc"
                        EContestStatus.CANCELLED -> "Đã bị hủy"
                        EContestStatus.NOT_APPROVAL -> "Không được duyệt"
                        EContestStatus.DELETED -> "Đã xóa"
                        else -> "Unknown"
                    }

                    setTextColor(
                        when (item.status) {
                            EContestStatus.ACTIVE -> ContextCompat.getColor(context, R.color.main_color)
                            EContestStatus.FINISHED -> ContextCompat.getColor(context, R.color.gray)
                            EContestStatus.CANCELLED -> ContextCompat.getColor(context, R.color.red)
                            else -> ContextCompat.getColor(context, R.color.text_color)
                        }
                    )
                }

                val startDate = item.startDate?.let { DateUtils.convertToVietnameseDate(it) }
                val endDate = item.endDate?.let { DateUtils.convertToVietnameseDate(it) }

                tvContestDatesStart.text = "Ngày bắt đầu: ${startDate ?: "N/A"} "
                tvContestDatesEnd.text = " Ngày kết thúc: ${endDate ?: "N/A"}"

                tvCompletionStatus.apply {
                    val completed = item.registrations?.any { it.runner.email == email && it.status == ERegistrationStatus.COMPLETED } == true
                    text = if (completed) "Đã hoàn thành" else "Chưa hoàn thành"
                    setTextColor(
                        if (completed) ContextCompat.getColor(context, R.color.main_color)
                        else ContextCompat.getColor(context, R.color.red)
                    )
                }

                contestCardView.setOnClickListener {
                    it.context.startNewActivity(
                        ContestDetailsActivity::class.java,
                        mapOf( KEY_CONTEST to item)
                    )
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContestRunnerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Gọi phương thức bind để cập nhật UI
        holder.bind(contests[position], email)
    }

    override fun getItemCount(): Int = contests.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newContest: List<Contest>) {
        contests = newContest
        notifyDataSetChanged()
    }
}
