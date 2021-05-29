package com.decagon.mobifind.viewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.decagon.mobifind.model.data.MobifindUser
import com.decagon.mobifind.model.data.Photo
import com.decagon.mobifind.model.data.UserLocation
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage

class MobifindViewModel : ViewModel() {
    private var _userLocation = MutableLiveData<UserLocation>()
    val userLocation : LiveData<UserLocation>
    get() = _userLocation


    private var firestore : FirebaseFirestore = FirebaseFirestore.getInstance()

    private var _uploadStatus = MutableLiveData<String>()
    val uploadStatus : LiveData<String>
        get() = _uploadStatus

    private var storageReference = FirebaseStorage.getInstance().reference

    init {
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
    }

    fun saveUserLocationUpdates(userLocation: UserLocation){
        _userLocation.value = userLocation
    }



    fun save(mobiUser: MobifindUser, photo: Photo, user: FirebaseUser) {
        mobiUser.latitude = _userLocation.value?.latLng?.latitude
        mobiUser.longitude = _userLocation.value?.latLng?.longitude
        val document = firestore.collection("mobifindUsers").document(mobiUser.phoneNumber)
        // mobiUser.userId = document.id
        document.collection("details").document(mobiUser.phoneNumber).set(mobiUser)
            .addOnSuccessListener {
                savePhotos(mobiUser,photo,user)
            }
            .addOnFailureListener{
                Log.d("Firebase", "save: failed")
            }
    }

    private fun savePhotos(
        mobiUser: MobifindUser,
        photo: Photo,
        user1: FirebaseUser
    ) {
        firestore.collection("mobifindUsers")
            .document(mobiUser.phoneNumber)
            .collection("photos").document(mobiUser.phoneNumber)
            .set(photo).addOnSuccessListener {
            uploadPhotos(mobiUser,photo,user1)
        }
    }

    private fun uploadPhotos(
        mobiUser: MobifindUser,
        photo: Photo,
        user1: FirebaseUser
    ) {
        val uri = Uri.parse(photo.localUri)
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

    private fun uploadPhotoDatabase(mobiUser: MobifindUser, photo: Photo) {
        firestore.collection("mobifindUsers").document(mobiUser.phoneNumber)
            .collection("photos")
            .document(mobiUser.phoneNumber).set(photo).addOnSuccessListener {
                _uploadStatus.value = photo.remoteUri
                mobiUser.photoUri = photo.remoteUri
                setPhotoInDetails(photo.remoteUri,mobiUser)

            }
    }


    private fun setPhotoInDetails(photoUri : String, mobiUser: MobifindUser){
        firestore.collection("mobifindUsers").document(mobiUser.phoneNumber)
            .collection("details")
            .document(mobiUser.phoneNumber).update("photoUri",photoUri).addOnSuccessListener{
                Log.d("MobifindViewmodel", "setPhotoInDetails: Photo in details")
            }
    }




}