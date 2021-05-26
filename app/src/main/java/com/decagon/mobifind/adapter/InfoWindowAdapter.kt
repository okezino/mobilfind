package com.decagon.mobifind.adapter

import android.app.Activity
import android.view.View
import android.widget.TextView
import com.decagon.mobifind.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

/**
 *  This class implements the GoogleMap.InfoWindowAdapter
 *  it is used to define a custom info window for the map
 */
class InfoWindowAdapter(context: Activity) :
    GoogleMap.InfoWindowAdapter {

    // Inflate the content of the map's info window with the
    // custom layout defined
    private val contents: View = context.layoutInflater.inflate(
        R.layout.marker_layout, null)


    override fun getInfoWindow(marker: Marker): View? {
        // This function is required, but can return null if
        // not replacing the entire info window
        return null
    }

    // Set the marker's title and snippet to the appropriate text view
    // in the layout created.
    override fun getInfoContents(marker: Marker): View? {
        val titleView = contents.findViewById<TextView>(R.id.title)
        titleView.text = marker.title ?: ""

        val phoneView = contents.findViewById<TextView>(R.id.phone)
        phoneView.text = marker.snippet ?: ""

        return contents
    }
}