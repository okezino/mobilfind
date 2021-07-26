package com.decagon.mobifind.viewModel

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.decagon.mobifind.model.data.*
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.decagon.mobifind.model.data.TrackState.*
import com.google.firebase.firestore.*
import java.time.LocalDateTime

class MobifindViewModel : ViewModel() {
    private var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var documentReference: DocumentReference
    private lateinit var userDocumentReference: DocumentReference
    private var storageReference = FirebaseStorage.getInstance().reference
    lateinit var phoneNumber: String
    private lateinit var firebaseUser: FirebaseUser

    private val _isTrackerDeleted = MutableLiveData<Boolean>()
    val isTrackerDeleted = _isTrackerDeleted as LiveData<Boolean>

    private val _currentUserName = MutableLiveData<String>()
    val currentUserName = _currentUserName as LiveData<String>

    private var _userLocation = MutableLiveData<UserLocation>()
    val userLoc = _userLocation as LiveData<UserLocation>

    private var _uploadStatus = MutableLiveData<String?>(null)
    val uploadStatus : LiveData<String?>
        get() = _uploadStatus

    private var _mobifindUsers = MutableLiveData<ArrayList<String>>()
    val mobifindUser: LiveData<ArrayList<String>>
        get() = _mobifindUsers

    private var _photoUri = MutableLiveData<String?>()
    val photoUri: LiveData<String?>
        get() = _photoUri

    private var _response = MutableLiveData<Boolean>()

    private var _isSignedUpSuccess = MutableLiveData<Boolean>()
    val isSignedUp: LiveData<Boolean>
        get() = _isSignedUpSuccess

    private val _tracking = MutableLiveData<List<Track>>(emptyList())
    val tracking = _tracking as LiveData<List<Track>>

    private val _myTrackers = MutableLiveData<List<Track>>(emptyList())
    val myTrackers = _myTrackers as LiveData<List<Track>>
    val Tag = "MobifindViewModel"

    init {
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
    }

    fun setUpFirebaseUser(user: FirebaseUser) {
        firebaseUser = user
        phoneNumber = firebaseUser.phoneNumber!!
        documentReference = firestore.collection("mobifindUsers")
            .document(phoneNumber)
    }

    fun setUpUserFirebase(phoneNumber: String) {
        userDocumentReference = firestore.collection("mobifindUsers")
            .document(phoneNumber)
    }

    fun saveUserLocationUpdates(userLocation: UserLocation) {
        _userLocation.value = userLocation
        Log.d("MobifindViewModel", "saveUserLocationUpdates: ${userLocation.latLng.latitude}")
    }


    fun signUpUserWithoutPhoto(mobiUser: MobifindUser) {
        documentReference.collection("details").document(phoneNumber).set(saveUser(mobiUser))
            .addOnSuccessListener {
                _isSignedUpSuccess.value = true
            }
            .addOnFailureListener {
            }
    }


    fun save(mobiUser: MobifindUser, photo: Photo, user: FirebaseUser) {
        documentReference.collection("details").document(phoneNumber).set(saveUser(mobiUser))
            .addOnSuccessListener {
                savePhotos(mobiUser, photo, user)
            }
            .addOnFailureListener {

            }
    }

    private fun saveUser(mobiUser: MobifindUser): MobifindUser {
        documentReference.set(Track(phoneNumber = phoneNumber))
        mobiUser.latitude = _userLocation.value?.latLng?.latitude
        mobiUser.longitude = _userLocation.value?.latLng?.longitude
        mobiUser.time = _userLocation.value?.time
        return mobiUser
    }

    private fun savePhotos(
        mobiUser: MobifindUser,
        photo: Photo,
        user1: FirebaseUser
    ) {
        documentReference.collection("photos").document(phoneNumber)
            .set(photo).addOnSuccessListener {
                uploadPhotos(mobiUser, photo, user1)
            }
    }

    private fun uploadPhotos(
        mobiUser: MobifindUser,
        photo: Photo,
        user1: FirebaseUser
    ) {
        val uri = Uri.parse(photo.localUri)
        val imageRef = storageReference.child("images/" + user1.uid + "/" + uri.lastPathSegment)
        val uploadTask = imageRef.putFile(uri)
        uploadTask.addOnSuccessListener {
            val downloadUrl = imageRef.downloadUrl
            downloadUrl.addOnSuccessListener {
                photo.remoteUri = it.toString()
                //Update our cloud firestore with the public image URI
                uploadPhotoDatabase(mobiUser, photo)
            }
        }
        uploadTask.addOnFailureListener {}
    }

