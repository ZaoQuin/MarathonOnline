package com.university.marathononline.ui.view.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
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

class PaymentConfirmationActivity: BaseActivity<PaymentConfirmationViewModel, ActivityPaymentConfirmationBinding>() {

    private val VNPAY_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntentExtras(intent)
        viewModel.getUser()
        setUpObserve()
    }

    private fun setUpObserve() {
        viewModel.user.observe(this) {
            when(it){
                is Resource.Success -> updateUserUI(it.value)
                is Resource.Failure -> handleApiError(it)
                else -> Unit
            }
        }

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
                viewModel.createVNPayPayment()
            }
        }

        viewModel.addPayment.observe(this) {
            when(it){
                is Resource.Success -> {
                    Log.d("PaymentActivity", it.toString())
                    Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show()

                    val resultIntent = Intent().apply {
                        putExtra("payment_success", true)
                        putExtra("registration_status", viewModel.registration.value?.status?.name)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
                is Resource.Failure -> {handleApiError(it)
                    Log.e("PaymentActivity",
                        it.fetchErrorMessage())

                    val resultIntent = Intent().apply {
                        putExtra("payment_success", false)
                        putExtra("error_message", it.fetchErrorMessage())
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
                else -> Unit
            }
        }

        viewModel.processVNPayResult.observe(this) {
            when(it) {
                is Resource.Success -> {
                    Log.d("PaymentActivity", "Payment processed successfully: ${it.value}")
                    viewModel.addPayment(it.value)
                }
                is Resource.Failure -> {
                    handleApiError(it)
                    binding.btnPayment.enable(true)
                    Log.e("PaymentActivity", "Payment processing failed: ${it.fetchErrorMessage()}")

                    val resultIntent = Intent().apply {
                        putExtra("payment_success", false)
                        putExtra("error_message", "Thanh toán thất bại: ${it.fetchErrorMessage()}")
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
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

        viewModel.vnpayPaymentUrl.observe(this) {
            when(it) {
                is Resource.Success -> {
                    Log.d("VNPay", "Payment URL: ${it.value}")
                    openVNPayPayment(it.value.str)
                }
                is Resource.Failure -> {
                    handleApiError(it)
                    binding.btnPayment.enable(true)
                    Log.e("VNPay", "Error creating payment URL: ${it.fetchErrorMessage()}")
                }
                else -> Unit
            }
        }

    }

    private fun openVNPayPayment(paymentUrl: String) {
        try {
            VNPayWebViewActivity.startForResult(this, paymentUrl, VNPAY_REQUEST_CODE)

        } catch (e: Exception) {
            Log.e("VNPay", "Error opening payment", e)
            Toast.makeText(this, "Lỗi mở trang thanh toán", Toast.LENGTH_SHORT).show()
            binding.btnPayment.enable(true)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == VNPAY_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    val paymentResultUrl = data?.getStringExtra("payment_result_url")
                    paymentResultUrl?.let { url ->
                        handleVNPayReturn(url)
                    }
                }
                RESULT_CANCELED -> {
                    Log.d("VNPay", "Payment cancelled by user")
                    Toast.makeText(this, "Thanh toán đã bị hủy", Toast.LENGTH_SHORT).show()

                    val resultIntent = Intent().apply {
                        putExtra("payment_success", false)
                        putExtra("error_message", "Giao dịch đã bị hủy")
                    }
                    setResult(RESULT_CANCELED, resultIntent)
                    finish()
                }
                else -> {
                    Log.e("VNPay", "Payment failed with result code: $resultCode")
                    Toast.makeText(this, "Thanh toán thất bại", Toast.LENGTH_SHORT).show()

                    val resultIntent = Intent().apply {
                        putExtra("payment_success", false)
                        putExtra("error_message", "Thanh toán thất bại.")
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
            }
        }
    }

    private fun handleVNPayReturn(url: String) {
        try {
            Log.d("VNPay", "Processing return URL: $url")

            val uri = Uri.parse(url)
            val params = mutableMapOf<String, String>()

            uri.queryParameterNames.forEach { paramName ->
                uri.getQueryParameter(paramName)?.let { paramValue ->
                    params[paramName] = paramValue
                }
            }

            Log.d("VNPay", "Payment parameters: $params")

            val responseCode = params["vnp_ResponseCode"]
            when (responseCode) {
                "00" -> {
                    Log.d("VNPay", "Payment successful")
                    viewModel.processVNPayReturn(params)
                }
                "24" -> {
                    // Giao dịch bị hủy
                    Log.d("VNPay", "Payment cancelled")
                    Toast.makeText(this, "Giao dịch đã bị hủy", Toast.LENGTH_SHORT).show()
                    binding.btnPayment.enable(true)
                }
                else -> {
                    // Thanh toán thất bại
                    Log.e("VNPay", "Payment failed with code: $responseCode")
                    Toast.makeText(this, "Thanh toán thất bại. Mã lỗi: $responseCode", Toast.LENGTH_SHORT).show()
                    binding.btnPayment.enable(true)
                }
            }

        } catch (e: Exception) {
            Log.e("VNPay", "Error processing payment return", e)
            Toast.makeText(this, "Lỗi xử lý kết quả thanh toán", Toast.LENGTH_SHORT).show()
            binding.btnPayment.enable(true)
        }
    }

    private fun updateUserUI(user: User) {
        binding.apply {
            tvFullName.text = user.fullName
            tvUserGender.text = user.gender!!.value
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
                btnPayment.enable(false)
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

    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        super.onBackPressed()
    }
}