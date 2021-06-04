package com.decagon.mobifind

import com.decagon.mobifind.utils.isSignedUp
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

class LoginValidationTest {
    private lateinit var listOfUsers : ArrayList<String>
    @Before
    fun setUp(){
        listOfUsers = arrayListOf("+2348090539526","+19999999999","+2347025838175")
    }

    @Test
    fun validatePhoneNumber_existsInMobifindUsers_returnsTrue() {
        // Given a phoneNumber to validate
        val phoneNumber = "+19999999999"

        // When
        val result = isSignedUp(phoneNumber,listOfUsers)
        // Then
        assertThat(result,`is`(true))
    }

    @Test
    fun validatePhoneNumber_existsInMobifindUsers_returnsFalse() {
        // Given a phoneNumber to validate
        val phoneNumber = "+2348032456789"

        // When
        val result = isSignedUp(phoneNumber,listOfUsers)
        // Then
        assertThat(result,`is`(false))
    }


}