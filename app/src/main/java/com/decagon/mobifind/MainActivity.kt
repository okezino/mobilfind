package com.decagon.mobifind

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.decagon.mobifind.databinding.ActivityMainBinding
import com.decagon.mobifind.utils.NetworkLiveData
import com.decagon.mobifind.utils.SharedPreferenceUtil.isAccessibilitySettingsOn


class MainActivity : AppCompatActivity() {
    companion object { val activity = HashMap<String, AppCompatActivity>() }
    private lateinit var navController: NavController
    lateinit var binding : ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NetworkLiveData.init(this.application)
         binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val accessEnabled = isAccessibilitySettingsOn(this)
        if (!accessEnabled) {
            AlertDialog.Builder(this, R.style.MyDialogTheme)
                .setCancelable(false)
                .setTitle(R.string.accessibility_dialog_msg)
                .setPositiveButton("OK") { _, _, ->
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    startActivityForResult(intent, 0)
                }
                .show()
        }
        activity["ACTIVITY"] = this

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController
    }
}