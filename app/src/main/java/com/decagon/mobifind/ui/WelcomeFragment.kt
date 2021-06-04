package com.decagon.mobifind.ui

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.decagon.mobifind.MainActivity
import com.decagon.mobifind.R
import com.decagon.mobifind.databinding.FragmentWelcomeBinding
import com.decagon.mobifind.model.data.MobifindUser
import com.decagon.mobifind.model.data.Photo
import com.decagon.mobifind.model.data.UserLocation
import com.decagon.mobifind.utils.*
import com.decagon.mobifind.viewModel.MobifindViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.io.IOException


class WelcomeFragment : Fragment() {
    private var _binding: FragmentWelcomeBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var activity : MainActivity

    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val mobifindViewModel by activityViewModels<MobifindViewModel>()
    private lateinit var alertDialog: AlertDialog
    private lateinit var displayPhotoIv: ImageView
    private var mobifindUsers = arrayListOf<String>()

    private var imageUri: Uri? = null
    private var photo: Photo? = null
    private var user: FirebaseUser? = null
    private lateinit var logInNumber: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        // Receives updates when device location changes

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation
                val currentLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                val place = Geocoder(activity)
                val myAddress =
                    place.getFromLocation(lastLocation.latitude, lastLocation.longitude, 1)
                val userLocation = UserLocation(currentLatLng, myAddress)
                mobifindViewModel.saveUserLocationUpdates(userLocation)
            }
        }
        makeLocationRequest()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if (FirebaseAuth.getInstance().currentUser != null) {
            user = FirebaseAuth.getInstance().currentUser
            mobifindViewModel.setUpFirebaseUser(user!!)
            findNavController().navigate(R.id.dashBoardFragment)

        }

        _binding = FragmentWelcomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = FirebaseAuth.getInstance().currentUser

        mobifindViewModel.getAllMobifindUsers()
        mobifindViewModel.mobifindUser.observe(requireActivity(), {
            Log.d("Mobifindders", "onViewCreated: $it")
            mobifindUsers = it
        })

        /**
         * Signs up a new user with their phoneNumber
         */
        binding.signupBtn.setOnClickListener {
            if(requestPermission(SIGN_UP_FIREBASE))
            signUpPhoneNumberFirebaseUI()
        }

        /**
         * An alternative login route
         */
        binding.loginBtn.setOnClickListener {
            Log.d("MobifindUserss", "onViewCreated: $mobifindUsers")

            if (requestPermission(LOG_IN_FIREBASE)){
                logInUser()
            }

        }
    }

    private fun logInUser(){
        val number = binding.mobileNumberEt.text.toString()
        if (isSignedUp(filterNumber(number), mobifindUsers))
            signInPhoneNumberFirebaseUI(filterNumber(number))
        else {
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
            //   val response = IdpResponse.fromResultIntent(data) ?: return
            when (requestCode) {
                AUTH_SIGN_IN -> {
                    user = FirebaseAuth.getInstance().currentUser
                    mobifindViewModel.setUpFirebaseUser(user!!)
                    showDialog()

                }
                AUTH_SIGN_UP -> {
                    user = FirebaseAuth.getInstance().currentUser
                    mobifindViewModel.setUpFirebaseUser(user!!)
                    findNavController().navigate(R.id.dashBoardFragment)
                }
                LOCATION_UPDATE_STATE -> {
                    startLocationUpdates()
                }
                PICK_IMAGE -> {
                    try {
                        imageUri = data?.data!!
                        photo = Photo(localUri = imageUri.toString())
                        binding.welcomeTv.showToast("Image Saved")
                        displayPhotoIv.load(imageUri.toString())
                        // saveMobifindUser()
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
                    isSignedUp.observe(viewLifecycleOwner, {
                        if (it) {
                            findNavController().navigate(R.id.dashBoardFragment)
                        }
                    })
                } else {
                    saveMobifindUser(mobiUser)
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

    /**
     * Creates an instance of location request,sets interval, fastest interval
     * and a high priority for the realtime update and makes a request for
     * the user to turn on location if disabled, after which it can start receiving
     * location updates
     */
    private fun makeLocationRequest() {
        locationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(requireActivity())
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            startLocationUpdates()
        }

        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    e.startResolutionForResult(requireActivity(), LOCATION_UPDATE_STATE)
                } catch (sendEx: IntentSender.SendIntentException) {

                }
            }
        }
    }

    /**
     * Requests permission if not granted, and if granted makes a call to the
     * fused location client to request location updates using the location request and callback
     */
    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                GET_LOCATION_UPDATE
            )
            return
        }
        /*
        The Looper object whose message queue will be used to implement the callback mechanism, location
        request to make the request and callback for the location updates
         */
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            GET_LOCATION_UPDATE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates()
                } else {
                    binding.mobileNumberEt.showSnackBar("Permission is Required")
                }
            }
            SIGN_UP_FIREBASE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates()
                    signUpPhoneNumberFirebaseUI()
                } else {
                    binding.mobileNumberEt.showSnackBar("Permission is Required")
                }
            }
            LOG_IN_FIREBASE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates()
                    logInUser()
                } else {
                    binding.mobileNumberEt.showSnackBar("Permission is Required")
                }
            }
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

        mobifindViewModel.uploadStatus.observe(viewLifecycleOwner, {
            if (it != null) {
                if (it.isNotEmpty()) {
                    binding.fragmentWelcomeProgress.visibility = View.GONE
                    binding.fragmentWelcomeProgress.showSnackBar("Authentication and Image Upload Successful")
                    findNavController().navigate(R.id.dashBoardFragment)
                }
            }
        })
    }


    private fun requestPermission(requestCode: Int) : Boolean{
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                requestCode
            )
            return false
        }
        return true
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.activity = context as MainActivity
    }

}