package com.decagon.mobifind.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.decagon.mobifind.R

/**
 * An adapter class for the viewPager items
 * @param itemsCount number of items to be displayed
 */

class OnboardViewPagerAdapter(private val itemsCount : Int) : RecyclerView.Adapter<OnboardViewPagerAdapter.ViewHolder>() {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val button = view.findViewById<Button>(R.id.button2)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.view_pager_layout,parent,false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == itemsCount- 1) holder.button.visibility = View.VISIBLE

    }

    override fun getItemCount() = itemsCount

}