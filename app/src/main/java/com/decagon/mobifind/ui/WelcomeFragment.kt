package com.decagon.mobifind.ui

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.decagon.mobifind.BuildConfig
import com.decagon.mobifind.R
import com.decagon.mobifind.databinding.FragmentWelcomeBinding
import com.decagon.mobifind.model.data.MobifindUser
import com.decagon.mobifind.model.data.Photo
import com.decagon.mobifind.services.NewMobifindService
import com.decagon.mobifind.utils.*
import com.decagon.mobifind.utils.SharedPreferenceUtil.getServiceState
import com.decagon.mobifind.viewModel.MobifindViewModel
import com.firebase.ui.auth.AuthUI
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.shreyaspatil.MaterialDialog.MaterialDialog
import java.io.IOException


class WelcomeFragment : Fragment() {
    private var _binding: FragmentWelcomeBinding? = null
    private val binding
        get() = _binding!!

    private val mobifindViewModel by activityViewModels<MobifindViewModel>()
    private lateinit var alertDialog: AlertDialog
    private lateinit var displayPhotoIv: ImageView
    private var mobifindUsers = arrayListOf<String>()

    private var imageUri: Uri? = null
    private var photo: Photo? = null
    private var user: FirebaseUser? = null

    private var isSuccess = false
    private lateinit var logInNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("WELCOMEFRAG", "onCreate: ")
        println("WELCOMEFRAG")

        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (FirebaseAuth.getInstance().currentUser != null) {
            user = FirebaseAuth.getInstance().currentUser
            mobifindViewModel.setUpFirebaseUser(user!!)
            actionOnService(Actions.START)
            findNavController().navigate(R.id.dashBoardFragment)
        }

        _binding = FragmentWelcomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (isSuccess) {
            binding.fragmentWelcomeProgress.visibility = View.VISIBLE
        } else {
            binding.fragmentWelcomeProgress.visibility = View.GONE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = FirebaseAuth.getInstance().currentUser


        mobifindViewModel.getAllMobifindUsers()

        mobifindViewModel.mobifindUser.observe(requireActivity()) {
            mobifindUsers = it
        }

        /**
         * Signs up a new user with their phoneNumber
         */
        binding.signupBtn.setOnClickListener {
            requestPermissionAndLoginUser(SIGN_UP_FIREBASE) { signUpPhoneNumberFirebaseUI() }
        }

        /**
         * An alternative login route
         */
        binding.loginBtn.setOnClickListener {
            requestPermissionAndLoginUser(LOG_IN_FIREBASE) { logInUser() }
        }
    }

