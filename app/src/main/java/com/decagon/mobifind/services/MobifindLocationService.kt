package com.decagon.mobifind.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.decagon.mobifind.MainActivity
import com.decagon.mobifind.R
import com.decagon.mobifind.model.data.MobifindUser
import com.decagon.mobifind.model.data.UserLocation
import com.decagon.mobifind.utils.*
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.*
import java.text.SimpleDateFormat
import java.util.*

class MobifindLocationService : LifecycleService()  {

    private val channelId = "12345"
    private var chanCount = 1234
    private val description = "Test Notification"
    private lateinit var builder: Notification.Builder
    // Used only for storage of the last known location.
    private lateinit var currentLocation : Location

    // LocationCallback - Called when FusedLocationProviderClient has a new Location.
    private lateinit var locationCallback: LocationCallback

    // LocationRequest - Requirements for the location updates, i.e., how often you should receive
    // updates, the priority, etc.
    private lateinit var locationRequest: LocationRequest

    // FusedLocationProviderClient - Main class for receiving location updates.
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var serviceRunningInForeground = false
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationChannel : NotificationChannel


    companion object {
        private const val TAG = "MobifindLocationService"

        private const val PACKAGE_NAME = "com.decagon.mobifind"

        internal const val ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST =
            "$PACKAGE_NAME.action.FOREGROUND_ONLY_LOCATION_BROADCAST"

        internal const val EXTRA_LOCATION = "$PACKAGE_NAME.extra.LOCATION"

        private const val NOTIFICATION_ID = 12345678

        private const val NOTIFICATION_CHANNEL_ID = "mobifind_channel_01"
    }

    override fun onCreate() {
        super.onCreate()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        getLocationUpdates()

        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        var fore = SharedPreferenceUtil.getPhoneNumber(this@MobifindLocationService)
        locationCallback = object : LocationCallback() {
            @SuppressLint("SimpleDateFormat")
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                currentLocation = p0.lastLocation
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                val currentLatLng = LatLng(currentLocation.latitude, currentLocation.longitude)
                val userLocation = UserLocation(currentLatLng, time = dateFormat.format(Date()).toString())

                // Updates notification content if this service is running as a foreground
                // service.
                if (serviceRunningInForeground) {
                    notificationManager.notify(
                        NOTIFICATION_ID,
                        generateNotification())
                }
                fore = SharedPreferenceUtil.getPhoneNumber(this@MobifindLocationService)
                if(fore != null){
                    val updateDetails = mapOf("latitude" to userLocation.latLng.latitude,"longitude" to userLocation.latLng.longitude,"time" to userLocation.time)
                    firestore.collection("mobifindUsers")
                        .document(fore!!).collection("details").document(fore!!).update(
                            updateDetails)
                }

            }
        }

        /**
         * Listen to firebase change and send User notification
         */
        fore?.let {
            firestore.collection("mobifindUsers").document(it).collection("tracking")
                .addSnapshotListener { value, error ->
                    fore?.let { currentUser ->
                        firestore.collection("mobifindUsers").document(it).collection("details")
                            .document(currentUser).get().addOnSuccessListener { doc ->
                                val track = doc.toObject(MobifindUser::class.java)
                                displayNotification(fore, value, track!!.trackListNum)

                            }
                    }

                }
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
            val notification = generateNotification()
            startForeground(NOTIFICATION_ID, notification)
            serviceRunningInForeground = true

        getLocationUpdates()
        Log.d(TAG, "onStartCommand()")

        SharedPreferenceUtil.saveLocationTrackingPref(this, true)

        // Binding to this service doesn't actually trigger onStartCommand(). That is needed to
        // ensure this Service can be promoted to a foreground service, i.e., the service needs to
        // be officially started (which we do here).
       // startService(Intent(applicationContext, MobifindLocationService::class.java))

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper())
        } catch (unlikely: SecurityException) {
            SharedPreferenceUtil.saveLocationTrackingPref(this, false)
        }

        // Tells the system to recreate the service after it's been killed.
        return START_STICKY
    }

    private fun getLocationUpdates() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 10f
        }


        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            startLocationUpdates()
        }

        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    e.startResolutionForResult(MainActivity.activity["ACTIVITY"]!!, LOCATION_UPDATE_STATE)
                } catch (sendEx: IntentSender.SendIntentException) {

                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            stopSelf()
            return
        }
        /*
        The Looper object whose message queue will be used to implement the callback mechanism, location
        request to make the request and callback for the location updates
         */
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        Log.d("Services", "getLocationUpdates: ")

    }


    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun generateNotification(): Notification {

        // Gets data
        val mainNotificationText = getString(R.string.no_location_text)
        val titleText = getString(R.string.app_name)

        // Creates Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, titleText, NotificationManager.IMPORTANCE_LOW)
            notificationChannel.setSound(null,null)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // Sets up main Intent/Pending Intents for notification
        val launchActivityIntent = Intent(this, MainActivity::class.java)

        val colour = resources.getColor(R.color.status_bar)

        val activityPendingIntent = PendingIntent.getActivity(
            this, 0, launchActivityIntent, 0)

        // Builds and issues the notification
        val notificationCompatBuilder =
            NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)

        return notificationCompatBuilder
            .setContentTitle(titleText)
            .setContentText(mainNotificationText)
            .setSmallIcon(R.drawable.ic_logo_name)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(activityPendingIntent)
            .setColor(colour)
            .setSound(null)
            .build()
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    fun notificationAlert(name: String) : Notification{
        // Creates Notification Channel
               chanCount++
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationChannel = NotificationChannel(
                channelId, description, NotificationManager.IMPORTANCE_LOW)
            notificationChannel.setSound(null,null)
            notificationManager.createNotificationChannel(notificationChannel)
        }

                // Sets up main Intent/Pending Intents for notification
        val launchActivityIntent = Intent(this, MainActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(
            this, 0, launchActivityIntent, 0)

        val colour = resources.getColor(R.color.status_bar)

        // Builds and issues the notification
        val notificationCompatBuilder =
            NotificationCompat.Builder(applicationContext, channelId)

         return notificationCompatBuilder
            .setContentTitle(TRACKER_ALERT)
            .setContentText(alertMessage(name))
            .setSmallIcon(R.drawable.ic_baseline_circle_notifications_24)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(activityPendingIntent)
            .setColor(colour)
            .setSound(null)
            .build()




    }

    private fun updateTackList(fore: String?, list:Int){

        fore?.let {
            firestore.collection("mobifindUsers").document(it).collection("details")
                .document(it).update("trackListNum", list)
        }
    }

    private fun displayNotification(fore : String?, queryValue : QuerySnapshot? , currentValue : Int ){
        if (queryValue != null) {
            if(queryValue.documents.size > currentValue){

                fore?.let {
                    firestore.collection("mobifindUsers").document(it).collection("tracking")
                        .orderBy("timestamp",Query.Direction.DESCENDING).get().addOnSuccessListener {
                            val name =  it.documents[0].get("name")
                            notificationManager.notify(
                                chanCount,
                                notificationAlert(name.toString()))

                        }
                }
                updateTackList(fore, queryValue.documents.size)
            }else{

                updateTackList(fore,queryValue.documents.size)
            }

        }
    }


}