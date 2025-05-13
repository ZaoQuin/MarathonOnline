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
            val timeParts = time.split(":")
            binding.apply {
                daysTextView.text = timeParts[0]
                hoursTextView.text = timeParts[1]
                minutesTextView.text = timeParts[2]
                secondsTextView.text = timeParts[3]
            }
        }
        viewModel.contest.observe(this){
            lifecycleScope.launch {
                val emailValue = userPreferences.email.first()
                emailValue?.let { it1 -> viewModel.checkRegister(it1) }
            }
        }
        viewModel.isRegistered.observe(this){
            Log.e("ContestDetailsActivity", it.toString())
            binding.registerContainer.visible(!it)
            binding.recordContainer.visible(it)
        }

        viewModel.isBlocked.observe(this){
            Log.e("ContestDetailsActivity", "Check Block")
            if(it) {
                binding.btnRecord.enable(false)
                binding.btnRecord.text = ERegistrationStatus.BLOCK.value
            }
        }

        viewModel.registration.observe(this){
            binding.apply {
                val currentDistance = it.records.sumOf { it.distance }
                val contestDistance = viewModel.contest.value?.distance
                val ratio = (currentDistance / contestDistance!!)*100
                binding.apply {
                    processBar.progress = ratio.toInt()
                    processBarValue.text = "${formatDistance(currentDistance)}/${formatDistance(contestDistance)}"
                }

            }
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

        binding.apply {
            btnRegisterContest.setOnClickListener {
                viewModel.contest.value?.let {
                    startNewActivity(PaymentConfirmationActivity::class.java,
                        mapOf( KEY_CONTEST to it)
                    )
                }
            }

            btnRecord.setOnClickListener{
                startNewActivity(RecordActivity::class.java)
            }
        }
    }

    private fun setUpData() {
        viewModel.contest.value?.let {
            binding.apply {
                tvDistance.text = it.distance?.let { it1 -> formatDistance(it1) }
                tvFee.text = it.fee?.let { it1 -> convertToVND(it1) }
                tvMaxMembers.text = if (it.maxMembers == 0) "Không giới hạn người tham gia" else it.maxMembers.toString()
                contestName.text = it.name
                startDate.text = it.startDate?.let { it1 -> DateUtils.convertToVietnameseDate(it1) }
                endDate.text = it.endDate?.let { it1 -> DateUtils.convertToVietnameseDate(it1) }
                contentDetails.text = it.description
                feeRegister.text = it.fee?.let { it1 -> convertToVND(it1) }
                organizationalName.text = it.organizer?.fullName
                emailOrganizer.text = it.organizer?.email
                val totalRegistration = it.registrations?.size
                maxMembers.text = if (it.maxMembers == 0) "Không giới hạn người tham gia" else "$totalRegistration/ ${it.maxMembers.toString()}"
                if(it.maxMembers != 0 && it.maxMembers!! <= it.registrations?.size!!){
                    btnRegisterContest.enable(false)
                    btnRegisterContest.text = "Số lượng đăng ký đã quá giới hạn"
                }
                if(it.registrationDeadline?.let { it1 -> DateUtils.convertStringToLocalDateTime(it1).isBefore(LocalDateTime.now()) } == true){
                    btnRegisterContest.enable(false)
                    btnRegisterContest.text = "Hết hạn đăng ký"
                }
                if (it.startDate?.let { start ->
                        DateUtils.convertStringToLocalDateTime(start).isBefore(LocalDateTime.now())
                    } == false && it.status == EContestStatus.ACTIVE) {
                    binding.btnRecord.enable(false)
                    binding.btnRecord.text = "Cuộc thi chưa bắt đầu"
                } else {
                    sectionLeaderBoard.visible(it.status == EContestStatus.ACTIVE
                            || it.status == EContestStatus.FINISHED
                            || it.status == EContestStatus.COMPLETED)
                }

                if(it.status == EContestStatus.PENDING ){
                    btnRegisterContest.enable(false)
                    btnRegisterContest.text = it.status!!.value
                }

                if ( it.status == EContestStatus.COMPLETED ||
                    it.status == EContestStatus.FINISHED){
                    btnRecord.enable(false)
                    btnRecord.text = it.status!!.value
                }

                if ( it.status == EContestStatus.COMPLETED ||
                    it.status == EContestStatus.FINISHED){
                    btnRegisterContest.enable(false)
                    btnRegisterContest.text = it.status!!.value
                }
            }
        }
    }

    private fun handleIntentExtras(intent: Intent) {
        intent.apply {
            viewModel.apply {
                (getSerializableExtra(KEY_CONTEST) as? Contest)?.let {
                    setContest(it)
                    it.rules?.let { it1 ->
                        setRules(it1)
                    }
                    it.rewards?.let { it1 ->
                        setRewardGroups(it1)
                    }
                    it.registrationDeadline?.let { it2 ->
                        setDeadlineTime(it2)
                    }
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


    var isTabSelected = false

    private fun setUpScrollView() {
        binding.scrollView.viewTreeObserver.addOnScrollChangedListener {
            if (!isTabSelected) {
                binding.apply {
                    val scrollY = scrollView.scrollY
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
                    when (tab?.position) {
                        0 -> scrollView.smoothScrollTo(0, sectionDetails.top)
                        1 -> scrollView.smoothScrollTo(0, sectionDeadline.top)
                        2 -> scrollView.smoothScrollTo(0, sectionRewards.top)
                        3 -> scrollView.smoothScrollTo(0, sectionRules.top)
                        4 -> scrollView.smoothScrollTo(0, sectionLeaderBoard.top)
                        5 -> scrollView.smoothScrollTo(0, sectionOrganizational.top)
                    }

                    handler.postDelayed({
                        isTabSelected = false
                    }, 500)
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        }
    }

    private val handler = Handler(Looper.getMainLooper())
}