    private fun requestPermissionAndLoginUser(requestCode: Int, startUser: () -> Unit) {

        if (foregroundPermissionApproved()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (backgroundPermissionApproved()) {
                    startUser()
                } else {
                    requireContext().checkBackgroundLocationPermissionAPI30(LOG_IN_FIREBASE)
                }
            } else {
                startUser()
            }
        } else {
            requestForegroundPermissions(requestCode)
        }
    }

    private fun logInUser() {
        val number = binding.mobileNumberEt.text.toString().trim()
        if (number.isEmpty()) {
            binding.mobileNumberEt.error = "Please enter your mobile number"
            return
        }
        if (isSignedUp(filterNumber(number), mobifindUsers)) {
            signInPhoneNumberFirebaseUI(filterNumber(number))

        } else {
            binding.forgotPasswordTv.showSnackBar("You need to sign up")
            binding.mobileNumberEt.text?.clear()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun signInPhoneNumberFirebaseUI(number: String) {
        logInNumber = number
        binding.fragmentWelcomeProgress.visibility = View.VISIBLE
        val providers = arrayListOf(
            AuthUI.IdpConfig.PhoneBuilder().setDefaultNumber(number).build(),
        )
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            AUTH_SIGN_UP
        )
    }

    private fun signUpPhoneNumberFirebaseUI() {
        binding.fragmentWelcomeProgress.visibility = View.VISIBLE
        val providers = arrayListOf(
            AuthUI.IdpConfig.PhoneBuilder().build(),
        )
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.AuthenticationTheme)
                .build(),
            AUTH_SIGN_IN
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                AUTH_SIGN_IN -> {
                    user = FirebaseAuth.getInstance().currentUser
                    isSuccess = true
                    SharedPreferenceUtil.savePhoneNumberInSharedPref(
                        requireActivity(),
                        user?.phoneNumber
                    )

                    if (user?.phoneNumber?.let { isSignedUp(it, mobifindUsers) } == true) {
                        isSuccess = false
                        generateMaterialDialog(requireActivity(), "You have previously signed up",
                            "Continue to dashboard", "OK", "LOGIN",
                            { findNavController().navigate(R.id.dashBoardFragment) }, null
                        )

                        return
                    }
                    mobifindViewModel.setUpFirebaseUser(user!!)
                    showDialog()

                }
                AUTH_SIGN_UP -> {
                    user = FirebaseAuth.getInstance().currentUser
                    isSuccess = true
                    SharedPreferenceUtil.savePhoneNumberInSharedPref(
                        requireActivity(),
                        user?.phoneNumber
                    )
                    mobifindViewModel.setUpFirebaseUser(user!!)

                    findNavController().navigate(R.id.dashBoardFragment)
                }
                PICK_IMAGE -> {
                    try {
                        imageUri = data?.data!!
                        photo = Photo(localUri = imageUri.toString())
                        binding.welcomeTv.showToast("Image Saved")
                        displayPhotoIv.load(imageUri.toString())
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun showDialog() {
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_layout, null)
        val doneBtn = dialogView.findViewById<Button>(R.id.done_btn)
        val cameraIcon = dialogView.findViewById<ImageView>(R.id.camera_icon)
        val displayNameEt = dialogView.findViewById<EditText>(R.id.display_name_et)
        displayPhotoIv = dialogView.findViewById(R.id.display_pic_iv)
        cameraIcon.setOnClickListener { prepOpenImageGallery() }
        doneBtn.setOnClickListener {
            val displayName = displayNameEt.text.toString().trim()
            if (displayName.isEmpty()) {
                displayNameEt.error = "Username must not be empty"
                return@setOnClickListener
            }


            val mobiUser = MobifindUser().apply {
                phoneNumber = user!!.phoneNumber.toString()
                name = displayName
            }

            mobifindViewModel.apply {
                if (imageUri == null) {
                    signUpUserWithoutPhoto(mobiUser)
                    alertDialog.dismiss()
                    isSignedUp.observe(viewLifecycleOwner) {
                        if (it) {
                            findNavController().navigate(R.id.dashBoardFragment)
                        }
                    }
                } else {
                    saveMobifindUser(mobiUser)
                    binding.fragmentWelcomeProgress.visibility = View.VISIBLE
                    alertDialog.dismiss()
                }
            }
        }

        alertDialog = MaterialAlertDialogBuilder(requireContext(), R.style.MyDialogTheme).apply {
            setTitle("Almost Done")
            setView(dialogView)
            setCancelable(false)
        }.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            SIGN_UP_FIREBASE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestPermissionAndLoginUser(SIGN_UP_FIREBASE) { signUpPhoneNumberFirebaseUI() }
                } else {
                    showSettings()
                }
            }
            LOG_IN_FIREBASE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestPermissionAndLoginUser(LOG_IN_FIREBASE) { logInUser() }
                } else {
                    showSettings()
                }
            }
        }
    }

    private fun showSettings() {
        generateMaterialDialog(
            requireActivity(), getString(R.string.permission_denied_explanation),
            getString(R.string.permission_denied_walkthrough), "Continue", "Cancel",
            positiveAction = { openSettings() },
            negativeAction = null
        )
    }

    private fun openSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts(
            "package",
            BuildConfig.APPLICATION_ID,
            null
        )
        intent.data = uri
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    // load the permission setting screen
    @TargetApi(30)
    private fun Context.checkBackgroundLocationPermissionAPI30(backgroundLocationRequestCode: Int) {
        if (backgroundPermissionApproved()) return

        generateMaterialDialog(
            requireActivity(),
            getString(R.string.background_location_permission_title),
            getString(R.string.background_location_permission_message),
            getString(R.string.yes))
            {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    backgroundLocationRequestCode
                )
            }
    }

    //=== IMAGE UPLPOAD =====//
    /**
     * Chooses image from gallery
     */
    private fun prepOpenImageGallery() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
            startActivityForResult(this, PICK_IMAGE)
        }
    }

    /**
     * Transfers the logic of saving a user with photo to the cloudFirestore and storage to the
     * viewModel
     */
    private fun saveMobifindUser(mobiUser: MobifindUser) {
        if (user == null) {
            findNavController().navigate(R.id.welcomeFragment)
        }
        user ?: return

        photo?.let { mobifindViewModel.save(mobiUser, it, user!!) }

        mobifindViewModel.uploadStatus.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.isNotEmpty()) {
                    binding.fragmentWelcomeProgress.visibility = View.GONE
                    binding.fragmentWelcomeProgress.showSnackBar("Authentication and Image Upload Successful")
                    findNavController().navigate(R.id.dashBoardFragment)
                }
            }
        }
    }

    private fun foregroundPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun backgroundPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    }

    private fun requestForegroundPermissions(requestCode: Int) {
        val provideRationale = foregroundPermissionApproved()

        // If the user denied a previous request, but didn't check "Don't ask again", provide
        // additional rationale.
        if (provideRationale) {
            Snackbar.make(
                binding.loginBtn,
                R.string.permission_rationale,
                Snackbar.LENGTH_LONG
            )
                .setAction(R.string.ok) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        requireContext().checkBackgroundLocationPermissionAPI30(requestCode)
                    } else {
                        requestPermissions(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            requestCode
                        )
                    }
                }
                .show()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                requestCode
            )
        }
    }

    private fun actionOnService(action: Actions) {
        if (getServiceState(requireActivity()) == ServiceState.STOPPED && action == Actions.STOP) return
        Intent(requireActivity(), NewMobifindService::class.java).also {
            it.action = action.name
            ContextCompat.startForegroundService(requireActivity(), it)
        }

    }
}