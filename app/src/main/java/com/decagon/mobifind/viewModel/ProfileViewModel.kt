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
    private var _mobifindUsers = MutableLiveData<ArrayList<MobifindUser>>()
    val mobifindUser : LiveData<ArrayList<MobifindUser>>
        get() = _mobifindUsers

    private var _userPhotoUrl = MutableLiveData<String>()
    val userPhotoUrl : LiveData<String>
        get() = _userPhotoUrl



    init {
        user = FirebaseAuth.getInstance().currentUser
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        listenToSpecimens()
    }

    /**
     * Listens for updates from firestore
     */
    private fun listenToSpecimens() {
        firestore.collection("mobifindUsers")
            .addSnapshotListener { value, error ->
            if (error != null){
                Log.w("Listening", "listenToSpecimens: Listen Failed")
                return@addSnapshotListener
            }
            if (value != null){
                val allMobifindUser = ArrayList<MobifindUser>()
                val documents =   value.documents
                documents.forEach {
                    val mobifindUser = it.toObject(MobifindUser::class.java)
                    if (mobifindUser != null){
                        mobifindUser.userId = it.id
                        allMobifindUser.add(mobifindUser)
                        if (mobifindUser.phoneNumber == user?.phoneNumber){
                            _userPhotoUrl.value = mobifindUser.photoUri
                        }
                    }
                }
                _mobifindUsers.value = allMobifindUser
              //  getUserPicture(FirebaseAuth.getInstance().currentUser!!)

            }
        }

    }


}