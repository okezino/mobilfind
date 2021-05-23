package com.decagon.mobifind.model.data

import android.net.Uri

data class MobifindUser(var latitude : Double = 0.0,
var longitude : Double = 0.0,
var phoneNumber : String = "",
var userName : String = "",
var userId : String = "",
var photoUri : String = ""){
    override fun toString(): String {
        return "$userName $phoneNumber"
    }
}