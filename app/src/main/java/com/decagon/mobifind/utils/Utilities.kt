package com.decagon.mobifind.utils

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.decagon.mobifind.R
import com.decagon.mobifind.adapter.PhoneContactAdapter
import com.decagon.mobifind.adapter.UserAdapter
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Callback

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
            .placeholder(R.drawable.loading_status_animation)
            .error(R.drawable.ic_error_image)
            ).into(this)
    Log.d("InfoAdapt", "load: $imageUrl")
}

fun initAdapter(adapter: UserAdapter, recyclerView: RecyclerView) {
    val divider = DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)
    recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
    recyclerView.adapter = adapter
    recyclerView.addItemDecoration(divider)
}

fun initPhoneAdapter(adapter: PhoneContactAdapter, recyclerView: RecyclerView) {
    val divider = DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)
    recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
    recyclerView.adapter = adapter
    recyclerView.addItemDecoration(divider)
}
