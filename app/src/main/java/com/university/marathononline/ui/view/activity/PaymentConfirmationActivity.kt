package com.university.marathononline.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.Observer
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.auth.AuthApiService
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.User
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.databinding.ActivityPaymentConfirmationBinding
import com.university.marathononline.ui.viewModel.PaymentConfirmationViewModel
import com.university.marathononline.utils.KEY_CONTEST
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime

class PaymentConfirmationActivity : BaseActivity<PaymentConfirmationViewModel, ActivityPaymentConfirmationBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntentExtras(intent)
        viewModel.getUser()
        setUpObserve()
    }

    private fun setUpObserve() {
        viewModel.user.observe(this, Observer {
            when(it){
                is Resource.Success -> updateUserUI(it.value)
                is Resource.Failure -> handleApiError(it)
                else -> Unit
            }
        })
    }

    private fun updateUserUI(user: User) {
        binding.apply {
            tvFullName.text = user.fullName
            tvUserGender.text = user.gender.value
            tvUserAddress.text = user.address
        }
    }

    private fun handleIntentExtras(intent: Intent) {
        intent.apply {
            viewModel.apply {
                (getSerializableExtra(KEY_CONTEST) as? Contest)?.let {
                    setContest(it)
                    updateContestUI(it)
                }
            }
        }
    }

    private fun updateContestUI(contest: Contest) {
        binding.apply {
            tvContestName.text = contest.name
            tvContestDistance.text = contest.distance.toString()
            tvContestFee.text = contest.fee.toString()
            tvtOrganizerName.text = contest.organizer?.fullName
            tvtOrganizerUsername.text = contest.organizer?.username
            tvRegisterDate.text = LocalDateTime.now().toString()
        }
    }

    override fun getViewModel() = PaymentConfirmationViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) = ActivityPaymentConfirmationBinding.inflate(inflater)

    override fun getActivityRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val api = retrofitInstance.buildApi(AuthApiService::class.java, token)
        return listOf(
            AuthRepository(api, userPreferences)
        )
    }
}