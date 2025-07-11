package com.university.marathononline.ui.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.university.marathononline.R
import com.university.marathononline.utils.startNewActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SignUpSuccessActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_success)

        lifecycleScope.launch {
            delay(3000)
            startNewActivity(LoginActivity::class.java, true)
        }
    }
}