package com.decagon.mobifind

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.decagon.mobifind.adapter.OnboardViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlin.math.abs

class OnboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboard)

        val viewPager = findViewById<ViewPager2>(R.id.myViewPager)
        val tabDots = findViewById<TabLayout>(R.id.tabDots)


      viewPager.apply {
            // Configures the adapter of the viewpager to the ViewPagerAdapter class created
            adapter = OnboardViewPagerAdapter( 3)

            // Sets the viewpager not to clip its children and resize
            clipToPadding = false

            // The child of the viewgroup isn't clipped to its bound
            clipChildren = false

            // make sure left/right item is rendered
            offscreenPageLimit = 2
        }

        // Increases the distance in pixels between any two pages
        val marginTransformer = MarginPageTransformer(30)

        /*
         Allows for combination of multiple transformation that is to be performed which includes
         the marginPage transformation and reduction in Y axis for the page based on the page
         and position of the item in the viewPager by 0.25 page 0 -> front and center
         */
        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(marginTransformer)
        compositePageTransformer.addTransformer { page, position ->
            page.scaleY = 1 - (0.25f * abs(position))
        }

        viewPager.setPageTransformer(compositePageTransformer)

        /*
         * Hooks up the viewpager with the tabs layout which defines horizontal tabs
         * in which each tab is replaced with a dot based on the number of items in the
         * viewpager
         */
        TabLayoutMediator(tabDots, viewPager) { tab, _ ->
            viewPager.setCurrentItem(tab.position, true)
        }.attach()

    }
}