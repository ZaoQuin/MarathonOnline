package com.university.marathononline.ui.view.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.android.material.tabs.TabLayoutMediator
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.contest.ContestApiService
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.EContestStatus
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.databinding.ActivityManagementContestDetailsBinding
import com.university.marathononline.ui.adapter.ManagementDetailsContestPagerAdapter
import com.university.marathononline.ui.viewModel.ManagementDetailsContestActivityViewModel
import com.university.marathononline.utils.KEY_CONTEST
import com.university.marathononline.utils.KEY_UPDATE_CONTEST
import com.university.marathononline.utils.finishAndGoBack
import com.university.marathononline.utils.visible
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class ManagementDetailsContestActivity :
    BaseActivity<ManagementDetailsContestActivityViewModel, ActivityManagementContestDetailsBinding>() {

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val updatedContest = result.data?.getSerializableExtra(KEY_UPDATE_CONTEST) as Contest

                viewModel.setContest(updatedContest)
            }
        }
        handleIntentExtras(intent)
        setUpObserve()
        setupDeleteButton()
        setupUI()

    }

    private fun setupUI() {
        binding.buttonBack.setOnClickListener{
            finishAndGoBack()
        }

        binding.buttonEdit.setOnClickListener{
            val contest = viewModel.contest.value ?: return@setOnClickListener
            val intent = Intent(this, AddContestActivity::class.java)
            intent.putExtra(KEY_CONTEST, contest)
            resultLauncher.launch(intent)
        }
    }

    private fun handleIntentExtras(intent: Intent) {
        intent.apply {
            viewModel.apply {
                (getSerializableExtra(KEY_CONTEST) as? Contest)?.let { setContest(it) }
            }
        }
    }

    private fun setUpObserve() {
        viewModel.contest.observe(this){
            setupViewPager()
            setupMenuButton()
        }

        viewModel.cancelResponse.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> {
                    Toast.makeText(this, "Hủy thành công", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Resource.Failure -> {
                    val errorMessage = when {
                        resource.errorBody != null -> resource.errorBody.string()
                        resource.errorMessage != null -> resource.errorMessage
                        else -> "An unknown error occurred"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
                else -> Unit
            }
        }
    }

    private fun setupMenuButton() {
        val pending = viewModel.contest.value!!.status == EContestStatus.PENDING
        binding.buttonEdit.visible(pending)
        binding.btnCancel.visible(pending)
    }

    private fun setupViewPager() {
        val pagerAdapter = ManagementDetailsContestPagerAdapter(
            this,
            viewModel.contest.value!!
        )
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Chi tiết cuộc thi"
                1 -> "Danh sách đăng ký"
                else -> ""
            }
        }.attach()
    }

    private fun setupDeleteButton() {
        binding.btnCancel.setOnClickListener {
            showCancelConfirmationDialog()
        }
    }

    private fun showCancelConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Xác nhận hủy cuộc thi")
        builder.setMessage("Bạn có chắc chắn muốn hủy cuộc thi này không?")
        builder.setPositiveButton("Xác nhận") { dialog, _ ->
            viewModel.cancel()
            dialog.dismiss()
        }
        builder.setNegativeButton("Trở lại") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    override fun getViewModel() = ManagementDetailsContestActivityViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) = ActivityManagementContestDetailsBinding.inflate(inflater)

    override fun getActivityRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val apiContest = retrofitInstance.buildApi(ContestApiService::class.java, token)
        return listOf(
            ContestRepository(apiContest)
        )
    }
}