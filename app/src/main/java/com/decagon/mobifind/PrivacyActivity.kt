package com.decagon.mobifind

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import com.decagon.mobifind.databinding.ActivityPrivacyBinding
import com.decagon.mobifind.utils.*
import com.decagon.mobifind.utils.SharedPreferenceUtil
import kotlin.system.exitProcess

class PrivacyActivity : AppCompatActivity() {
    lateinit var binding : ActivityPrivacyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.privacyActivityAgree.setOnClickListener {
           navigateWelcomeFragment()
        }

        binding.privacyActivityDisagree.setOnClickListener {
            generateMaterialDialog(this, ALERT, exitMessage(),
                YES, NO, {existApp()},{})
        }
    }

    private fun existApp(){

        finish()
        exitProcess(0)
    }

    private fun navigateWelcomeFragment(){
        SharedPreferenceUtil.setOnboardViewedState(this,true)
        finish()

    }
}