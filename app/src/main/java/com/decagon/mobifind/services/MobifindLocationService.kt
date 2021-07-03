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
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.decagon.mobifind.R
import com.decagon.mobifind.model.data.MobifindUser
import com.decagon.mobifind.model.data.UserLocation
import com.decagon.mobifind.utils.*
import com.decagon.mobifind.utils.SharedPreferenceUtil.setServiceState
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.*
import java.text.SimpleDateFormat
import java.util.*
import android.net.Uri

import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS

import android.os.PowerManager
import com.decagon.mobifind.MobifindReceiver
import android.app.AlarmManager

import android.os.Build

import android.app.PendingIntent
import com.decagon.mobifind.MainActivity
import java.util.concurrent.TimeUnit





class MobifindLocationService : LifecycleService()  {

  //  private var wakeLock: PowerManager.WakeLock? = null
   // private var isServiceStarted = false


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

    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationChannel : NotificationChannel

    /*
* Checks whether the bound activity has really gone away (foreground service with notification
* created) or simply orientation change (no-op).
*/
    private var configurationChange = false

    private var serviceRunningInForeground = false

    private val localBinder = LocalBinder()


    companion object {
        private const val TAG = "MobifindLocationService"

        private const val PACKAGE_NAME = "com.decagon.mobifind"

        internal const val ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST =
            "$PACKAGE_NAME.action.FOREGROUND_ONLY_LOCATION_BROADCAST"

        private const val EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION =
            "$PACKAGE_NAME.extra.CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION"

        internal const val EXTRA_LOCATION = "$PACKAGE_NAME.extra.LOCATION"

        private const val NOTIFICATION_ID = 1

        private const val NOTIFICATION_CHANNEL_ID = "mobifind_channel_01"

        private const val ALARM_REQUEST_CODE = 56745678
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)

        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        return localBinder
    }

    override fun onRebind(intent: Intent) {
        Log.d(TAG, "onRebind()")

        // MainActivity (client) returns to the foreground and rebinds to service, so the service
        // can become a background services.
        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d(TAG, "onUnbind()")

        // MainActivity (client) leaves foreground, so service needs to become a foreground service
        // to continue receiving location updates
        if (!configurationChange && SharedPreferenceUtil.getLocationTrackingPref(this)) {
            Log.d(TAG, "Start foreground service")
            val notification = generateNotification()
            startForeground(NOTIFICATION_ID, notification)
            serviceRunningInForeground = true
        }

        // Ensures onRebind() is called if MainActivity (client) rebinds.
        return true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configurationChange = true
    }


    /**
     * Class used for the client Binder.  This service runs in the same process as its
     * clients
     */
    inner class LocalBinder : Binder() {
        internal val service: MobifindLocationService
            get() = this@MobifindLocationService
    }

    override fun onCreate() {
        super.onCreate()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

      //  getLocationUpdates()
    //    acquireWakelock()
        restartService(this)

        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()

 

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
           val notification = generateNotification()
            startForeground(NOTIFICATION_ID, notification)
            serviceRunningInForeground = true

        var fore = SharedPreferenceUtil.getPhoneNumber(this@MobifindLocationService)


//
//        getLocationUpdates()
//        Log.d(TAG, "onStartCommand()")
//
//        SharedPreferenceUtil.saveLocationTrackingPref(this, true)
//
//        // Binding to this service doesn't actually trigger onStartCommand(). That is needed to
//        // ensure this Service can be promoted to a foreground service, i.e., the service needs to
//        // be officially started (which we do here).
//       // startService(Intent(applicationContext, MobifindLocationService::class.java))
//
//        try {
//            fusedLocationClient.requestLocationUpdates(
//                locationRequest, locationCallback, Looper.getMainLooper())
//        } catch (unlikely: SecurityException) {
//            SharedPreferenceUtil.saveLocationTrackingPref(this, false)
//        }

        //Never Ending Service
//        if(intent != null){
//            when(intent.action){
//                Actions.START.name -> startService()
//                Actions.STOP.name -> stopService()
//                else -> Log.d(TAG, "onStartCommand: This should never happen")
//            }
//        }else{
//            Log.d(
//                TAG,
//                "onStartCommand: with a null intent has probably been restarted by the system"
//            )
//        }

        val cancelLocationTrackingFromNotification =
            intent?.getBooleanExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, false)

        if (cancelLocationTrackingFromNotification == true) {
            unsubscribeToLocationUpdates()
            stopSelf()
        }


        // Tells the system to recreate the service after it's been killed.

        // Tells the system to recreate the service after it's been killed.
        return START_STICKY
    }



    fun subscribeToLocationUpdates() {
        Log.d(TAG, "subscribeToLocationUpdates()")
        val notification = generateNotification()
        startForeground(NOTIFICATION_ID, notification)
        serviceRunningInForeground = true

        SharedPreferenceUtil.saveLocationTrackingPref(this, true)

        // Binding to this service doesn't actually trigger onStartCommand(). That is needed to
        // ensure this Service can be promoted to a foreground service, i.e., the service needs to
        // be officially started (which we do here).
//        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
//            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MobifindService::lock").apply {
//                acquire(10*60*1000L /*10 minutes*/)
//            }
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(applicationContext,MobifindLocationService::class.java))
        } else startService(Intent(applicationContext, MobifindLocationService::class.java))

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper())
        } catch (unlikely: SecurityException) {
            SharedPreferenceUtil.saveLocationTrackingPref(this, false)
            Log.e(TAG, "Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }


    private fun unsubscribeToLocationUpdates() {
        Log.d(TAG, "unsubscribeToLocationUpdates()")

        try {
            // TODO: Step 1.6, Unsubscribe to location changes.
            val removeTask = fusedLocationClient.removeLocationUpdates(locationCallback)
            removeTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Location Callback removed.")
                    stopSelf()
                } else {
                    Log.d(TAG, "Failed to remove Location Callback.")
                }
            }
            SharedPreferenceUtil.saveLocationTrackingPref(this, false)
        } catch (unlikely: SecurityException) {
            SharedPreferenceUtil.saveLocationTrackingPref(this, true)
            Log.e(TAG, "Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }




    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
       // releaseWakelock()

    }

//    private fun startService(){
//        if(isServiceStarted) return
//        val notification = generateNotification()
//        startForeground(NOTIFICATION_ID, notification)
//        Log.d(TAG, "startService: Starting the foreground service task")
//        Toast.makeText(this, "Service starting its task", Toast.LENGTH_SHORT).show()
//        isServiceStarted = true
//        setServiceState(this, ServiceState.STARTED)
//
//        getLocationUpdates()
//

//
//        try {
//            fusedLocationClient.requestLocationUpdates(
//                locationRequest, locationCallback, Looper.getMainLooper())
//        } catch (unlikely: SecurityException) {
//            SharedPreferenceUtil.saveLocationTrackingPref(this, false)
//        }
//    }
//
//    private fun stopService(){
//        Log.d(TAG, "Stopping the foreground service")
//        Toast.makeText(this, "Service stopping", Toast.LENGTH_SHORT).show()
//        try {
//            wakeLock?.let {
//                if (it.isHeld) {
//                    it.release()
//                }
//            }
//            stopForeground(true)
//            stopSelf()
//        } catch (e: Exception) {
//            Log.d(TAG, "Service stopped without being started: ${e.message}")
//        }
//        isServiceStarted = false
//        setServiceState(this, ServiceState.STOPPED)
//    }



    private fun generateNotification(): Notification {

        // Gets data
        val mainNotificationText = getString(R.string.no_location_text)
        val titleText = getString(R.string.app_name)

//        // Creates Notification Channel
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//
//            val notificationChannel = NotificationChannel(
//                NOTIFICATION_CHANNEL_ID, titleText, NotificationManager.IMPORTANCE_LOW)
//            notificationChannel.setSound(null,null)
//            notificationManager.createNotificationChannel(notificationChannel)
//        }

        // Sets up main Intent/Pending Intents for notification
        val launchActivityIntent = Intent(this, MainActivity::class.java)

        val colour = resources.getColor(R.color.status_bar)

        val activityPendingIntent = PendingIntent.getActivity(
            this, 0, launchActivityIntent, 0)

        // Builds and issues the notification
        val notificationCompatBuilder =
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)

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




    // we need this lock so our service gets not affected by Doze Mode





    fun restartService(context: Context) {
//        val alarm = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        val intent = Intent(this, MobifindReceiver::class.java)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerInMillis,
//                    PendingIntent.getBroadcast(this, intent, PendingIntent.FLAG_UPDATE_CURRENT));
//
//            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                AlarmManager.AlarmClockInfo ac=
//                new AlarmManager.AlarmClockInfo(triggerInMillis, null);
//                alarm.setAlarmClock(ac, PendingIntent.getBroadcast(this, intent, PendingIntent.FLAG_UPDATE_CURRENT));
//
//            } else {
//                alarm.setExact(AlarmManager.RTC_WAKEUP, triggerInMillis,
//                    PendingIntent.getBroadcast(this, intent, PendingIntent.FLAG_UPDATE_CURRENT));
//            }
//    }
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val intent = Intent(this, MobifindReceiver::class.java)
        intent.action = "StartAlarm"
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            ALARM_REQUEST_CODE, intent, 0
        )
        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager!!.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime()
                        + TimeUnit.SECONDS.toMillis(295), pendingIntent)
        } else
            alarmManager!!.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()
                    + TimeUnit.SECONDS.toMillis(295), pendingIntent)

    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Toast.makeText(this, "removed", Toast.LENGTH_SHORT).show()
    }

}