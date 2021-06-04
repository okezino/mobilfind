package com.decagon.mobifind.model.data

import java.util.*

data class Photo(
    var localUri: String = "",
    var remoteUri: String = "",
    var dateTaken: Date = Date()
)
