package com.decagon.mobifind.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.decagon.mobifind.model.data.UserLocation

class MobifindViewModel : ViewModel() {
    private var _userLocation = MutableLiveData<UserLocation>()
    val userLocation : LiveData<UserLocation>
    get() = _userLocation

    fun saveUserLocationUpdates(userLocation: UserLocation){
        _userLocation.value = userLocation
    }



}