package com.decagon.mobifind.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.decagon.mobifind.adapter.ViewPagerAdapter
import com.decagon.mobifind.databinding.FragmentDashBoardBinding
import com.decagon.mobifind.utils.DepthPageTransformer
import com.google.android.material.tabs.TabLayoutMediator

class DashBoardFragment : Fragment() {
    private var _binding: FragmentDashBoardBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDashBoardBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPagerAdapter = ViewPagerAdapter(this)
        binding.viewPager.apply {
            adapter = viewPagerAdapter
            setPageTransformer(DepthPageTransformer())
        }
        TabLayoutMediator(binding.tabView, binding.viewPager) { tabs, position ->
            tabs.text = if (position == 0) "Tracking" else  "Trackers"
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}