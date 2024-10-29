package com.university.marathononline.ui.view

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.university.marathononline.databinding.ActivityRecordBinding
import com.university.marathononline.ui.viewModel.RecordViewModel

class RecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordBinding
    private val viewModel: RecordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpBackButton()
        setUpRecordButton()
    }

    private fun setUpRecordButton() {
        binding.playButton.setOnClickListener{
            binding.playButton.visibility = View.GONE
            binding.stopButton.visibility = View.VISIBLE
        }


        binding.stopButton.setOnClickListener{
            binding.stopButton.visibility = View.GONE
            binding.playButton.visibility = View.VISIBLE
        }
    }

    private fun setUpBackButton() {
        binding.buttonBack.setOnClickListener {
            finish()
        }
    }
}