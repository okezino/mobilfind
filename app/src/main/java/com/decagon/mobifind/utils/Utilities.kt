package com.decagon.mobifind.utils

import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat.getColor
import com.decagon.mobifind.R
import com.google.android.material.snackbar.Snackbar

fun View.showSnackBar(message : String){
    Snackbar.make(this,message,Snackbar.LENGTH_SHORT)
        .show()
}

fun View.showToast(message: String){
    Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
}
