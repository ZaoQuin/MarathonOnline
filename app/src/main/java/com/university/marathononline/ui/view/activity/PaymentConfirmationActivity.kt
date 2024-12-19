package com.university.marathononline.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.lifecycle.Observer
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.auth.AuthApiService
import com.university.marathononline.data.api.contest.ContestApiService
import com.university.marathononline.data.api.payment.PaymentApiService
import com.university.marathononline.data.api.registration.RegistrationApiService
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.ERegistrationStatus
import com.university.marathononline.data.models.User
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.data.repository.PaymentRepository
import com.university.marathononline.data.repository.RegistrationRepository
import com.university.marathononline.databinding.ActivityPaymentConfirmationBinding
import com.university.marathononline.ui.viewModel.PaymentConfirmationViewModel
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.KEY_CONTEST
import com.university.marathononline.utils.convertToVND
import com.university.marathononline.utils.enable
import com.university.marathononline.utils.finishAndGoBack
import com.university.marathononline.utils.formatDistance
import com.university.marathononline.utils.startNewActivity
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

        viewModel.registerResponse.observe(this){
            binding.btnPayment.enable(false)
            when(it){
                is Resource.Success -> {
                    viewModel.setRegistration(it.value)
                }
                is Resource.Failure -> handleApiError(it)
                else -> Unit
            }
        }

        viewModel.registration.observe(this){

            if(viewModel.registration.value?.status == ERegistrationStatus.PENDING) {
                viewModel.payment()
            }
        }

        viewModel.addPayment.observe(this) {
            when(it){
                is Resource.Success -> {
                    Log.d("PaymentActivity", it.toString())
                    showToastWithDelay("Đăng ký thành công", 2500)
                    viewModel.getContestById()
                }
                is Resource.Failure -> {handleApiError(it)
                    Log.e("PaymentActivity",
                        it.fetchErrorMessage())
                }
                else -> Unit
            }
        }

        viewModel.getContestById.observe(this){
            when(it){
                is Resource.Success -> {
                    startNewActivity(ContestDetailsActivity::class.java, mapOf(KEY_CONTEST to it.value))
                }
                is Resource.Failure -> {handleApiError(it)
                    Log.e("PaymentActivity",
                        it.fetchErrorMessage())
                }
                else -> Unit
            }
        }
    }
    private fun updateUserUI(user: User) {
        binding.apply {
            tvFullName.text = user.fullName
            tvUserGender.text = user.gender.value
            tvUserAddress.text = user.address

            buttonBack.setOnClickListener{
                finishAndGoBack()
            }
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
            tvContestDistance.text = contest.distance?.let { formatDistance(it) }
            tvContestFee.text = contest.fee?.let { convertToVND(it) }
            tvtOrganizerName.text = contest.organizer?.fullName
            tvtOrganizerUsername.text = "@${contest.organizer?.username}"
            tvRegisterDate.text = DateUtils.convertToVietnameseDate(LocalDateTime.now().toString())

            btnPayment.setOnClickListener{
                viewModel.registerContest()
            }

        }
    }

    override fun getViewModel() = PaymentConfirmationViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) = ActivityPaymentConfirmationBinding.inflate(inflater)

    override fun getActivityRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val authApi = retrofitInstance.buildApi(AuthApiService::class.java, token)
        val regApi = retrofitInstance.buildApi(RegistrationApiService::class.java, token)
        val paymentApi = retrofitInstance.buildApi(PaymentApiService::class.java, token)
        val contestApi = retrofitInstance.buildApi(ContestApiService::class.java, token)
        return listOf(
            AuthRepository(authApi, userPreferences),
            RegistrationRepository(regApi),
            PaymentRepository(paymentApi),
            ContestRepository(contestApi)
        )
    }

    private fun showToastWithDelay(message: String, delayMillis: Long) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({
        }, delayMillis)
    }

}