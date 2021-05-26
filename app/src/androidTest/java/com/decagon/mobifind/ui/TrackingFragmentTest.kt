package com.decagon.mobifind.ui

import android.os.Handler
import android.os.Looper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.decagon.mobifind.R
import com.decagon.mobifind.viewModel.TrackViewModel
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.not
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TrackingFragmentTest {
    private lateinit var viewModel: TrackViewModel

    @get: Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        viewModel = TrackViewModel()
        launchFragmentInContainer<TrackingFragment>(themeResId = R.style.Theme_Mobifind)
    }

    @Test
    fun checkVisibilityOfViewComponentsWhenYouAreTrackingNoOne() {
        onView(ViewMatchers.withId(R.id.empty_list)).check(matches(isDisplayed()))
        onView(ViewMatchers.withId(R.id.empty_list))
            .check(matches(ViewMatchers.withText(R.string.empty_tracking_list)))
        onView(ViewMatchers.withId(R.id.recyclerview)).check(matches(not(isDisplayed())))
    }
}