package com.decagon.mobifind.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.decagon.mobifind.OnboardActivity
import com.decagon.mobifind.R
import com.decagon.mobifind.databinding.ViewPagerLayoutBinding
import com.decagon.mobifind.model.data.Onboard

/**
 * An adapter class for the viewPager items
 * @param itemsCount number of items to be displayed
 */

class OnboardViewPagerAdapter(private val items : List<Onboard>, private val activity : OnboardActivity) : RecyclerView.Adapter<OnboardViewPagerAdapter.ViewHolder>() {


    class ViewHolder(private val binding: ViewPagerLayoutBinding) : RecyclerView.ViewHolder(binding.root){

        val getStartedBtn = binding.getStartedBtn

        fun bind(onboard: Onboard){
           binding.pageTitle.text = onboard.pageTitle
           binding.pageDesc.text = onboard.pageDesc
           binding.pageImg.setImageResource(onboard.pageImg)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewPagerLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == items.size- 1) holder.getStartedBtn.visibility = View.VISIBLE

       holder.getStartedBtn.setOnClickListener {
         //  SharedPreferenceUtil.setOnboardViewedState(holder.button.context,true)
            activity.finish()
        }

        holder.bind(items[position])


    }

    override fun getItemCount() = items.size

}