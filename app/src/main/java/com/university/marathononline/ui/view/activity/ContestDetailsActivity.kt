package com.university.marathononline.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.tabs.TabLayout
import com.university.marathononline.R
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.contest.ContestApiService
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.Registration
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.ui.viewModel.ContestDetailsViewModel
import com.university.marathononline.databinding.ActivityContestDetailsBinding
import com.university.marathononline.ui.adapter.RewardAdapter
import com.university.marathononline.ui.adapter.RuleAdapter
import com.university.marathononline.ui.view.fragment.LeaderBoardFragment
import com.university.marathononline.utils.ContestUserStatusManager
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.KEY_CONTEST
import com.university.marathononline.utils.KEY_CONTEST_ID
import com.university.marathononline.utils.KEY_REGISTRATIONS
import com.university.marathononline.utils.convertToVND
import com.university.marathononline.utils.enable
import com.university.marathononline.utils.formatDistance
import com.university.marathononline.utils.startNewActivity
import com.university.marathononline.utils.visible
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.Serializable

class ContestDetailsActivity :
    BaseActivity<ContestDetailsViewModel, ActivityContestDetailsBinding>() {

    companion object {
        const val PAYMENT_REQUEST_CODE = 1001
        private const val REFRESH_DELAY_MS = 1000L
    }

    private lateinit var ruleAdapter: RuleAdapter
    private lateinit var rewardAdapter: RewardAdapter
    private val handler = Handler(Looper.getMainLooper())
    private var isTabSelected = false
    private var statusManager: ContestUserStatusManager? = null
    private var leaderBoardFragment: LeaderBoardFragment? = null
    private var isDataRefreshing = false
    private var shimmerContainer: ShimmerFrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            initializeShimmer()
            handleIntentExtras(intent)
            initializeUI()
            setRuleAdapter()
            setRewardAdapter()
            setUpLeaderBoard(savedInstanceState)
            setUpObserve()
        } catch (e: Exception) {
            Log.e("ContestDetailsActivity", "Error in onCreate: ${e.message}", e)
            // Handle the error gracefully - maybe show error message and finish
            Toast.makeText(this, "Error loading contest details", Toast.LENGTH_LONG).show()
            finish()
        }
    }


    private fun initializeShimmer() {
        shimmerContainer = binding.shimmerContainer
        showShimmer()
    }

    private fun showShimmer() {
        shimmerContainer?.let { shimmer ->
            shimmer.visible(true)
            shimmer.startShimmer()
            binding.apply{
                binding.mainContent.visible(false)
                binding.registerContainer.visible(false)
                binding.recordContainer.visible(false)
            }
        }
    }

    private fun hideShimmer() {
        shimmerContainer?.let { shimmer ->
            shimmer.stopShimmer()
            shimmer.visible(false)

            binding.apply{
                binding.mainContent.visible(true)
                binding.registerContainer.visible(true)
                binding.recordContainer.visible(true)
            }
        }
    }

    private fun setUpLeaderBoard(savedInstanceState: Bundle?) {
        val registrations = viewModel.contest.value?.registrations
        leaderBoardFragment = LeaderBoardFragment().apply {
            arguments = Bundle().apply {
                val registrationsList = when {
                    registrations != null -> ArrayList(registrations)
                    else -> ArrayList<Registration>()
                }
                putSerializable(KEY_REGISTRATIONS, registrationsList)
            }
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.mainContent.id, leaderBoardFragment!!)
                .commit()
        }
    }

    private fun setUpObserve() {
        viewModel.rules.observe(this) {
            ruleAdapter.updateData(it)
        }

        viewModel.rewardGroup.observe(this) {
            rewardAdapter.updateData(it)
        }

        viewModel.deadlineTime.observe(this) {
            viewModel.startCountdown()
        }

        viewModel.remainingTime.observe(this) { time ->
            updateCountdownDisplay(time)
        }

        viewModel.contest.observe(this) { contest ->
            if (contest == null) {
                Log.e("ContestDetailsActivity", "Contest is null")
                return@observe
            }

            lifecycleScope.launch {
                val emailValue = userPreferences.email.first()
                emailValue?.let { email ->
                    viewModel.resetRegistrationState()
                    viewModel.checkRegister(email)
                    updateStatusManager(contest, null)

                    val safeRegistrations = contest.registrations ?: emptyList()
                    updateLeaderBoardFragment(safeRegistrations)

                    populateBasicInfo(contest)
                    hideShimmer()
                }
            }
        }

        viewModel.registration.observe(this) { registration ->
            Log.d("ContestDetailsActivity", "Registration updated: ${registration?.status}")
            viewModel.contest.value?.let { contest ->
                updateStatusManager(contest, registration)
            }
            updateUIBasedOnStatus()
        }

        viewModel.refreshContest.observe(this) { resource ->
            when(resource) {
                is Resource.Loading -> {
                    // Hiển thị loading indicator nếu cần
                    Log.d("ContestDetailsActivity", "Refreshing contest data...")
                }
                is Resource.Success -> {
                    Log.d("ContestDetailsActivity", "Contest data refreshed successfully")
                    isDataRefreshing = false

                    // Cập nhật toàn bộ data
                    viewModel.setContest(resource.value)
                    viewModel.setRewardGroups(resource.value.rewards)
                    viewModel.setRules(resource.value.rules)
                    viewModel.setDeadlineTime(resource.value.registrationDeadline)

                    // Force refresh registration status với data mới
                    refreshUserRegistrationStatus()
                }
                is Resource.Failure -> {
                    isDataRefreshing = false
                    handleApiError(resource)
                    Log.e("ContestDetailsActivity", "Failed to refresh contest data")
                }
                else -> Unit
            }
        }

        // Deprecated observers - giữ lại để debug
        viewModel.isRegistered.observe(this) {
            Log.d("ContestDetailsActivity", "isRegistered: $it")
        }

        viewModel.isBlocked.observe(this) {
            Log.d("ContestDetailsActivity", "isBlocked: $it")
        }
    }

    private fun updateLeaderBoardFragment(registrations: List<Registration>?) {
        leaderBoardFragment?.let { fragment ->
            val newBundle = Bundle().apply {
                if (registrations != null) {
                    putSerializable(KEY_REGISTRATIONS, registrations as Serializable)
                } else {
                    putSerializable(KEY_REGISTRATIONS, ArrayList<Registration>() as Serializable)
                }
            }
            fragment.arguments = newBundle

            if (fragment.isAdded) {
                fragment.updateRegistrations(registrations ?: emptyList())
            }
        }
    }

    private fun updateStatusManager(contest: Contest, registration: Registration?) {
        statusManager = ContestUserStatusManager(contest, registration)
        updateUIBasedOnStatus()
    }

    private fun updateUIBasedOnStatus() {
        statusManager?.let { manager ->
            val displayState = manager.getDisplayState()

            updateContainerVisibility(displayState)

            updateButtonStates(displayState)

            if (displayState.showProgress) {
                updateProgressDisplay(manager)
            }

            updateLeaderboardVisibility(displayState.showLeaderboard)

            displayState.statusMessage?.let { message ->
                showStatusMessage(message)
            }

            Log.d("ContestDetailsActivity", "UI updated - User Contest Status: ${displayState.userStatus}")
        }
    }

    private fun updateContainerVisibility(displayState: ContestUserStatusManager.ContestDisplayState) {
        binding.registerContainer.visible(!displayState.showProgress)
        binding.recordContainer.visible(displayState.showProgress)
    }

    private fun updateButtonStates(displayState: ContestUserStatusManager.ContestDisplayState) {
        binding.apply {
            btnRegisterContest.text = displayState.registerButtonText
            btnRegisterContest.enable(displayState.registerButtonEnabled)

            btnRecord.text = displayState.recordButtonText
            btnRecord.enable(displayState.recordButtonEnabled)
        }
    }

    private fun updateProgressDisplay(manager: ContestUserStatusManager) {
        val (currentDistance, contestDistance, ratio) = manager.getProgressInfo()

        binding.apply {
            processBar.progress = ratio
            processBarValue.text = "${formatDistance(currentDistance)}/${formatDistance(contestDistance)}"
        }
    }

    private fun updateLeaderboardVisibility(shouldShow: Boolean) {
        binding.sectionLeaderBoard.visible(shouldShow)
    }

    private fun showStatusMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.i("ContestDetailsActivity", "Status Message: $message")
    }

    private fun updateCountdownDisplay(time: String) {
        val timeParts = time.split(":")
        binding.apply {
            daysTextView.text = timeParts[0]
            hoursTextView.text = timeParts[1]
            minutesTextView.text = timeParts[2]
            secondsTextView.text = timeParts[3]
        }
    }

    private fun setRewardAdapter() {
        binding.rewards.layoutManager = LinearLayoutManager(this)
        rewardAdapter = RewardAdapter(emptyList())
        binding.rewards.adapter = rewardAdapter
    }

    private fun setRuleAdapter() {
        binding.rules.layoutManager = LinearLayoutManager(this)
        ruleAdapter = RuleAdapter(emptyList())
        binding.rules.adapter = ruleAdapter
    }

    private fun initializeUI() {
        setUpData()
        setUpTabLayout()
        setUpScrollView()
        setUpBackButton()
        setUpButtonListeners()
    }

    private fun setUpButtonListeners() {
        binding.apply {
            btnRegisterContest.setOnClickListener {
                handleRegisterButtonClick()
            }

            btnRecord.setOnClickListener {
                handleRecordButtonClick()
            }
        }
    }

    private fun handleRegisterButtonClick() {
        statusManager?.let { manager ->
            val displayState = manager.getDisplayState()

            when (displayState.userStatus) {
                ContestUserStatusManager.UserContestStatus.NOT_REGISTERED,
                ContestUserStatusManager.UserContestStatus.PAYMENT_FAILED,
                ContestUserStatusManager.UserContestStatus.REGISTERED_UNPAID,
                ContestUserStatusManager.UserContestStatus.PAYMENT_PENDING -> {
                    navigateToPayment()
                }
                else -> {
                    Log.w("ContestDetailsActivity", "Register button clicked but action not allowed")
                }
            }
        }
    }

    private fun navigateToPayment(){
        viewModel.contest.value?.let { contest ->
            val intent = Intent(this, PaymentConfirmationActivity::class.java).apply {
                putExtra(KEY_CONTEST, contest)
            }
            startActivityForResult(intent, PAYMENT_REQUEST_CODE)
        }
    }

    private fun handleRecordButtonClick() {
        statusManager?.let { manager ->
            if (manager.canPerformAction(ContestUserStatusManager.ContestAction.RECORD)) {
                startNewActivity(RecordActivity::class.java)
            } else {
                Log.w("ContestDetailsActivity", "Record button clicked but action not allowed")
            }
        }
    }

    private fun setUpData() {
        viewModel.contest.value?.let { contest ->
            populateBasicInfo(contest)
        }
    }

    private fun populateBasicInfo(contest: Contest) {
        binding.apply {
            tvDistance.text = contest.distance?.let { formatDistance(it) }
            tvFee.text = contest.fee?.let { convertToVND(it) }
            tvMaxMembers.text = if (contest.maxMembers == 0)
                "Không giới hạn người tham gia"
            else
                contest.maxMembers.toString()
            contestName.text = contest.name
            if (contest.imgUrl.isNullOrEmpty()) {
                contestImg.setImageResource(R.drawable.example_event)
            } else {
                Glide.with(root.context)
                    .load(contest.imgUrl)
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.example_event)
                    .into(contestImg)
            }
            startDate.text = contest.startDate?.let { DateUtils.convertToVietnameseDate(it) }
            endDate.text = contest.endDate?.let { DateUtils.convertToVietnameseDate(it) }
            contentDetails.text = contest.description
            feeRegister.text = contest.fee?.let { convertToVND(it) }
            organizationalName.text = contest.organizer?.fullName
            emailOrganizer.text = contest.organizer?.email

            val totalRegistration = contest.registrations?.size ?: 0
            maxMembers.text = if (contest.maxMembers == 0)
                "Không giới hạn người tham gia"
            else
                "$totalRegistration/ ${contest.maxMembers}"
        }
    }

    private fun handleIntentExtras(intent: Intent) {
        intent.apply {
            viewModel.apply {
                val contestId = getSerializableExtra(KEY_CONTEST_ID) as? Long
                contestId?.let {
                    viewModel.getById(it)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PAYMENT_REQUEST_CODE) {
            Log.d("ContestDetailsActivity", "Payment result received - ResultCode: $resultCode")

            when (resultCode) {
                RESULT_OK -> {
                    val paymentSuccess = data?.getBooleanExtra("payment_success", false) ?: false
                    val registrationStatus = data?.getStringExtra("registration_status")
                    val errorMessage = data?.getStringExtra("error_message")

                    Log.d("ContestDetailsActivity", "Payment Success: $paymentSuccess, Status: $registrationStatus")

                    handlePaymentResult(paymentSuccess, registrationStatus, errorMessage)

                    // Refresh data với delay longer để đảm bảo server đã cập nhật
                    refreshContestDataWithDelay(REFRESH_DELAY_MS)
                }
                RESULT_CANCELED -> {
                    Log.d("ContestDetailsActivity", "Payment cancelled")
                    // Vẫn refresh vì có thể có thay đổi data
                    refreshContestDataWithDelay(500L)
                }
                else -> {
                    Log.d("ContestDetailsActivity", "Payment failed or unknown result")
                    refreshContestDataWithDelay(REFRESH_DELAY_MS)
                }
            }
        }
    }

    private fun handlePaymentResult(paymentSuccess: Boolean, registrationStatus: String?, errorMessage: String?) {
        when {
            paymentSuccess -> {
                showSuccessMessage("Thanh toán thành công! Bạn đã đăng ký thành công cuộc thi.")
            }
            errorMessage != null -> {
                showErrorMessage(errorMessage)
            }
            else -> {
                when (registrationStatus) {
                    "PENDING" -> showInfoMessage("Đăng ký của bạn đang chờ thanh toán.")
                    "BLOCK" -> showErrorMessage("Tài khoản của bạn đã bị chặn khỏi cuộc thi này.")
                    "ACTIVE" -> showSuccessMessage("Đăng ký thành công!")
                    else -> Unit
                }
            }
        }
    }

    private fun refreshContestDataWithDelay(delayMs: Long) {
        if (isDataRefreshing) {
            Log.d("ContestDetailsActivity", "Data refresh already in progress, skipping")
            return
        }

        isDataRefreshing = true

        handler.postDelayed({
            refreshContestData()
        }, delayMs)
    }

    private fun refreshContestData() {
        viewModel.contest.value?.id?.let { contestId ->
            Log.d("ContestDetailsActivity", "Refreshing contest data for ID: $contestId")
            viewModel.getById(contestId)
        }
    }

    private fun refreshUserRegistrationStatus() {
        getCurrentUserEmail()?.let { email ->
            handler.postDelayed({
                Log.d("ContestDetailsActivity", "Refreshing user registration status for: $email")
                viewModel.resetRegistrationState()
                viewModel.checkRegister(email)
            }, 200L)
        }
    }

    private fun getCurrentUserEmail(): String? {
        return try {
            runBlocking {
                userPreferences.email.first()
            }
        } catch (e: Exception) {
            Log.e("ContestDetailsActivity", "Error getting user email: ${e.message}")
            null
        }
    }

    private fun showSuccessMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showInfoMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun getViewModel() = ContestDetailsViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) =
        ActivityContestDetailsBinding.inflate(inflater)

    override fun getActivityRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val api = retrofitInstance.buildApi(ContestApiService::class.java, token)
        return listOf(
            ContestRepository(api)
        )
    }

    // Rest of the methods remain the same...
    private fun setUpBackButton() {
        binding.buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun setUpScrollView() {
        binding.scrollView.viewTreeObserver.addOnScrollChangedListener {
            if (!isTabSelected) {
                binding.apply {
                    val scrollY = scrollView.scrollY
                    updateTabSelectionBasedOnScroll(scrollY)
                }
            }
        }
    }

    private fun updateTabSelectionBasedOnScroll(scrollY: Int) {
        binding.apply {
            val sectionDetailsTop = getTopRelativeToParent(sectionDetails)
            val deadlineTop = getTopRelativeToParent(sectionDeadline)
            val rewardsTop = getTopRelativeToParent(sectionRewards)
            val rulesTop = getTopRelativeToParent(sectionRules)
            val organizationalTop = getTopRelativeToParent(sectionOrganizational)
            val sectionLeaderBoardTop = getTopRelativeToParent(sectionLeaderBoard)

            when {
                scrollY >= organizationalTop -> {
                    tabLayout.selectTab(tabLayout.getTabAt(5))
                    tabLayout.visible(true)
                }
                scrollY >= sectionLeaderBoardTop -> {
                    tabLayout.selectTab(tabLayout.getTabAt(4))
                    tabLayout.visible(true)
                }
                scrollY >= rulesTop -> {
                    tabLayout.selectTab(tabLayout.getTabAt(3))
                    tabLayout.visible(true)
                }
                scrollY >= rewardsTop -> {
                    tabLayout.selectTab(tabLayout.getTabAt(2))
                    tabLayout.visible(true)
                }
                scrollY >= deadlineTop -> {
                    tabLayout.selectTab(tabLayout.getTabAt(1))
                    tabLayout.visible(true)
                }
                scrollY >= sectionDetailsTop -> {
                    tabLayout.selectTab(tabLayout.getTabAt(0))
                    tabLayout.visible(true)
                }
                else -> {
                    tabLayout.visible(false)
                }
            }
        }
    }

    private fun getTopRelativeToParent(view: View): Int {
        var offset = 0
        var v: View? = view
        while (v != null && v != binding.scrollView) {
            offset += v.top
            v = v.parent as? View
        }
        return offset
    }

    private fun setUpTabLayout() {
        binding.apply {
            tabLayout.visible(false)
            tabLayout.addTab(tabLayout.newTab().setText(sectionDetailsTitle.text))
            tabLayout.addTab(tabLayout.newTab().setText(sectionDeadlineTitle.text))
            tabLayout.addTab(tabLayout.newTab().setText(sectionRewardsTitle.text))
            tabLayout.addTab(tabLayout.newTab().setText(sectionRulesTitle.text))
            tabLayout.addTab(tabLayout.newTab().setText(sectionLeaderBoardTitle.text))
            tabLayout.addTab(tabLayout.newTab().setText(sectionOrganizationalTitle.text))

            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    isTabSelected = true
                    scrollToSelectedTab(tab?.position ?: 0)
                    handler.postDelayed({
                        isTabSelected = false
                    }, 500)
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        }
    }

    private fun scrollToSelectedTab(position: Int) {
        binding.apply {
            when (position) {
                0 -> scrollView.smoothScrollTo(0, sectionDetails.top)
                1 -> scrollView.smoothScrollTo(0, sectionDeadline.top)
                2 -> scrollView.smoothScrollTo(0, sectionRewards.top)
                3 -> scrollView.smoothScrollTo(0, sectionRules.top)
                4 -> scrollView.smoothScrollTo(0, sectionLeaderBoard.top)
                5 -> scrollView.smoothScrollTo(0, sectionOrganizational.top)
            }
        }
    }
}