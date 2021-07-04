package com.decagon.mobifind.adapter

import android.app.Activity
import android.graphics.drawable.Drawable
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.decagon.mobifind.R
import com.decagon.mobifind.utils.load
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.util.logging.Handler


/**
 *  This class implements the GoogleMap.InfoWindowAdapter
 *  it is used to define a custom info window for the map
 */
class InfoWindowAdapter(context: Activity) :
    GoogleMap.InfoWindowAdapter {

    // Inflate the content of the map's info window with the
    // custom layout defined
    private val contents: View = context.layoutInflater.inflate(
        R.layout.marker_layout, null
    )


    override fun getInfoWindow(marker: Marker): View? {
        // This function is required, but can return null if
        // not replacing the entire info window

        // Set the marker's title and snippet to the appropriate text view
        // in the layout created.

        val titleView = contents.findViewById<TextView>(R.id.title)
        titleView.text = marker.title.split("https://")[0]

        val phoneView = contents.findViewById<TextView>(R.id.phone)
        phoneView.text = marker.snippet ?: ""
        val userImage = contents.findViewById<ImageView>(R.id.profile_image)
        val photoUri = "https://${marker.title.split("https://")[1]}"


        Glide.with(userImage.context)
            .load(photoUri).listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    val handler = android.os.Handler(Looper.getMainLooper())
                    handler.post {
                        if (marker.isInfoWindowShown) {
                            marker.hideInfoWindow()
                            marker.showInfoWindow()
                        }
                    }
                    return false
                }

            })
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_status_animation)
                    .error(R.drawable.ic_error_image)
            ).into(userImage)
        return contents

    }

    // Set the marker's title and snippet to the appropriate text view
    // in the layout created.
    override fun getInfoContents(marker: Marker): View? {
        return null
    }
}


