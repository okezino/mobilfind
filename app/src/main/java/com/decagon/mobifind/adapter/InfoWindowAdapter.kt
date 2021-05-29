package com.decagon.mobifind.adapter

import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.decagon.mobifind.R
import com.decagon.mobifind.utils.image
import com.decagon.mobifind.utils.load
import com.decagon.mobifind.viewModel.MapViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException

import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import java.lang.Exception


/**
 *  This class implements the GoogleMap.InfoWindowAdapter
 *  it is used to define a custom info window for the map
 */
class InfoWindowAdapter(context: Activity) :
    GoogleMap.InfoWindowAdapter {

//    private val images: Map<Marker, Bitmap> = HashMap()
//    private val targets: Map<Marker, Target<Bitmap>> = HashMap()
//
//    /** initiates loading the info window and makes sure the new image is used in case it changed  */
//    fun showInfoWindow(marker: Marker) {
//        Glide.with(contents.context).clear(contents) // will do images.remove(marker) too
//        marker.showInfoWindow() // indirectly calls getInfoContents which will return null and start Glide load
//    }
//
//    /** use this to discard a marker to make sure all resources are freed and not leaked  */
//    fun remove(marker: Marker) {
//        images.remove(marker)
//        Glide.clear(targets.remove(marker))
//        marker.remove()
//    }


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
        titleView.text = marker.title.split("https://")[0] ?: ""

        val phoneView = contents.findViewById<TextView>(R.id.phone)
        phoneView.text = marker.snippet ?: ""
        val userImage = contents.findViewById<ImageView>(R.id.profile_image)
        val photoUri = "https://${marker.title.split("https://")[1]}"
        Log.d("InfoWindowAdapter", "getInfoContents: $photoUri")

       userImage.load((photoUri))
//        Glide.with(userImage.context).load(photoUri).addListener(object:RequestListener<Drawable>{
//            override fun onLoadFailed(
//                e: GlideException?,
//                model: Any?,
//                target: com.bumptech.glide.request.target.Target<Drawable>?,
//                isFirstResource: Boolean
//            ): Boolean {
//                e?.printStackTrace()
//                return false
//            }
//
//            override fun onResourceReady(
//                resource: Drawable?,
//                model: Any?,
//                target: com.bumptech.glide.request.target.Target<Drawable>?,
//                dataSource: DataSource?,
//                isFirstResource: Boolean
//            ): Boolean {
//                if (!isFirstResource) marker.showInfoWindow()
//                return false
//            }
//        }).into(userImage)

//
//            .override(50, 50)
//            .listener(object : RequestListener<String?, Bitmap?> {
//                fun onException(
//                    e: Exception,
//                    model: String?,
//                    target: Target<Bitmap?>?,
//                    isFirstResource: Boolean
//                ): Boolean {
//                    e.printStackTrace()
//                    return false
//                }
//
//                fun onResourceReady(
//                    resource: Bitmap?,
//                    model: String?,
//                    target: Target<Bitmap?>?,
//                    isFromMemoryCache: Boolean,
//                    isFirstResource: Boolean
//                ): Boolean {
//                    if (!isFromMemoryCache) marker.showInfoWindow()
//                    return false
//                }
//            }).into(imageView)
//
//        contents.requestLayout()
        return contents
    }
}


