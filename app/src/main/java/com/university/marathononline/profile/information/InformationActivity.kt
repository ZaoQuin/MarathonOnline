package com.university.marathononline.profile.information

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.university.marathononline.databinding.ActivityInformationBinding

class InformationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInformationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonBack.setOnClickListener {
            finish()
        }
    }
}