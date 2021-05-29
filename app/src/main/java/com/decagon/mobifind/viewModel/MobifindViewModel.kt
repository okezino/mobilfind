package com.decagon.mobifind.viewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.decagon.mobifind.model.data.MobifindUser
import com.decagon.mobifind.model.data.Photo
import com.decagon.mobifind.model.data.Track
import com.decagon.mobifind.model.data.UserLocation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage

class MobifindViewModel : ViewModel() {
    private var firestore : FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var documentReference : DocumentReference
    private var storageReference = FirebaseStorage.getInstance().reference
    private lateinit var phoneNumber : String
    private lateinit var firebaseUser: FirebaseUser

    private var _userLocation = MutableLiveData<UserLocation>()
    val userLocation : LiveData<UserLocation>
    get() = _userLocation

    private var _uploadStatus = MutableLiveData<String>()
    val uploadStatus : LiveData<String>
        get() = _uploadStatus

    private var _mobifindUsers = MutableLiveData<ArrayList<String>>()
    val mobifindUser : LiveData<ArrayList<String>>
        get() = _mobifindUsers

    private var _trackers = MutableLiveData<ArrayList<String>>()
    val trackers : LiveData<ArrayList<String>>
        get() = _trackers

    private var _isSignedUpSuccess = MutableLiveData<Boolean>()
    val isSignedUp : LiveData<Boolean>
    get() = _isSignedUpSuccess

    init {
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
    }

    fun setUpFirebaseUser(user : FirebaseUser){
        firebaseUser = user
        phoneNumber = firebaseUser.phoneNumber!!
        documentReference = firestore.collection("mobifindUsers")
            .document(phoneNumber)
    }

    fun saveUserLocationUpdates(userLocation: UserLocation){
        _userLocation.value = userLocation
    }


    fun signUpUserWithoutPhoto(mobiUser: MobifindUser){
        documentReference.collection("details").document(phoneNumber).set(saveUser(mobiUser))
            .addOnSuccessListener {
                _isSignedUpSuccess.value = true
            }
            .addOnFailureListener{
                Log.d("Firebase", "save: failed")
            }
    }


    fun save(mobiUser: MobifindUser, photo: Photo, user: FirebaseUser) {
        documentReference.collection("details").document(phoneNumber).set(saveUser(mobiUser))
            .addOnSuccessListener {
                savePhotos(mobiUser,photo,user)
            }
            .addOnFailureListener{
                Log.d("Firebase", "save: failed")
            }
    }

    private fun saveUser(mobiUser: MobifindUser) : MobifindUser{
        documentReference.set(Track(phoneNumber= phoneNumber))
        mobiUser.latitude = _userLocation.value?.latLng?.latitude
        mobiUser.longitude = _userLocation.value?.latLng?.longitude
        return mobiUser
    }



    private fun savePhotos(
        mobiUser: MobifindUser,
        photo: Photo,
        user1: FirebaseUser
    ) {
            documentReference.collection("photos").document(phoneNumber)
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
       documentReference.collection("photos")
            .document(phoneNumber).set(photo).addOnSuccessListener {
                _uploadStatus.value = photo.remoteUri
                mobiUser.photoUri = photo.remoteUri

                setPhotoInDetails(photo.remoteUri)

            }
    }


    private fun setPhotoInDetails(photoUri : String){
        documentReference.collection("details")
            .document(phoneNumber).update("photoUri",photoUri).addOnSuccessListener{
                Log.d("MobifindViewmodel", "setPhotoInDetails: Photo in details")
            }
    }


    fun getAllMobifindUsers() {
        firestore.collection("mobifindUsers")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w("Listening", "listenToSpecimens: Listen Failed")
                    return@addSnapshotListener
                }
                if (value != null) {
                    val allMobifindUser = ArrayList<String>()
                    val documents = value.documents
                    documents.forEach {
                        val mobifindUser = it.id
                            //  mobifindUser.userId = it.id
                            allMobifindUser.add(mobifindUser.toString())
                        }
                        _mobifindUsers.value = allMobifindUser
                    }
                }
            }


    fun pushToTrackers(tracker : Track){
        documentReference.collection("trackers")
            .document(phoneNumber)
            .set(tracker).addOnSuccessListener {
                Log.d("PushTracker", "pushToTrackers: ${tracker.name} successfully added")
            }
    }

    fun pushToTracking(tracker: Track){
        documentReference.collection("tracking")
            .document(phoneNumber)
            .set(tracker).addOnSuccessListener {
                Log.d("PushTracker", "pushToTrackers: ${tracker.name} successfully added")
            }
    }

    fun readFromTrackers(){
        documentReference.collection("trackers").addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w("Listening", "listenToSpecimens: Listen Failed")
                    return@addSnapshotListener
                }
                if (value != null) {
                    val allMobifindUser = ArrayList<String>()
                    val documents = value.documents
                    documents.forEach {
                        val mobifindUser = it.toObject(Track::class.java)
                        //  mobifindUser.userId = it.id
                        mobifindUser?.name?.let { it1 -> allMobifindUser.add(it1) }
                    }
                    _trackers.value = allMobifindUser
                }
            }
            }
    }
