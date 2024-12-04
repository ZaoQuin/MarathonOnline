package com.university.marathononline.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.models.Reward

class RunnerRewardsViewModel(

): BaseViewModel(listOf()) {

    private val _rewards = MutableLiveData<List<Reward>>()
    val rewards: LiveData<List<Reward>> get() = _rewards

    fun setRewards(rewards: List<Reward>){
        _rewards.value = rewards
    }
}