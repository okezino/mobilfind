package com.decagon.mobifind.utils

import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat.getColor
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.decagon.mobifind.R
import com.google.android.material.snackbar.Snackbar

fun View.showSnackBar(message : String){
    Snackbar.make(this,message,Snackbar.LENGTH_SHORT)
        .show()
}

fun View.showToast(message: String){
    Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
}

fun ImageView.load(imageUrl: String) {
    Glide.with(this.context)
        .load(imageUrl).apply(RequestOptions()
            .placeholder(R.drawable.loading_animation)
            .error(R.drawable.ic_broken_image)
            ).into(this)
}
