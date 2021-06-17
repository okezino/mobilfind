package com.decagon.mobifind.utils

import android.annotation.SuppressLint
import android.content.Context
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
import com.bumptech.glide.request.RequestOptions
import com.decagon.mobifind.MyAccessibilityService
import com.decagon.mobifind.R
import com.decagon.mobifind.adapter.PhoneContactAdapter
import com.decagon.mobifind.adapter.UserAdapter
import com.decagon.mobifind.model.data.ForegroundData
import com.decagon.mobifind.model.data.UserLocation
import com.google.android.material.snackbar.Snackbar
import com.google.type.LatLng
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private val ACCESSIBILITY_ENABLED = 1

fun View.showSnackBar(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT)
        .show()
}

fun View.showToast(message: String) {
    Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
}

fun ImageView.load(imageUrl: String) {
    Glide.with(this.context)
        .load(imageUrl).apply(
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
    return "Hello $name \n check out this cool location tracker app using the below Link +++"
}

fun denyMessage():String{
    return "Manually give Mobifind permission to your Contact, to enable you add Trackers to your list"
}
fun sendSuccessMessage(name: String):String{
    return "$name has been successfully added to Tracker List"
}

fun failedMessage():String{
    return "Failed Operation: Try again"
}

fun searchContact(s:String, contact:ArrayList<String>) : List<String>{
    return  contact.filter { it.contains(s, ignoreCase = true)}
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
 * Returns the `location` object as a human readable string.
 */
fun Location?.toText(): String {
    return if (this != null) {
        "($latitude, $longitude)"
    } else {
        "Unknown location"
    }
}

/**
 * Provides access to SharedPreferences for location to Activities and Services.
 */
internal object SharedPreferenceUtil {

    const val KEY_FOREGROUND_ENABLED = "tracking_foreground_location"
    const val LOCATION_LATITUDE = "Location_latitude"
    const val LOCATION_LONGITUDE = "Location_longitude"
    const val DISPLAY_NAME = "displayName"
    const val PHONE_NUMBER = "phoneNumber"

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The [Context].
     */
    fun getLocationTrackingPref(context: Context): Boolean =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            .getBoolean(KEY_FOREGROUND_ENABLED, false)

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    fun saveLocationTrackingPref(context: Context, requestingLocationUpdates: Boolean) =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE).edit {
            putBoolean(KEY_FOREGROUND_ENABLED, requestingLocationUpdates)
            apply()
        }

    fun saveDisplayNamePref(context: Context,displayName : String, phoneNumber : String){
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE).edit {
            putString(DISPLAY_NAME, displayName)
            putString(PHONE_NUMBER, phoneNumber)
            apply()
        }

    }

    fun getDisplayName(context: Context) : ForegroundData{
        val name = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE).getString(
            DISPLAY_NAME, null)
        val phoneNumber = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE).getString(
            PHONE_NUMBER, null)
        return ForegroundData(name,phoneNumber)
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
}