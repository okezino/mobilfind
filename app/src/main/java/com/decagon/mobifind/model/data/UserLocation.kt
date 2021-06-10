package com.decagon.mobifind.model.data

import android.location.Address
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserLocation(
    val latLng: LatLng,
    val address: MutableList<Address>? = null,
    var time: String? = null
): Parcelable