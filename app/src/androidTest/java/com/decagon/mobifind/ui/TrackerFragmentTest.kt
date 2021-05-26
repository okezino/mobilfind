package com.decagon.mobifind.ui

import android.os.Handler
import android.os.Looper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.decagon.mobifind.R
import com.decagon.mobifind.viewModel.TrackViewModel
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class TrackerFragmentTest {
    private lateinit var viewModel: TrackViewModel

    @get: Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        viewModel = TrackViewModel()
        launchFragmentInContainer<TrackerFragment>(themeResId = R.style.Theme_Mobifind)
    }

    @Test
    fun checkVisibilityOfViewComponentsWhenThereAreNoTrackers() {
        onView(withId(R.id.fab)).check(matches(isDisplayed()))
        onView(withId(R.id.fab)).check(matches(isClickable()))
        onView(withId(R.id.empty_list)).check(matches(isDisplayed()))
        onView(withId(R.id.empty_list)).check(matches(withText(R.string.empty_tracker_list)))
        onView(withId(R.id.recyclerview)).check(matches(not(isDisplayed())))
    }

    @Test
    fun checkVisibilityOfViewComponentsWhenThereAreTrackers() {
        viewModel.addUsers()
        Handler(Looper.getMainLooper()).postDelayed({
            onView(withId(R.id.fab)).check(matches(isDisplayed()))
            onView(withId(R.id.fab)).check(matches(isClickable()))
            onView(withId(R.id.empty_list)).check(matches(not(isDisplayed())))
            onView(withId(R.id.recyclerview)).check(matches(isDisplayed()))
        }, 3000)
    }
}