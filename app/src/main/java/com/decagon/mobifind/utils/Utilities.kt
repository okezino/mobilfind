package com.decagon.mobifind.utils

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
import com.google.android.material.snackbar.Snackbar

fun View.showSnackBar(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT)
        .show()
}

fun View.showToast(message: String) {
    Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
}

fun ImageView.load(imageUrl: String) {
    Glide.with(this.context)
        .load(imageUrl).apply(
            RequestOptions()
                .placeholder(R.drawable.loading_status_animation)
                .error(R.drawable.ic_error_image)
        ).into(this)
}

fun initAdapter(adapter: UserAdapter, recyclerView: RecyclerView) {
    val divider = DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)
    recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
    recyclerView.adapter = adapter
    recyclerView.addItemDecoration(divider)
}

fun filterNumber(number: String): String {
    if(number.isEmpty()) return ""
    val result = if (number.take(1) == "+") number else number.replaceRange(0..0, "+234")
    return result.filter { it == '+' || it.isDigit() }
}

fun inviteMessage(name: String): String {
    return "Hello $name \n check out this cool location tracker app using the below Link +++"
}

fun denyMessage():String{
    return "Manually give Mobifind permission to your Contact, to enable you add Trackers to your list"
}
fun sendSuccessMessage(name: String):String{
    return "$name has been successfully added to Tracker List"
}

fun failedMessage():String{
    return "Failed Operation: Try again"
}

fun searchContact(s:String, contact:ArrayList<String>) : List<String>{
    return  contact.filter { it.contains(s, ignoreCase = true)}
}

fun isSignedUp(number: String, users: ArrayList<String>) = number in users