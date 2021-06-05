package com.decagon.mobifind.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.decagon.mobifind.model.data.MobifindUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class MapViewModel : ViewModel() {
    private var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var _details = MutableLiveData<MobifindUser>()
    val details: LiveData<MobifindUser>
        get() = _details

    init {
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
    }

    fun getMapDetails(phoneNumber: String) {
        firestore.collection("mobifindUsers").document(phoneNumber)
            .collection("details")
            .document(phoneNumber).addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null) {
                    val userDetails = value.toObject(MobifindUser::class.java)
                    if(userDetails != null){
                        _details.value = userDetails!!
                    }
                }
            }
    }

}
