package com.decagon.mobifind.viewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.decagon.mobifind.model.data.MobifindUser
import com.decagon.mobifind.model.data.Photo
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage

class SignupViewModel : ViewModel() {
    private var firestore : FirebaseFirestore = FirebaseFirestore.getInstance()
    private var _mobifindUsers = MutableLiveData<ArrayList<MobifindUser>>()
    val mobifindUser : LiveData<ArrayList<MobifindUser>>
    get() = _mobifindUsers
    private var storageReference = FirebaseStorage.getInstance().getReference()

    init {
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        listenToSpecimens()
    }

    /**
     * Listens for updates from firestore
     */
    private fun listenToSpecimens() {
        firestore.collection("mobifindUsers").addSnapshotListener { value, error ->
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
                    }
                }
                _mobifindUsers.value = allMobifindUser
            }
        }

    }

    fun save(mobiUser: MobifindUser, photos: ArrayList<Photo>, user: FirebaseUser) {
        val document = firestore.collection("mobifindUsers").document()
        mobiUser.userId = document.id
       document.set(mobiUser)
            .addOnSuccessListener {
                Log.d("Firebase", "document saved")
                if (photos.size > 0){
                    savePhotos(mobiUser,photos,user)
                }
            }
            .addOnFailureListener{
                Log.d("Firebase", "save: failed")
            }

    }

    private fun savePhotos(
        mobiUser: MobifindUser,
        photos: java.util.ArrayList<Photo>,
        user1: FirebaseUser
    ) {
      val collection =  firestore.collection("mobifindUsers")
            .document(mobiUser.userId)
            .collection("photos")
        photos.forEach { photo ->
            collection.add(photo).addOnSuccessListener {
                photo.id = it.id
                uploadPhotos(mobiUser,photos,user1)
            }
        }
    }

    private fun uploadPhotos(
        mobiUser: MobifindUser,
        photos: java.util.ArrayList<Photo>,
        user1: FirebaseUser
    ) {
        photos.forEach {
            photo-> val uri = Uri.parse(photo.localUri)
            val imageRef = storageReference.child("images/"+ user1.uid+"/"+ uri.lastPathSegment)
            val uploadTask = imageRef.putFile(uri)
            uploadTask.addOnSuccessListener {
               val downloadUrl =  imageRef.downloadUrl
                downloadUrl.addOnSuccessListener {
                   photo.remoteUri = it.toString()
                    //Update our cloud firestore with the public image URI
                    uploadPhotoDatabase(mobiUser,photo)
                }

            }
            uploadTask.addOnFailureListener{
                Log.e("Firebase", "uploadPhotos: ${it.message}", )
            }
        }


    }

    private fun uploadPhotoDatabase(mobiUser: MobifindUser, photo: Photo) {
        firestore.collection("mobifindUsers").document(mobiUser.userId)
            .collection("photos")
            .document(photo.id).set(photo).addOnSuccessListener {
                Log.d("Uploaded", "uploadPhotoDatabase: ${photo.remoteUri}")
            }
    }

}