package com.decagon.mobifind.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.decagon.mobifind.ui.TrackerFragment
import com.decagon.mobifind.ui.TrackingFragment

class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment =
        if (position == 0) TrackingFragment() else TrackerFragment()
}