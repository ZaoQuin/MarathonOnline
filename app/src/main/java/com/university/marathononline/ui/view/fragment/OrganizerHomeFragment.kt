package com.university.marathononline.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.university.marathononline.base.BaseFragment
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.auth.AuthApiService
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.databinding.FragmentOrganizerHomeBinding
import com.university.marathononline.ui.viewModel.MainViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class OrganizerHomeFragment : BaseFragment<MainViewModel, FragmentOrganizerHomeBinding>() {

    override fun getViewModel(): Class<MainViewModel> = MainViewModel::class.java

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentOrganizerHomeBinding {
        return FragmentOrganizerHomeBinding.inflate(inflater, container, false)
    }

    override fun getFragmentRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val authApi = retrofitInstance.buildApi(AuthApiService::class.java, token)
        return listOf(
            AuthRepository(authApi, userPreferences)
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.userFullNameText.text = runBlocking { userPreferences.fullName.first() }
    }
}
