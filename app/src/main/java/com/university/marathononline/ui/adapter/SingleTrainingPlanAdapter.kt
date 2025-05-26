package com.university.marathononline.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.data.models.ETrainingPlanStatus
import com.university.marathononline.data.models.SingleTrainingPlan
import com.university.marathononline.databinding.ItemTrainingPlanBinding
import com.university.marathononline.ui.view.activity.TrainingPlanDetailsActivity
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.KEY_TRAINING_PLAN_ID
import com.university.marathononline.utils.startNewActivity

class SingleTrainingPlanAdapter(private var trainingPlans: List<SingleTrainingPlan>) :
    RecyclerView.Adapter<SingleTrainingPlanAdapter.ViewHolder>() {
    class ViewHolder(private val binding: ItemTrainingPlanBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SingleTrainingPlan) {
            binding.apply {
                itemName.text = item.name

                val planStartDate = item.startDate?.let { DateUtils.convertToVietnameseDate(it) }
                val planEndDate = item.endDate?.let { DateUtils.convertToVietnameseDate(it) }
                itemTime.text = "$planStartDate - $planEndDate"

                if (item.status == ETrainingPlanStatus.COMPLETED){
                    btnViewDetails.setOnClickListener {
                        it.context.startNewActivity(
                            TrainingPlanDetailsActivity::class.java,
                            mapOf(KEY_TRAINING_PLAN_ID to item.id)
                        )
                    }
                } else {
                    btnViewDetails.visibility = View.GONE
                }
                // Giả sử bạn có thể thêm các trường completedDays, remainingDays, progress trong SingleTrainingPlan,
                // Nếu chưa có, cần thêm vào data model hoặc tính toán ở ViewModel
                // Giả sử có 2 biến completedDays và remainingDays kiểu Int
//                completedDays.text = item.completedDays?.toString() ?: "0"
//                remainingDays.text = item.remainingDays?.toString() ?: "0"

                // Cập nhật thanh progress (0-100)
//                progressBar.progress = item.progressPercent ?: 0
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding =
            ItemTrainingPlanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(trainingPlans[position])
    }

    override fun getItemCount() = trainingPlans.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<SingleTrainingPlan>) {
        trainingPlans = newData
        notifyDataSetChanged()
    }

    fun addData(additionalTrainingPlans: List<SingleTrainingPlan>) {
        val oldSize = trainingPlans.size
        val tempList = trainingPlans.toMutableList()
        tempList.addAll(additionalTrainingPlans)
        trainingPlans = tempList
        notifyItemRangeInserted(oldSize, additionalTrainingPlans.size)
    }
}