package com.decagon.mobifind.ui

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.decagon.mobifind.R
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashBoardFragmentTest {
    @Before
    fun setUp() {
        launchFragmentInContainer<DashBoardFragment>(themeResId = R.style.Theme_Mobifind)
    }

    @Test
    fun checkVisibilityOfViewComponents() {
        onView(withId(R.id.app_bar)).check(matches(isDisplayed()))
        onView(withId(R.id.user_image)).check(matches(isDisplayed()))
        onView(withId(R.id.user_image)).check(matches(isClickable()))
        onView(withId(R.id.logout)).check(matches(isDisplayed()))
        onView(withId(R.id.logout)).check(matches(isClickable()))
        onView(withId(R.id.tab_view)).check(matches(isDisplayed()))
        onView(withId(R.id.view_pager)).check(matches(isDisplayed()))
        onView(withId(R.id.divider)).check(matches(isDisplayed()))
    }
}