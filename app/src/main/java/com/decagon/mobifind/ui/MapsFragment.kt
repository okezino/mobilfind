package com.decagon.mobifind.ui

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.decagon.mobifind.R
import com.decagon.mobifind.adapter.InfoWindowAdapter
import com.decagon.mobifind.utils.GET_LOCATION_UPDATE
import com.decagon.mobifind.utils.LOCATION_PERMISSION_REQUEST_CODE
import com.decagon.mobifind.utils.LOCATION_UPDATE_STATE
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsFragment : Fragment() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false




    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        map = googleMap
        val sydney = LatLng(-34.0, 151.0)
        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 12f))
        googleMap.uiSettings.isZoomControlsEnabled = true
        map.setInfoWindowAdapter(InfoWindowAdapter(requireActivity()))
        setUpMap()


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        // Receives updates when device location changes
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation
                Log.d("MapsFragment", "onLocationResult:${lastLocation.latitude} ")
            }
        }
        //makeLocationRequest()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

    }

    private fun setUpMap() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        map.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
            // Got last known location. In some rare situations this can be null.
            // 3
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                val place = Geocoder(context?.applicationContext)
                val address = place.getFromLocation(location.latitude, location.longitude, 1)

                placeMarkerOnMap(currentLatLng, address)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
        }
    }

    private fun placeMarkerOnMap(location: LatLng, address: MutableList<Address>) {
        val markerOptions = MarkerOptions().position(location).title("Tolulope Longe")
            .snippet("Address: ${address[0].getAddressLine(0)}").icon(
                BitmapDescriptorFactory.fromBitmap(
                    BitmapFactory.decodeResource(resources, R.mipmap.map_marker)))
        map.addMarker(markerOptions).showInfoWindow()
    }

//    /**
//     * Requests permission if not granted, and if granted makes a call to the
//     * fused location client to request location updates using the location request and callback
//     */
//    private fun startLocationUpdates() {
//        if (ContextCompat.checkSelfPermission(
//                requireContext(),
//                android.Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            requestPermissions(
//                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
//                GET_LOCATION_UPDATE
//            )
//            return
//        }
//        /*
//        The Looper object whose message queue will be used to implement the callback mechanism, location
//        request to make the request and callback for the location updates
//         */
//        fusedLocationClient.requestLocationUpdates(
//            locationRequest,
//            locationCallback,
//            Looper.getMainLooper()
//        )
//    }
//
//    /**
//     * Creates an instance of location request,sets interval, fastest interval
//     * and a high priority for the realtime update and makes a request for
//     * the user to turn on location if disabled, after which it can start receiving
//     * location updates
//     */
//    private fun makeLocationRequest() {
//        locationRequest = LocationRequest.create()
//        locationRequest.interval = 10000
//        locationRequest.fastestInterval = 5000
//        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//
//        val builder = LocationSettingsRequest.Builder()
//            .addLocationRequest(locationRequest)
//
//        val client = LocationServices.getSettingsClient(requireActivity())
//        val task = client.checkLocationSettings(builder.build())
//
//        task.addOnSuccessListener {
//            locationUpdateState = true
//            startLocationUpdates()
//        }
//
//        task.addOnFailureListener { e ->
//            if (e is ResolvableApiException) {
//                try {
//                    e.startResolutionForResult(requireActivity(), LOCATION_UPDATE_STATE)
//                } catch (sendEx: IntentSender.SendIntentException) {
//
//                }
//            }
//        }
//    }
//
//    // Starts update request for location update state
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == LOCATION_UPDATE_STATE) {
//            if (resultCode == Activity.RESULT_OK) {
//                locationUpdateState = true
//                startLocationUpdates()
//            }
//        }
//    }
//
//
//    // Handles permission request for getting location update if granted for ACCESS_FINE_LOCATION
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == GET_LOCATION_UPDATE) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                getLocationUpdates()
//            } else {
//                Toast.makeText(requireContext(), "Permission Required", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    /**
//     * Requests for fine location permission and if already granted sets ups a blue dot for the device
//     * current location
//     */
//    private fun getLocationUpdates() {
//        if (ContextCompat.checkSelfPermission(
//                requireContext(),
//                android.Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            requestPermissions(
//                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
//                GET_LOCATION_UPDATE
//            )
//            return
//        }
//        map.isMyLocationEnabled = true
//    }


}