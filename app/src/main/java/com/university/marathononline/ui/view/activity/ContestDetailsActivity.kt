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
import com.google.android.material.tabs.TabLayout
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
        private const val REFRESH_DELAY_MS = 1000L // Tăng delay để đảm bảo server đã cập nhật
    }

    private lateinit var ruleAdapter: RuleAdapter
    private lateinit var rewardAdapter: RewardAdapter
    private val handler = Handler(Looper.getMainLooper())
    private var isTabSelected = false
    private var statusManager: ContestUserStatusManager? = null
    private var leaderBoardFragment: LeaderBoardFragment? = null
    private var isDataRefreshing = false // Flag để tránh refresh đồng thời

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntentExtras(intent)

        initializeUI()
        setRuleAdapter()
        setRewardAdapter()
        setUpLeaderBoard(savedInstanceState)
        setUpObserve()
    }

    private fun setUpLeaderBoard(savedInstanceState: Bundle?) {
        val registrations = viewModel.contest.value?.registrations
        leaderBoardFragment = LeaderBoardFragment().apply {
            arguments = Bundle().apply {
                putSerializable(KEY_REGISTRATIONS, registrations as Serializable)
            }
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, leaderBoardFragment!!)
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
            lifecycleScope.launch {
                val emailValue = userPreferences.email.first()
                emailValue?.let { email ->
                    // Reset registration state trước khi check
                    viewModel.resetRegistrationState()
                    viewModel.checkRegister(email)
                    updateStatusManager(contest, null)

                    // Cập nhật LeaderBoard với data mới
                    updateLeaderBoardFragment(contest.registrations)

                    // Cập nhật basic info với data mới
                    populateBasicInfo(contest)
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

    /**
     * Cập nhật LeaderBoard fragment với data mới
     */
    private fun updateLeaderBoardFragment(registrations: List<Registration>?) {
        leaderBoardFragment?.let { fragment ->
            val newBundle = Bundle().apply {
                putSerializable(KEY_REGISTRATIONS, registrations as? Serializable)
            }
            fragment.arguments = newBundle

            // Nếu fragment đã được add, cập nhật data
            if (fragment.isAdded) {
                fragment.updateRegistrations(registrations)
            }
        }
    }

    /**
     * Cập nhật StatusManager và UI dựa trên trạng thái mới
     */
    private fun updateStatusManager(contest: Contest, registration: Registration?) {
        statusManager = ContestUserStatusManager(contest, registration)
        updateUIBasedOnStatus()
    }

    /**
     * Cập nhật toàn bộ UI dựa trên trạng thái từ StatusManager
     */
    private fun updateUIBasedOnStatus() {
        statusManager?.let { manager ->
            val displayState = manager.getDisplayState()

            // Cập nhật visibility của các container
            updateContainerVisibility(displayState)

            // Cập nhật trạng thái button
            updateButtonStates(displayState)

            // Cập nhật progress nếu cần
            if (displayState.showProgress) {
                updateProgressDisplay(manager)
            }

            // Cập nhật leaderboard visibility
            updateLeaderboardVisibility(displayState.showLeaderboard)

            // Hiển thị status message nếu có
            displayState.statusMessage?.let { message ->
                showStatusMessage(message)
            }

            Log.d("ContestDetailsActivity", "UI updated - User Contest Status: ${displayState.userStatus}")
        }
    }

    /**
     * Cập nhật visibility của register và record container
     */
    private fun updateContainerVisibility(displayState: ContestUserStatusManager.ContestDisplayState) {
        binding.registerContainer.visible(!displayState.showProgress)
        binding.recordContainer.visible(displayState.showProgress)
    }

    /**
     * Cập nhật trạng thái và text của các button
     */
    private fun updateButtonStates(displayState: ContestUserStatusManager.ContestDisplayState) {
        binding.apply {
            // Register button
            btnRegisterContest.text = displayState.registerButtonText
            btnRegisterContest.enable(displayState.registerButtonEnabled)

            // Record button
            btnRecord.text = displayState.recordButtonText
            btnRecord.enable(displayState.recordButtonEnabled)
        }
    }

    /**
     * Cập nhật hiển thị progress
     */
    private fun updateProgressDisplay(manager: ContestUserStatusManager) {
        val (currentDistance, contestDistance, ratio) = manager.getProgressInfo()

        binding.apply {
            processBar.progress = ratio
            processBarValue.text = "${formatDistance(currentDistance)}/${formatDistance(contestDistance)}"
        }
    }

    /**
     * Cập nhật visibility của leaderboard
     */
    private fun updateLeaderboardVisibility(shouldShow: Boolean) {
        binding.sectionLeaderBoard.visible(shouldShow)
    }

    /**
     * Hiển thị status message
     */
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

    /**
     * Xử lý click register button dựa trên trạng thái
     */
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
                (getSerializableExtra(KEY_CONTEST) as? Contest)?.let { contest ->
                    setContest(contest)
                    contest.rules?.let { rules -> setRules(rules) }
                    contest.rewards?.let { rewards -> setRewardGroups(rewards) }
                    contest.registrationDeadline?.let { deadline -> setDeadlineTime(deadline) }
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

    /**
     * Refresh contest data với delay để đảm bảo server đã cập nhật
     */
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
            viewModel.refreshContest(contestId)
        }
    }

    /**
     * Refresh user registration status với data contest mới nhất
     */
    private fun refreshUserRegistrationStatus() {
        getCurrentUserEmail()?.let { email ->
            // Delay nhỏ để đảm bảo contest data đã được cập nhật
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