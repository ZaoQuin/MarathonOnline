package com.university.marathononline.ui.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.R.string.*
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.ERegistrationStatus
import com.university.marathononline.data.models.Registration
import com.university.marathononline.databinding.ItemRegistrationBinding
import com.university.marathononline.ui.components.ContestStatisticsDialog
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.formatDistance

class RegistrationAdapter(private var registrations: List<Registration>,
                          private var contest: Contest,
                          private val onBlockRegistration: (Registration) -> Unit ) :
    RecyclerView.Adapter<RegistrationAdapter.ViewHolder>() {

    private var filteredRegistrations = registrations

    class ViewHolder(private val binding: ItemRegistrationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Registration, contest: Contest, onBlockRegistration: (Registration) -> Unit) {
            binding.apply {
                tvFullName.text = item.runner.fullName
                tvUsername.text = itemView.context.getString(registration_username, item.runner.username)
                val totalDistance = item.races.sumOf { it.distance }
                val totalTime = item.races.sumOf { it.timeTaken }
                tvTotalDistance.text =  itemView.context.getString(registration_totalDistance, formatDistance(totalDistance))
                tvTotalTime.text =  itemView.context.getString(registration_totalTime, DateUtils.convertSecondsToHHMMSS(totalTime))
                tvStatus.text = itemView.context.getString(registration_status, item.status.value)

                tvStatus.setTextColor(getStatusColor(item.status))

                statisticsContest.setOnClickListener {
                    val context = it.context
                    val dialog = ContestStatisticsDialog(context,
                        contest,
                        item.runner.email,
                        true,
                        onBlockRegistration = { onBlockRegistration(item) })
                    dialog.show()
                }
            }
        }

        private fun getStatusColor(status: ERegistrationStatus): Int {
            return when (status) {
                ERegistrationStatus.PENDING -> Color.YELLOW
                ERegistrationStatus.ACTIVE -> Color.GREEN
                ERegistrationStatus.COMPLETED -> Color.GRAY
                ERegistrationStatus.BLOCK -> Color.RED
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRegistrationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = filteredRegistrations.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredRegistrations[position], contest, onBlockRegistration)
    }

    // Update data and notify adapter
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newRegistrations: List<Registration>) {
        registrations = newRegistrations
        filteredRegistrations = newRegistrations.filter { it.status != ERegistrationStatus.PENDING }
        contest.registrations = newRegistrations
        notifyDataSetChanged()
    }

    fun getCurrentData(): List<Registration> {
        return registrations
    }
}

