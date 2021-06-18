package com.decagon.mobifind

import android.content.*
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.decagon.mobifind.services.MobifindLocationService

class MobifindReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action!! == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            intent.action!! == Intent.ACTION_BOOT_COMPLETED) {
            Intent(context, MobifindLocationService::class.java).also {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context!!.startForegroundService(it)
                    return
                }
                context!!.startService(it)
            }
        }
    }
}