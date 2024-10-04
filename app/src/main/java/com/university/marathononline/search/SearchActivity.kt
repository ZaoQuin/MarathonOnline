package com.university.marathononline.search

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.university.marathononline.R
import com.university.marathononline.databinding.ActivitySearchBinding
import com.university.marathononline.entity.Contest
import com.university.marathononline.search.adapter.ResultAdapter

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var adapter: ResultAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setAdapter()
        setUpBackButton()

        observe()
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
    }
}