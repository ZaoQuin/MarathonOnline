package com.university.marathononline.ui.view.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.university.marathononline.databinding.ActivitySearchBinding
import com.university.marathononline.data.models.Contest
import com.university.marathononline.ui.viewModel.SearchViewModel
import com.university.marathononline.ui.adapter.ResultAdapter
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.contest.ContestApiService
import com.university.marathononline.data.repository.ContestRepository
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class SearchActivity : BaseActivity<SearchViewModel, ActivitySearchBinding>() {

    private lateinit var adapter: ResultAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getActiveContests()

        setInputSearch()
        setAdapter()
        setUpBackButton()
        observe()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getActiveContests()
    }


    private fun setInputSearch() {
        binding.key.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.search(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setAdapter() {
        binding.results.layoutManager = LinearLayoutManager(this)
        adapter = ResultAdapter(emptyList())
        binding.results.adapter = adapter
    }

    private fun setUpBackButton() {
        binding.buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun observe() {
        viewModel.results.observe(this, Observer { results: List<Contest> ->
            adapter.updateData(results)
        })

        viewModel.getContestReponse.observe(this){
            when(it){
                is Resource.Success -> viewModel.setContests(it.value.contests)
                is Resource.Failure -> handleApiError(it)
                else -> Unit
            }
        }
    }

    override fun getViewModel(): Class<SearchViewModel> {
        return SearchViewModel::class.java
    }

    override fun getActivityBinding(inflater: LayoutInflater): ActivitySearchBinding {
        return ActivitySearchBinding.inflate(inflater)
    }

    override fun getActivityRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val apiContest = retrofitInstance.buildApi(ContestApiService::class.java, token)
        return listOf(
            ContestRepository(apiContest)
        )
    }
}
