package com.decagon.mobifind.model.data

import android.location.Address
import com.google.android.gms.maps.model.LatLng

data class UserLocation(
    val latLng: LatLng,
    val address: MutableList<Address>
)