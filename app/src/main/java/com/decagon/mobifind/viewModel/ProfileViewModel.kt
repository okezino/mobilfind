package com.decagon.mobifind.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.decagon.mobifind.model.data.MobifindUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class ProfileViewModel : ViewModel() {
    private var firestore : FirebaseFirestore = FirebaseFirestore.getInstance()
    private var user : FirebaseUser? = null


    private var _userPhotoUrl = MutableLiveData<String>()
    val userPhotoUrl : LiveData<String>
        get() = _userPhotoUrl



    init {
        user = FirebaseAuth.getInstance().currentUser
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        getPhotoUri()
    }



    private fun getPhotoUri(){
        firestore.collection("mobifindUsers").document(user?.phoneNumber!!).collection("details")
            .document(user?.phoneNumber!!).addSnapshotListener { value, error ->
                _userPhotoUrl.value = value?.get("photoUri") as String?
            }
    }


}