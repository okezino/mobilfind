package com.decagon.mobifind.model.data

data class MobifindUser(
    var latitude: Double? = null,
    var longitude: Double? = null,
    var phoneNumber: String = "",
    var photoUri: String? = null,
    var name: String? = null,
    var time: String? = null,
    var trackListNum : Int = 0
)