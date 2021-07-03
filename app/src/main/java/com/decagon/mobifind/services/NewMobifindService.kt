package com.decagon.mobifind.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.decagon.mobifind.App.Companion.CHANNEL_ID
import com.decagon.mobifind.MainActivity
import com.decagon.mobifind.R
import com.decagon.mobifind.model.data.MobifindUser
import com.decagon.mobifind.model.data.UserLocation
import com.decagon.mobifind.utils.*
import com.decagon.mobifind.utils.SharedPreferenceUtil
import com.decagon.mobifind.utils.SharedPreferenceUtil.setServiceState
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.*

class NewMobifindService : Service() {

     private var isServiceStarted = false

    // LocationCallback - Called when FusedLocationProviderClient has a new Location.
    private lateinit var locationCallback: LocationCallback

    // LocationRequest - Requirements for the location updates, i.e., how often you should receive
    // updates, the priority, etc.
    private lateinit var locationRequest: LocationRequest

    // FusedLocationProviderClient - Main class for receiving location updates.
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Used only for storage of the last known location.
    private lateinit var currentLocation : Location

    private lateinit var notificationManager: NotificationManager

    private val channelId = "12345"
    private var chanCount = 1234
    private val description = "Test Notification"

    private var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var fore: String? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = generateNotification()
        startForeground(1, notification)
        fore = SharedPreferenceUtil.getPhoneNumber(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getLocationUpdates()

        receiveLocationCallBack()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

      //  Never Ending Service
        if(intent != null){
            when(intent.action){
                Actions.START.name -> startService()
                Actions.STOP.name -> stopService()
                else -> Log.d("New Service", "onStartCommand: This should never happen")
            }
        }else{
            Log.d(
                "New Service",
                "onStartCommand: with a null intent has probably been restarted by the system"
            )
        }

        return START_STICKY
    }



    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        releaseWakelock()

    }

    private fun generateNotification(): Notification {

        // Gets data
        val mainNotificationText = getString(R.string.no_location_text)
        val titleText = getString(R.string.app_name)


        // Sets up main Intent/Pending Intents for notification
        val launchActivityIntent = Intent(this, MainActivity::class.java)

        val colour = resources.getColor(R.color.status_bar)

        val activityPendingIntent = PendingIntent.getActivity(
            this, 0, launchActivityIntent, 0)

        // Builds and issues the notification
        val notificationCompatBuilder =
            NotificationCompat.Builder(this, CHANNEL_ID)

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


    private fun startService(){
        if(isServiceStarted) return
//        val notification = generateNotification()
//        startForeground(1, notification)
        Log.d("NewService", "startService: Starting the foreground service task")
        Toast.makeText(this, "Service starting its task", Toast.LENGTH_SHORT).show()
        isServiceStarted = true
        setServiceState(this, ServiceState.STARTED)
        displayNewUserNotification()

        // we need this lock so our service gets not affected by Doze Mode
        acquireWakelock()

      //  getLocationUpdates()


//
//        try {
//            fusedLocationClient.requestLocationUpdates(
//                locationRequest, locationCallback, Looper.getMainLooper())
//        } catch (unlikely: SecurityException) {
//            SharedPreferenceUtil.saveLocationTrackingPref(this, false)
//        }
    }

    private fun stopService() {
        Toast.makeText(this, "Service stopping", Toast.LENGTH_SHORT).show()
        try {
           releaseWakelock()
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {

        }
        isServiceStarted = false
        setServiceState(this, ServiceState.STOPPED)
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

    private fun receiveLocationCallBack() {
        locationCallback = object : LocationCallback() {
            @SuppressLint("SimpleDateFormat")
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                currentLocation = p0.lastLocation
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                val currentLatLng = LatLng(currentLocation.latitude, currentLocation.longitude)
                val userLocation = UserLocation(currentLatLng, time = dateFormat.format(Date()).toString())
                if(fore != null){
                    Log.d("New Service", "sendLocationUpdates:  am called $fore ${userLocation.latLng.latitude}")
                    val updateDetails = mapOf("latitude" to userLocation.latLng.latitude,"longitude" to userLocation.latLng.longitude,"time" to userLocation.time)
                    firestore.collection("mobifindUsers")
                        .document(fore!!).collection("details").document(fore!!).update(
                            updateDetails).addOnSuccessListener {
                            Log.d("New Service", "onLocationResult: successfully updated")
                        }
                }

            }
        }
    }

    private fun displayNewUserNotification(){
        /**
         * Listen to firebase change and send User notification
         */
        fore?.let {
            Log.d("New Service", "onStartCommand: Notification")
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

    private fun updateTackList(fore: String?, list:Int){

        fore?.let {
            firestore.collection("mobifindUsers").document(it).collection("details")
                .document(it).update("trackListNum", list)
        }
    }

    private fun displayNotification(fore : String?, queryValue : QuerySnapshot?, currentValue : Int ){
        if (queryValue != null) {
            if(queryValue.documents.size > currentValue){

                fore?.let {
                    firestore.collection("mobifindUsers").document(it).collection("tracking")
                        .orderBy("timestamp", Query.Direction.DESCENDING).get().addOnSuccessListener {
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


    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = Intent(applicationContext, NewMobifindService::class.java).also {
            it.setPackage(packageName)
        };
        val restartServicePendingIntent: PendingIntent = PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        applicationContext.getSystemService(Context.ALARM_SERVICE);
        val alarmService: AlarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            alarmService.setExactAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent
            )
        }else alarmService.set(AlarmManager.ELAPSED_REALTIME,SystemClock.elapsedRealtime() + 1000,restartServicePendingIntent)
    }

    private val wakeLock: PowerManager.WakeLock by lazy {
        (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ServiceWakelock::lock").apply {
                acquire(10*60*1000L /*10 minutes*/)
            }
        }
    }

    private fun acquireWakelock() {
        try {
            wakeLock.let {
                wakeLock.setReferenceCounted(false)
                if (!wakeLock.isHeld) {
                    wakeLock.acquire(10*60*1000L /*10 minutes*/)
                }
            }
        } catch (e: RuntimeException) {
        }
    }

    private fun releaseWakelock() {
        try {
            wakeLock.let {
                if (it.isHeld) {
                    it.release()
                }
            }
        } catch (e: RuntimeException) {
        }
    }
}