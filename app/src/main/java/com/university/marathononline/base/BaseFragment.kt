package com.university.marathononline.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.university.marathononline.data.api.RetrofitInstance
import com.university.marathononline.data.api.auth.AuthApiService
import com.university.marathononline.data.response.UserPreferences
import com.university.marathononline.ui.view.activity.MainActivity
import com.university.marathononline.ui.view.activity.SplashRedirectActivity
import com.university.marathononline.utils.startNewActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

abstract class BaseFragment<VM: BaseViewModel, B: ViewBinding, R: BaseRepository>: Fragment(){

    protected lateinit var userPreferences: UserPreferences
    protected lateinit var binding: B
    protected lateinit var viewModel: VM
    protected val retrofitInstance = RetrofitInstance

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        userPreferences = UserPreferences(requireContext())
        binding = getFragmentBinding(inflater, container)
        val factory = ViewModelFactory(getFragmentRepository())
        viewModel = ViewModelProvider(this, factory)[getViewModel()]

        lifecycleScope.launch {
            userPreferences.authToken.first()
        }

        return binding.root
    }

    abstract fun getViewModel(): Class<VM>

    abstract fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?): B

    abstract fun getFragmentRepository(): R

    fun logout() = lifecycleScope.launch {
        val authToken = userPreferences.authToken.first()
        val api = retrofitInstance.buildApi(AuthApiService::class.java, authToken)
        viewModel.logout(api)
        userPreferences.clearAuth()
        requireActivity().startNewActivity(SplashRedirectActivity::class.java, true)
    }
}