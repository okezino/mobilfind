package com.decagon.mobifind.services

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LifecycleService
import com.decagon.mobifind.utils.ACTION_PAUSE_SERVICE
import com.decagon.mobifind.utils.ACTION_START_OR_RESUME_SERVICE
import com.decagon.mobifind.utils.ACTION_STOP_SERVICE

class MobifindService : LifecycleService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_SERVICE -> {
                    Log.d("MobifindService", "onStartCommand: Started")
                }
                ACTION_PAUSE_SERVICE -> {
                    Log.d("MobifindService", "onPauseCommand: Paused")

                }
                ACTION_STOP_SERVICE -> {
                    Log.d("MobifindService", "onStopCommand: Stopped")

                }
                else -> ""
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
}