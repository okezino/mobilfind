package com.decagon.mobifind.model.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Track(
    var name : String? = null,
    var phoneNumber : String? = null,
    var photoUri : String? = null
) : Parcelable
