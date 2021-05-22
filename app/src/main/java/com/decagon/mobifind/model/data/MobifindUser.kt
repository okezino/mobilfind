package com.decagon.mobifind.model.data

data class MobifindUser(var latitude : Double = 0.0,
var longitude : Double = 0.0,
var phoneNumber : String = "",
var userName : String = "",
var userId : String = ""){
    override fun toString(): String {
        return "$userName $phoneNumber"
    }
}