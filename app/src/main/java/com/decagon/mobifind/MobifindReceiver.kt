package com.decagon.mobifind

import android.content.*
import androidx.core.content.ContextCompat
import com.decagon.mobifind.services.NewMobifindService
import com.decagon.mobifind.utils.Actions

class MobifindReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action!! == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            intent.action!! == Intent.ACTION_BOOT_COMPLETED
        ) {
            Intent(context, NewMobifindService::class.java).also {
                it.action = Actions.START.name
                ContextCompat.startForegroundService(context!!,it)
            }
        }
    }

}