    private fun uploadPhotoDatabase(mobiUser: MobifindUser, photo: Photo) {
        clearUploadStatus()
       documentReference.collection("photos")
            .document(phoneNumber).set(photo).addOnSuccessListener {
                _uploadStatus.value = photo.remoteUri
                mobiUser.photoUri = photo.remoteUri

                setPhotoInDetails(photo.remoteUri)
            }
        clearUploadStatus()
    }

    private fun setPhotoInDetails(photoUri: String) {
        documentReference.collection("details")
            .document(phoneNumber).update("photoUri", photoUri).addOnSuccessListener {
            }
    }


    fun clearUploadStatus() {
        _uploadStatus.value = null
    }

    fun getTrackerPhotoInPhotos(number: String, name: String): Boolean {
        var response = true
        userDocumentReference.collection("photos")
            .document(number).addSnapshotListener { value, error ->
                val photo = value?.data
                if (photo != null) {
                            val photoShot: String? = if (photo["remoteUri"] != null) photo["remoteUri"].toString() else null
                            val tracker = Track(name, number, photoShot)
                            response = pushToTrackers(tracker)
                        } else{
                    val tracker = Track(name, number)
                    response = pushToTrackers(tracker)
                }
            }
        return response
    }

    fun getCurrentUserName() {
        documentReference.collection("details")
            .document(phoneNumber).get().addOnSuccessListener {
                val user = it.data
                if (user != null) {
                    user["name"]?.let {
                        _currentUserName.value = it.toString()
                        return@addOnSuccessListener
                    }
                }
            }
    }

    // Check if a userDocumentReference has been initialized
    fun isDocumentRefInitialized(): Boolean = this::documentReference.isInitialized

    // Method to set the photoUri value to null
    fun setPhotoUriToNull() { _photoUri.value = null }

    fun getPhotoInPhotos() {
        documentReference.collection("photos")
            .document(phoneNumber).addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null) {
                    val photo = value.data
                    if (photo != null) {
                        _photoUri.value = photo["remoteUri"].toString()
                        return@addSnapshotListener
                    }
                }
            }
    }


    fun getAllMobifindUsers() {
        firestore.collection("mobifindUsers")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null) {
                    val allMobifindUser = ArrayList<String>()
                    val documents = value.documents
                    documents.forEach {
                        val mobifindUser = it.id
                        allMobifindUser.add(mobifindUser)
                    }
                    _mobifindUsers.value = allMobifindUser
                }
            }
    }

    // Method for setting trackers  in database
    private fun pushToTrackers(tracker: Track): Boolean {
        var result: Boolean = true
        documentReference.collection("trackers")
            .document(tracker.phoneNumber!!)
            .set(tracker).addOnSuccessListener {
                _response.value = true
            }.addOnFailureListener {
                _response.value = false
            }

        return result
    }


    // Method for setting tracking  in database
    fun pushToTracking(photo: String?) {
        val currentDateTime = System.currentTimeMillis()
        var tracker = Track(currentUserName.value, phoneNumber, photo, currentDateTime.toString())
        userDocumentReference.collection("tracking")
            .document(phoneNumber)
            .set(tracker).addOnSuccessListener {

            }.addOnFailureListener {

            }
    }

    // Method for getting trackers and tracking from database
    fun getTrackList(path: TrackState) {
        documentReference.collection(path.state)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null) {
                    val trackList = mutableListOf<Track>()
                    value.documents.forEach { snapshot ->
                        val track = snapshot.toObject(Track::class.java)
                        track?.let { trackList.add(it) }
                    }
                    when (path.state) {
                        TRACKERS.state -> _myTrackers.value = trackList
                        TRACKING.state -> _tracking.value = trackList
                    }
                }
            }
    }

    // Method for deleting tracker from trackers list and the user from the tracker's
    fun deleteFromTrackers(tPhoneNumber: String) {
        documentReference.collection(TRACKERS.state).document(tPhoneNumber).delete()
            .addOnSuccessListener {
                firestore.collection("mobifindUsers").document(tPhoneNumber)
                    .collection(TRACKING.state).document(phoneNumber).delete()
                    .addOnSuccessListener { _isTrackerDeleted.value = true }
            }
            .addOnFailureListener {
                _isTrackerDeleted.value = false
            }
    }

    fun updateLocationDetails(){
        documentReference.collection("details")
            .document(phoneNumber).update("longitude", _userLocation.value?.latLng?.longitude,
                "latitude",_userLocation.value?.latLng?.latitude,
                "time", _userLocation.value?.time)
            .addOnSuccessListener {
            }
    }
}