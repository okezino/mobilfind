package com.decagon.mobifind.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.decagon.mobifind.model.data.MobifindUser


// For Testing Only
class TrackViewModel : ViewModel() {
    private val _tracking = MutableLiveData<List<MobifindUser>>(emptyList())
    private val _trackers = MutableLiveData<List<MobifindUser>>(emptyList())


    fun addUsers() {
        val list = mutableListOf<MobifindUser>()
        val user1 = MobifindUser(
            phoneNumber = "+2348057084902",
            name = "Godday Okoduwa"
        )
        val user2 = MobifindUser(
            phoneNumber = "+2349057051318",
            name = "Alhaji Okezi"
        )
        list.add(user1)
        list.add(user2)
        _trackers.value = list
        _tracking.value = list
    }
}