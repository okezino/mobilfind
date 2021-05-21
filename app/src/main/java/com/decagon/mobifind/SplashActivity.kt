package com.decagon.mobifind


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.decagon.mobifind.databinding.ActivitySplashBinding
import com.decagon.mobifind.databinding.SplashScreenBinding

class SplashActivity : AppCompatActivity() {

    lateinit var topAnimation : Animation
    lateinit var bottomAnimation : Animation
    lateinit var bottomTextAnimation : Animation
    lateinit var binding : ActivitySplashBinding
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        /**
         * Declaring the Animations from res file
         */
        topAnimation = AnimationUtils.loadAnimation(this,R.anim.top_animation)
        bottomAnimation = AnimationUtils.loadAnimation(this,R.anim.bottom_animation)
        bottomTextAnimation = AnimationUtils.loadAnimation(this,R.anim.bottom_text_animation)
        /**
         * Assigning the animations to the respective views
         */
        binding.splashFragmentLogo.animation = topAnimation
        binding.splashFragmentBgImage.animation = bottomAnimation
        binding.splashFragmentTextLogo.animation = bottomTextAnimation
    }

    override fun onResume() {
        super.onResume()
        /**
         * handler for delaying the intent call to the Main activity
         */

        handler.postDelayed({
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)

        },2000)

    }

}