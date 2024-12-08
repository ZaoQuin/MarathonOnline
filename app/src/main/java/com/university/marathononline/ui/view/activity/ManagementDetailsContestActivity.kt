package com.university.marathononline.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
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
import com.university.marathononline.utils.finishAndGoBack
import com.university.marathononline.utils.startNewActivity
import com.university.marathononline.utils.visible
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class ManagementDetailsContestActivity :
    BaseActivity<ManagementDetailsContestActivityViewModel, ActivityManagementContestDetailsBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntentExtras(intent)
        setupUI()
        setUpObserve()
        setupDeleteButton()
    }

    private fun setupUI() {
        binding.buttonBack.setOnClickListener{
            finishAndGoBack()
        }

        binding.buttonEdit.setOnClickListener{
            val contest = viewModel.contest.value?:null
            contest?.let {
                startNewActivity(AddContestActivity::class.java,
                    mapOf(KEY_CONTEST to it))
            }
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

        viewModel.deleteResponse.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> {
                    Toast.makeText(this, "Xóa thành công", Toast.LENGTH_SHORT).show()
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
        binding.buttonDelete.visible(pending)
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
        binding.buttonDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Xác nhận xóa")
        builder.setMessage("Bạn có chắc chắn muốn xóa mục này không?")
        builder.setPositiveButton("Xóa") { dialog, _ ->
            viewModel.delete()
            dialog.dismiss()
        }
        builder.setNegativeButton("Hủy") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }
//
//    private fun setupDeleteButton() {
//        binding.fabEdit.setOnClickListener {
//            viewModel.editContest(contest)
//        }
//    }

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