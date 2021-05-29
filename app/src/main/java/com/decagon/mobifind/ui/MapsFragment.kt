package com.decagon.mobifind.ui

import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.decagon.mobifind.R
import com.decagon.mobifind.adapter.InfoWindowAdapter
import com.decagon.mobifind.utils.LOCATION_PERMISSION_REQUEST_CODE
import com.decagon.mobifind.viewModel.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsFragment : Fragment() {
    private lateinit var map: GoogleMap
    private lateinit var mapViewModel: MapViewModel


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
        mapViewModel.details.observe(viewLifecycleOwner,{
            Log.d("MapsFragment1", "${it.photoUri}:")
        })
        map = googleMap
        googleMap.uiSettings.isZoomControlsEnabled = true
        Log.d("MapsFragment2", "Map Called: ")
        map.setInfoWindowAdapter(
            InfoWindowAdapter(
                requireActivity()
            )
        )
        setUpMap()


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapViewModel = ViewModelProvider(requireActivity())[MapViewModel::class.java]

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
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        map.isMyLocationEnabled = true
        mapViewModel.getMapDetails("+2348090539526")
        mapViewModel.details.observe(viewLifecycleOwner, {
            val currentLatLng = it.latitude?.let { it1 ->
                it.longitude?.let { it2 ->
                    LatLng(
                        it1,
                        it2
                    )
                }
            }
            val address = it.latitude?.let { it1 ->
                it.longitude?.let { it2 ->
                    Geocoder(context?.applicationContext)
                        .getFromLocation(it1, it2, 1)
                }
            }

            if (currentLatLng != null) {
                if (address != null) {
                    placeMarkerOnMap(currentLatLng, address, it.name ?: "Mobifind User", it.photoUri ?: "")
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
                }
            }
        })
    }

    private fun placeMarkerOnMap(location: LatLng, address: MutableList<Address>, name: String, photoUri : String) {
        val markerOptions = MarkerOptions().position(location).title("${name.trim()}${photoUri.trim()}")
            .snippet("Address: ${address[0].getAddressLine(0)}")
        map.addMarker(markerOptions).showInfoWindow()
    }


}