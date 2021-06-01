package com.decagon.mobifind.viewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.decagon.mobifind.model.data.*
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import com.decagon.mobifind.model.data.TrackState.*

class MobifindViewModel : ViewModel() {
    private var firestore : FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var documentReference : DocumentReference
    private lateinit var userDocumentReference : DocumentReference
    private var storageReference = FirebaseStorage.getInstance().reference
    lateinit var phoneNumber : String
    private lateinit var firebaseUser: FirebaseUser

    private val _isTrackerDeleted = MutableLiveData<Boolean>()
    val isTrackerDeleted = _isTrackerDeleted as LiveData<Boolean>

    private var _userLocation = MutableLiveData<UserLocation>()
    val userLocation : LiveData<UserLocation>
    get() = _userLocation

    private var _uploadStatus = MutableLiveData<String>()
    val uploadStatus : LiveData<String>
        get() = _uploadStatus

    private var _mobifindUsers = MutableLiveData<ArrayList<String>>()
    val mobifindUser : LiveData<ArrayList<String>>
        get() = _mobifindUsers

    private var _photoUri = MutableLiveData<String?>()
    val photoUri : LiveData<String?>
        get() = _photoUri

    private var _trackerPhotoUri = MutableLiveData<String?>()
    val trackerPhotoUri : LiveData<String?>
        get() = _trackerPhotoUri

    private var _trackers = MutableLiveData<ArrayList<String>>()
    val trackers : LiveData<ArrayList<String>>
        get() = _trackers

    private var _response = MutableLiveData<String>()
    val response : LiveData<String>
        get() = _response

    private var _contactList = MutableLiveData<ArrayList<Contact>>()
    val contactList : LiveData<ArrayList<Contact>>
        get() = _contactList

    private var _isSignedUpSuccess = MutableLiveData<Boolean>()
    val isSignedUp : LiveData<Boolean>
    get() = _isSignedUpSuccess

    private val _tracking = MutableLiveData<List<Track>>(emptyList())
    val tracking = _tracking as LiveData<List<Track>>

    private val _myTrackers = MutableLiveData<List<Track>>(emptyList())
    val myTrackers = _myTrackers as LiveData<List<Track>>

    init {
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
    }

    fun setUpFirebaseUser(user : FirebaseUser){
        firebaseUser = user
        phoneNumber = firebaseUser.phoneNumber!!
        documentReference = firestore.collection("mobifindUsers")
            .document(phoneNumber)
    }

    fun setUpUserFirebase(phoneNumber : String){
        userDocumentReference = firestore.collection("mobifindUsers")
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

    fun getTrackerPhotoInPhotos(phoneNumber: String) {
        userDocumentReference.collection("photos")
            .document(phoneNumber).get().addOnSuccessListener {
                val photo = it.data!!
                for (i in photo.keys) {
                    if (i == "remoteUri") {
                        _trackerPhotoUri.value = photo[i].let { photo[i].toString() }
                        break
                    }
                }
            }

    }

    fun getPhotoInPhotos() {
        documentReference.collection("photos")
            .document(phoneNumber).addSnapshotListener { value, error ->
                if (error != null) {
                    Log.d("Error getting photo", error.message!!)
                    return@addSnapshotListener
                }
                if (value != null) {
                    val photo = value.data!!
                    for (i in photo.keys) {
                        if (i == "remoteUri") {
                            _photoUri.value = photo[i].toString()
                            return@addSnapshotListener
                        }
                    }
                }
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
            .document(tracker.phoneNumber!!)
            .set(tracker).addOnSuccessListener {
                _response.value = "${tracker.name} has been given permission to Track you"
            }.addOnFailureListener {
                _response.value = "Error: Fail to add "
            }

    }


    fun pushToTracking(photo:String?){
        var tracker = Track("myself",phoneNumber,photo)
        userDocumentReference.collection("tracking")
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

        fun updateContactList(list :ArrayList<Contact>) {
            _contactList.value = list
        }

    // Method for getting trackers and tracking from database
    fun getTrackList(path: TrackState) {
        documentReference.collection(path.state)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w("Listening", "listenToSpecimens: Listen Failed")
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

    fun deleteFromTrackers(phoneNumber: String) {
        documentReference.collection(TRACKERS.state).document(phoneNumber).delete()
            .addOnSuccessListener { _isTrackerDeleted.value = true }
            .addOnFailureListener {
                _isTrackerDeleted.value = false
                Log.d("ERROR DELETING TRACKER", "${it.message}")
            }
    }
}