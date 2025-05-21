package com.university.marathononline.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.contest.ContestApiService
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.EContestStatus
import com.university.marathononline.data.models.ERegistrationStatus
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.ui.viewModel.ContestDetailsViewModel
import com.university.marathononline.databinding.ActivityContestDetailsBinding
import com.university.marathononline.ui.adapter.RewardAdapter
import com.university.marathononline.ui.adapter.RuleAdapter
import com.university.marathononline.ui.view.fragment.LeaderBoardFragment
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.KEY_CONTEST
import com.university.marathononline.utils.KEY_REGISTRATIONS
import com.university.marathononline.utils.convertToVND
import com.university.marathononline.utils.enable
import com.university.marathononline.utils.formatDistance
import com.university.marathononline.utils.startNewActivity
import com.university.marathononline.utils.visible
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.Serializable
import java.time.LocalDateTime

class ContestDetailsActivity :
    BaseActivity<ContestDetailsViewModel, ActivityContestDetailsBinding>() {

    private lateinit var ruleAdapter: RuleAdapter
    private lateinit var rewardAdapter: RewardAdapter
    private val handler = Handler(Looper.getMainLooper())
    private var isTabSelected = false

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
        val fragment = LeaderBoardFragment().apply {
            arguments = Bundle().apply {
                putSerializable(KEY_REGISTRATIONS, registrations as Serializable)
            }
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, fragment)
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
        viewModel.contest.observe(this) {
            lifecycleScope.launch {
                val emailValue = userPreferences.email.first()
                emailValue?.let { email -> viewModel.checkRegister(email) }
            }
        }
        viewModel.isRegistered.observe(this) {
            Log.e("ContestDetailsActivity", it.toString())
            updateContainerVisibility(it)
        }
        viewModel.isBlocked.observe(this) {
            Log.e("ContestDetailsActivity", "Check Block")
            updateBlockedStatus(it)
        }
        viewModel.registration.observe(this) {
            updateProgressDisplay(it)
        }
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

    private fun updateContainerVisibility(isRegistered: Boolean) {
        binding.registerContainer.visible(!isRegistered)
        binding.recordContainer.visible(isRegistered)
    }

    private fun updateBlockedStatus(isBlocked: Boolean) {
        if (isBlocked) {
            binding.btnRecord.enable(false)
            binding.btnRecord.text = ERegistrationStatus.BLOCK.value
        }
    }

    private fun updateProgressDisplay(registration: com.university.marathononline.data.models.Registration) {
        val records = registration.records ?: emptyList()
        val currentDistance = records.sumOf { it.distance }
        val contestDistance = viewModel.contest.value?.distance ?: 0.0
        val ratio = if (contestDistance > 0) (currentDistance / contestDistance) * 100 else 0.0

        binding.apply {
            processBar.progress = ratio.toInt()
            processBarValue.text = "${formatDistance(currentDistance)}/${formatDistance(contestDistance)}"
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
                viewModel.contest.value?.let { contest ->
                    startNewActivity(PaymentConfirmationActivity::class.java,
                        mapOf(KEY_CONTEST to contest)
                    )
                }
            }

            btnRecord.setOnClickListener {
                startNewActivity(RecordActivity::class.java)
            }
        }
    }

    private fun setUpData() {
        viewModel.contest.value?.let { contest ->
            binding.apply {
                populateBasicInfo(contest)
                updateButtonStates(contest)
                updateLeaderboardVisibility(contest)
            }
        }
    }

    private fun updateLeaderboardVisibility(contest: Contest) {
        // Show leaderboard for active, finished or completed contests
        binding.sectionLeaderBoard.visible(
            contest.status == EContestStatus.ACTIVE ||
                    contest.status == EContestStatus.FINISHED ||
                    contest.status == EContestStatus.COMPLETED
        )
    }

    private fun populateBasicInfo(contest: Contest) {
        binding.apply {
            tvDistance.text = contest.distance?.let { formatDistance(it) }
            tvFee.text = contest.fee?.let { convertToVND(it) }
            tvMaxMembers.text = if (contest.maxMembers == 0) "Không giới hạn người tham gia" else contest.maxMembers.toString()
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

    private fun updateButtonStates(contest: Contest) {
        binding.apply {
            // Registration button states
            val now = LocalDateTime.now()
            val registrationDeadlinePassed = contest.registrationDeadline?.let {
                DateUtils.convertStringToLocalDateTime(it).isBefore(now)
            } ?: false

            val isMaxRegistrationsReached = contest.maxMembers != 0 &&
                    contest.maxMembers!! <= (contest.registrations?.size ?: 0)

            val contestHasntStarted = contest.startDate?.let {
                DateUtils.convertStringToLocalDateTime(it).isAfter(now)
            } ?: false

            // Update registration button
            when {
                contest.status == EContestStatus.PENDING -> {
                    btnRegisterContest.enable(false)
                    btnRegisterContest.text = contest.status!!.value
                }
                contest.status == EContestStatus.COMPLETED || contest.status == EContestStatus.FINISHED -> {
                    btnRegisterContest.enable(false)
                    btnRegisterContest.text = contest.status!!.value
                }
                isMaxRegistrationsReached -> {
                    btnRegisterContest.enable(false)
                    btnRegisterContest.text = "Số lượng đăng ký đã quá giới hạn"
                }
                registrationDeadlinePassed -> {
                    btnRegisterContest.enable(false)
                    btnRegisterContest.text = "Hết hạn đăng ký"
                }
            }

            // Update record button
            when {
                contestHasntStarted && contest.status == EContestStatus.ACTIVE -> {
                    btnRecord.enable(false)
                    btnRecord.text = "Cuộc thi chưa bắt đầu"
                }
                contest.status == EContestStatus.COMPLETED || contest.status == EContestStatus.FINISHED -> {
                    btnRecord.enable(false)
                    btnRecord.text = contest.status!!.value
                }
            }
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