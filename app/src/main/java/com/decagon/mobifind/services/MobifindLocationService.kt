package com.decagon.mobifind.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.decagon.mobifind.R
import com.decagon.mobifind.model.data.UserLocation
import com.decagon.mobifind.utils.LOCATION_UPDATE_STATE
import com.decagon.mobifind.utils.NOTIFICATION_CHANNEL_ID
import com.decagon.mobifind.utils.NOTIFICATION_CHANNEL_NAME
import com.decagon.mobifind.utils.NOTIFICATION_ID
import com.decagon.mobifind.viewModel.MobifindViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import java.text.SimpleDateFormat
import java.util.*

class MobifindLocationService : LifecycleService() {
    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mobifindViewModel : MobifindViewModel

    companion object {
        val pathPoints = MutableLiveData<UserLocation>()
    }



    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)

      //  mobifindViewModel = ViewModelProvider(applicationContext as Activity).get(MobifindViewModel::class.java)
        // Receives updates when device location changes

        locationCallback = object : LocationCallback() {
            @SuppressLint("SimpleDateFormat")
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                val currentLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                val userLocation = UserLocation(currentLatLng, time = dateFormat.format(Date()).toString())
                pathPoints.postValue(userLocation)
            //    mobifindViewModel.saveUserLocationUpdates(userLocation)
                Log.d("Servicces", "onCreate: Services called ${currentLatLng.latitude}")
                Toast.makeText(applicationContext, "Location received: " + currentLatLng.latitude, Toast.LENGTH_SHORT).show();
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        getLocationUpdates()

        return START_STICKY
    }

    private fun getLocationUpdates() {
        locationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.smallestDisplacement = 10f

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
                    e.startResolutionForResult(applicationContext as Activity, LOCATION_UPDATE_STATE)
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
//            requestPermissions(
//                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                GET_LOCATION_UPDATE
//            )
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

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Services", "stopped: ")
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun startForegroundService() {

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("Running App")
            .setContentText("00:00:00")
          //  .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

//    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
//        this,
//        0,
//        Intent(this, MainActivity::class.java).also {
//            it.action = ACTION_SHOW_TRACKING_FRAGMENT
//        },
//        FLAG_UPDATE_CURRENT
//    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}