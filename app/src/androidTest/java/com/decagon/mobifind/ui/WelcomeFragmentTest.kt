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
class WelcomeFragmentTest {

    @Before
    fun setUp() {
        launchFragmentInContainer<WelcomeFragment>(themeResId = R.style.Theme_Mobifind)
    }

    @Test
    fun checkVisibilityOfViewComponents() {
        onView(withId(R.id.image_header)).check(matches(isDisplayed()))
        onView(withId(R.id.welcome_tv)).check(matches(isDisplayed()))
        onView(withId(R.id.mobile_tv)).check(matches(isDisplayed()))
        onView(withId(R.id.phone_icon)).check(matches(isDisplayed()))
        onView(withId(R.id.mobile_number_et)).check(matches(isDisplayed()))
        onView(withId(R.id.login_btn)).check(matches(isDisplayed()))
        onView(withId(R.id.signup_btn)).check(matches(isDisplayed()))
    }

    @Test
    fun checkThatButtonsAreClickable() {
        onView(withId(R.id.login_btn)).check(matches(isClickable()))
        onView(withId(R.id.signup_btn)).check(matches(isClickable()))
    }

    @Test
    fun checkThatRightContentAreDisplayed() {
        onView(withId(R.id.welcome_tv)).check(matches(withText(R.string.welcome_text)))
        onView(withId(R.id.mobile_tv)).check(matches(withText(R.string.mobile_number)))
        onView(withId(R.id.mobile_number_et)).check(matches(withHint(R.string.mobile_number_hint)))
        onView(withId(R.id.login_btn)).check(matches(withText(R.string.login_btn_text)))
        onView(withId(R.id.signup_btn)).check(matches(withText(R.string.signup_btn_text)))
    }
}