package com.decagon.mobifind.ui

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.decagon.mobifind.R
import com.decagon.mobifind.databinding.ActivityFirebaseAuthBinding
import com.firebase.ui.auth.AuthUI

class FirebaseAuthActivity : AppCompatActivity() {

private lateinit var binding: ActivityFirebaseAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

     binding = ActivityFirebaseAuthBinding.inflate(layoutInflater)
     setContentView(binding.root)
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.FacebookBuilder().build(),
            AuthUI.IdpConfig.TwitterBuilder().build()
        )

        }
    }

}