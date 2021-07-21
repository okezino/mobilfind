package com.decagon.mobifind.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.location.Location
import android.provider.Settings
import android.text.TextUtils
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.edit
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.decagon.mobifind.MyAccessibilityService
import com.decagon.mobifind.R
import com.decagon.mobifind.adapter.UserAdapter
import com.decagon.mobifind.model.data.Track
import com.google.android.material.snackbar.Snackbar
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val ACCESSIBILITY_ENABLED = 1

fun View.showSnackBar(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT)
        .show()
}

fun View.showSnackBar(message: String, color : Int){
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT).setTextColor(color)
        .show()
}

fun View.showToast(message: String) {
    Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
}

fun ImageView.load(imageUrl: String) {
    Glide.with(this.context)
        .load(imageUrl).listener(object : RequestListener<Drawable>{
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }

        })
        .apply(
            RequestOptions()
                .placeholder(R.drawable.loading_status_animation)
                .error(R.drawable.ic_error_image)
        ).into(this)
}

fun initAdapter(adapter: UserAdapter, recyclerView: RecyclerView) {
    val divider = DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)
    recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
    recyclerView.adapter = adapter
    recyclerView.addItemDecoration(divider)
}

fun filterNumber(number: String): String {
    val result = if (number.take(1) == "+") number else number.replaceRange(0..0, "+234")
    return result.filter { it == '+' || it.isDigit() }
}

fun inviteMessage(name: String): String {
    return "Hi $name \nMobiFind helps you to get updated location of you and your love ones, download App by clicking the link below +++"
}
fun affirmationMessage(name:String) : String{
    return "Are you sure you want to add $name to trackers list?"
}

fun denyMessage():String{
    return "Manually give Mobifind permission to your Contact, to enable you add Trackers to your list"
}
fun alertMessage(name: String) : String =  "$name has been added to your Tracker List"

fun existingUserMessage(name: String) : String =  "$name is already on your Tracker List, Click   OK   to continue"

fun sendSuccessMessage(name: String):String{
    return "$name has been successfully added to Tracker List"
}

fun failedMessage():String{
    return "Failed Operation: Try again"
}

fun searchContact(s:String, contact:ArrayList<String>) : List<String>{
    return  contact.filter { it.contains(s, ignoreCase = true)}
}

fun validateUser(s:String, track :List<Track>) : Boolean{
    var result = false
    loo@for(i in track){
        if(i.phoneNumber == s) {
            result = true
            break@loo
        }
    }
    return result
}

fun isSignedUp(number: String, users: ArrayList<String>) = number in users


/**
 * Function to convert time to time ago. To be monitored...
 */
@SuppressLint("SimpleDateFormat")
fun timeConvert(string: String?): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    sdf.timeZone = TimeZone.getTimeZone("GMT+1")
    return try {
        val time = sdf.parse(string).time
        val now = System.currentTimeMillis()
        val ago =
            DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS)
        ago.toString()
    } catch (e: ParseException) {
        e.printStackTrace()
        System.currentTimeMillis().toString()
    }
}


/**
 * Provides access to SharedPreferences for location to Activities and Services.
 */
internal object SharedPreferenceUtil {
    const val PHONE_NUMBER = "phoneNumber"
    private const val name = "MOBIFINDSERVICE_KEY"
    private const val key = "MOBIFINDSERVICE_STATE"


    fun savePhoneNumberInSharedPref(context: Context, phoneNumber : String?){
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE).edit {
            putString(PHONE_NUMBER, phoneNumber)
            apply()
        }
    }


    fun getPhoneNumber(context: Context): String? {
        return context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        ).getString(
            PHONE_NUMBER, null
        )
    }

    // Function to check if accessibility is enabled for the app
    fun isAccessibilitySettingsOn(context: Context): Boolean {
        var accessibilityEnabled = 0
        val service: String = context.packageName
            .toString() + "/" + MyAccessibilityService::class.java.canonicalName
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                context.applicationContext.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (e: Settings.SettingNotFoundException) {
            Log.e("ACCESSIBILITY", "Error finding setting, default accessibility to not found: ")
        }
        val mStringColonSplitter = TextUtils.SimpleStringSplitter(':')
        if (accessibilityEnabled == ACCESSIBILITY_ENABLED) {
            val settingValue: String = Settings.Secure.getString(
                context.applicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            mStringColonSplitter.setString(settingValue)
            while (mStringColonSplitter.hasNext()) {
                val accessibilityService = mStringColonSplitter.next()
                if (accessibilityService.equals(service, ignoreCase = true)) {
                    return true
                }
            }
        }
        return false
    }

    fun setServiceState(context: Context, state: ServiceState) {
        val sharedPrefs = getPreferences(context)
        sharedPrefs.edit().let {
            it.putString(key, state.name)
            it.apply()
        }
    }

    fun getServiceState(context: Context): ServiceState {
        val sharedPrefs = getPreferences(context)
        val value = sharedPrefs.getString(key, ServiceState.STOPPED.name)
        return ServiceState.valueOf(value!!)
    }

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(name, 0)
    }
}

enum class ServiceState{
    STARTED,
    STOPPED
}


