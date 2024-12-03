package com.university.marathononline.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.models.Race
import com.university.marathononline.data.repository.RaceRepository
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: RaceRepository
): BaseViewModel(listOf(repository)) {

    private val _getRaceResponse: MutableLiveData<Resource<List<Race>>> = MutableLiveData()
    val getRaceResponse: LiveData<Resource<List<Race>>> get() = _getRaceResponse

    fun getRaces(){
        viewModelScope.launch {
            _getRaceResponse.value = Resource.Loading
            _getRaceResponse.value = repository.getByRunner()
        }
    }
}