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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.io.IOException


class WelcomeFragment : Fragment() {
    private var _binding: FragmentWelcomeBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mobifindViewModel: MobifindViewModel

    private var imageUri: Uri? = null

    private var photo : Photo? = null
    private var photoUri : Uri? = null
    private var user : FirebaseUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mobifindViewModel = ViewModelProvider(requireActivity())[MobifindViewModel::class.java]
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        // Receives updates when device location changes
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation
                val currentLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                val place = Geocoder(context?.applicationContext)
                val myAaddress = place.getFromLocation(lastLocation.latitude, lastLocation.longitude, 1)
                val userLocation = UserLocation(currentLatLng,myAaddress)
                mobifindViewModel.saveUserLocationUpdates(userLocation)
            }
        }
        makeLocationRequest()
      //  listenForLocationUpdates()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if(FirebaseAuth.getInstance().currentUser != null){
            findNavController().navigate(R.id.profileFragment)
        }
        _binding = FragmentWelcomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = FirebaseAuth.getInstance().currentUser

        /**
         * Signs up a new user with their phoneNumber
         */
        binding.signupBtn.setOnClickListener{
            signInPhoneNumberFirebaseUI()
        }

        /**
         * An alternative login route
         */
        binding.loginBtn.setOnClickListener {
            //  loginInFirebase()
            // Temporary navigation to the mapsFragment
            findNavController().navigate(R.id.mapsFragment)

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun signInPhoneNumberFirebaseUI() {
        binding.fragmentWelcomeProgress.visibility = View.VISIBLE
        val providers = arrayListOf(
            AuthUI.IdpConfig.PhoneBuilder().build(),
        )
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            AUTH_SIGN_IN
        )
    }

    private fun loginInFirebase(){
        val number = binding.mobileNumberEt.text.toString().trim()
        if (number.isEmpty() || number.length < 10) {
            binding.mobileNumberEt.error = "Valid number is required"
            binding.mobileNumberEt.requestFocus()
            return
        }
        val action = WelcomeFragmentDirections.actionWelcomeFragmentToVerificationFragment(number)
        findNavController().navigate(action)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK){
         //   val response = IdpResponse.fromResultIntent(data) ?: return
            when(requestCode){
                AUTH_SIGN_IN->{
                    user = FirebaseAuth.getInstance().currentUser
                   showDialog()

                }
                LOCATION_UPDATE_STATE->{
                    startLocationUpdates()
                }
                PICK_IMAGE->{
                    try {
                        imageUri = data?.data!!
                        photo = Photo(localUri = imageUri.toString())
                        binding.welcomeTv.showToast("Image Saved")
                        saveMobifindUser()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }


    private fun showDialog(){
        val alertDialog: AlertDialog? = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setMessage("Do you want to upload an image")
                setPositiveButton("YES"
                ) { _, _ ->
                    prepOpenImageGallery()
                    // User clicked OK button
                }
                setNegativeButton("NO"
                ) { dialog, id ->
                    dialog.dismiss()
                    // User cancelled the dialog
                }
            }

            // Create the AlertDialog
            builder.create()
        }

        //  findNavController().navigate(R.id.selectphoto_fragment)
        alertDialog?.show()

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
        if (requestCode == GET_LOCATION_UPDATE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(requireContext(), "Permission Required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //=== IMAGE UPLPOAD =====//
    /**
     * Chooses image from gallery
     */
    private fun prepOpenImageGallery(){
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
            startActivityForResult(this, PICK_IMAGE)
        }
    }

    /**
     * Transfers the logic of saving a user with photo to the cloudFirestore and storage to the
     * viewModel
     */
    private fun saveMobifindUser(){
        if (user == null){
            findNavController().navigate(R.id.welcomeFragment)
        }
        user ?: return
        val mobiUser = MobifindUser().apply {
            phoneNumber = user!!.phoneNumber.toString()
        }
        photo?.let { mobifindViewModel.save(mobiUser, it, user!!) }

        mobifindViewModel.uploadStatus.observe(viewLifecycleOwner,{
            if (it.isNotEmpty()){
                binding.fragmentWelcomeProgress.visibility = View.GONE
                binding.fragmentWelcomeProgress.showSnackBar("Authentication and Image Upload Successful")
                findNavController().navigate(R.id.profileFragment)
            }
        })


    }


}