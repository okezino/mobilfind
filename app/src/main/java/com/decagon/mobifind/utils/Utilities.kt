package com.decagon.mobifind.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import android.text.TextUtils
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.decagon.mobifind.MyAccessibilityService
import com.decagon.mobifind.R
import com.decagon.mobifind.adapter.UserAdapter
import com.decagon.mobifind.model.data.Track
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.shreyaspatil.MaterialDialog.MaterialDialog
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val ACCESSIBILITY_ENABLED = 1

fun View.showSnackBar(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT).setTextColor(resources.getColor(R.color.white))
        .setBackgroundTint(resources.getColor(R.color.dark_blue))
        .show()
}

fun View.showSnackBar(message: String, color : Int){
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT).setTextColor(color)
        .setBackgroundTint(resources.getColor(R.color.dark_blue))
        .show()
}


fun ImageView.load(imageUrl: String) {
    Glide.with(this.context)
        .load(imageUrl)
        .into(this)
}

fun initAdapter(adapter: UserAdapter, recyclerView: RecyclerView) {
    val divider = DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)
    recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
    recyclerView.adapter = adapter
    val drawable = ContextCompat.getDrawable(recyclerView.context,R.drawable.divider)
    drawable?.let {
        divider.setDrawable(it)
    }
    recyclerView.addItemDecoration(divider)
}

fun filterNumber(number: String): String {
    val result = if (number.take(1) == "+") number else number.replaceRange(0..0, "+234")
    return result.filter { it == '+' || it.isDigit() }
}

fun String.removeEmptySpace():String{

    return  this.filter { it != ' ' }
}

fun inviteMessage(name: String): String = "Hi $name \nMobiFind helps you to get updated location of you and your love ones, download App by clicking the link below $BASE_URL"

fun affirmationMessage(name:String) : String ="Are you sure you want to add $name to trackers list?"

fun logoutMessage():String = "Are you sure you want to Logout?"

fun exitMessage(): String = "This action will exit the application, Click YES to exit and NO to continue"

fun denyMessage():String = "Manually give Mobifind permission to your Contact, to enable you add Trackers to your list"

fun alertMessage(name: String) : String =  "$name has been added to your Tracker List"

fun existingUserMessage(name: String) : String =  "$name is already on your Tracker List, Click   OK   to continue"

fun sendSuccessMessage(name: String):String = "$name has been successfully added to Tracker List"

fun failedMessage():String ="Failed Operation: Try again"


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
    private const val onBoardViewed = "COMPLETED_ONBOARDING_PREF_NAME"


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

    fun setOnboardViewedState(context: Context, state : Boolean){
        val sharedPrefs = getPreferences(context)
        sharedPrefs.edit().let {
            it.putBoolean(onBoardViewed,state)
            it.apply()
        }
    }

    fun getOnboardViewedState(context: Context): Boolean {
        val sharedPrefs = getPreferences(context)
        return sharedPrefs.getBoolean(onBoardViewed, false)
    }

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(name, 0)
    }
}

enum class ServiceState{
    STARTED,
    STOPPED
}

// Function to get UserPhoto
fun getTrackerPhoto(phoneNumber: String, imageView: ImageView) {
    FirebaseFirestore.getInstance().collection("mobifindUsers").document(phoneNumber)
        .collection("photos").document(phoneNumber).addSnapshotListener { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (value != null) {
                val photo = value.data
                if (photo != null) {
                    imageView.load(photo["remoteUri"].toString())
                }
            }
        }
}

fun generateMaterialDialog(
    context: Activity, title: String, message: String
    , positiveBtnTitle: String, negativeBtnTitle: String = "",
    positiveAction: (() -> Unit)?, negativeAction: (() -> Unit)?
){
    MaterialDialog.Builder(context)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positiveBtnTitle) { dialogInterface, _ ->
            dialogInterface.dismiss()
            positiveAction?.invoke()
        }.setNegativeButton(negativeBtnTitle) { dialogInterface, _ ->
            dialogInterface.dismiss()
            negativeAction?.invoke()
        }.setCancelable(true)
        .build()
        .show()
}

fun generateMaterialDialog(
    context: Activity, title: String, message: String
    , positiveBtnTitle: String,
    positiveAction: (() -> Unit)?
){
    MaterialDialog.Builder(context)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positiveBtnTitle) { dialogInterface, _ ->
            dialogInterface.dismiss()
            positiveAction?.invoke()
        }.setCancelable(true)
        .build()
        .show()
}



