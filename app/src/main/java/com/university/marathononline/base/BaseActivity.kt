package com.university.marathononline.base

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.google.firebase.messaging.FirebaseMessaging
import com.university.marathononline.data.api.RetrofitInstance
import com.university.marathononline.data.api.auth.AuthApiService
import com.university.marathononline.data.preferences.UserPreferences
import com.university.marathononline.ui.view.activity.MainActivity.Companion.TAG
import com.university.marathononline.ui.view.activity.SplashRedirectActivity
import com.university.marathononline.utils.startNewActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

abstract class BaseActivity<VM: BaseViewModel, B: ViewBinding>: AppCompatActivity() {

    protected lateinit var userPreferences: UserPreferences
    protected lateinit var binding: B
    protected lateinit var viewModel: VM
    protected val retrofitInstance = RetrofitInstance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userPreferences = UserPreferences(this)
        binding = getActivityBinding(layoutInflater)
        val factory = ViewModelFactory(getActivityRepositories())
        viewModel = ViewModelProvider(this, factory)[getViewModel()]

        lifecycleScope.launch {
            userPreferences.authToken.first()
        }

        setContentView(binding.root)
    }

    abstract fun getViewModel(): Class<VM>

    abstract fun getActivityBinding(inflater: LayoutInflater): B

    abstract fun getActivityRepositories(): List<BaseRepository>

    fun logout() = lifecycleScope.launch {
        val authToken = userPreferences.authToken.first()
        val api = retrofitInstance.buildApi(AuthApiService::class.java, authToken)
        unsubscribeFromFeedbackTopics()
        viewModel.logout(api)
        userPreferences.clearAuth()
        startNewActivity(SplashRedirectActivity::class.java, true)
    }

    private fun unsubscribeFromFeedbackTopics() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic("feedback_notifications")
            .addOnCompleteListener { task ->
                Log.d(TAG, if (task.isSuccessful) {
                    "Unsubscribed from feedback_notifications"
                } else {
                    "Failed to unsubscribe from feedback_notifications"
                })
            }

        lifecycleScope.launch {
            val userEmail = userPreferences.email.first()
            userEmail?.let { email ->
                val topicName = "user_${email.replace("@", "_").replace(".", "_")}_feedback"
                FirebaseMessaging.getInstance().unsubscribeFromTopic(topicName)
                    .addOnCompleteListener { task ->
                        Log.d(TAG, if (task.isSuccessful) {
                            "Unsubscribed from personal feedback topic"
                        } else {
                            "Failed to unsubscribe from personal feedback topic"
                        })
                    }
            }
        }
    }
}
