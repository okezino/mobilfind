package com.decagon.mobifind.ui

import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.decagon.mobifind.MainActivity
import com.decagon.mobifind.R
import com.decagon.mobifind.adapter.InfoWindowAdapter
import com.decagon.mobifind.model.data.Track
import com.decagon.mobifind.utils.LOCATION_PERMISSION_REQUEST_CODE
import com.decagon.mobifind.utils.NetworkLiveData
import com.decagon.mobifind.utils.showSnackBar
import com.decagon.mobifind.utils.timeConvert
import com.decagon.mobifind.viewModel.MapViewModel
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsFragment : Fragment() {
    private lateinit var map: GoogleMap
    private val mapViewModel by viewModels<MapViewModel>()
    private val mapsArgs by navArgs<MapsFragmentArgs>()
    private lateinit var tracking: Track
    private var address: MutableList<Address>? = null


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
        tracking = mapsArgs.tracking
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
        tracking.phoneNumber?.let { mapViewModel.getMapDetails(it) }
        NetworkLiveData.observe(requireActivity(), { connected ->
            if (connected) {
                mapViewModel.details.observe(viewLifecycleOwner, {
                    val currentLatLng = it.latitude?.let { it1 ->
                        it.longitude?.let { it2 ->
                            LatLng(
                                it1,
                                it2
                            )
                        }
                    }
                    it.latitude?.let { it1 ->
                        it.longitude?.let { it2 ->
                            try {
                                address = Geocoder(activity)
                                    .getFromLocation(it1, it2, 1)
                            } catch (ex: Exception) {
                            }

                        }
                    }

                    if (currentLatLng != null) {
                        if (address != null) {
                            Log.d("MapsFragmenet", "setUpMap: $address")
                            placeMarkerOnMap(
                                currentLatLng,
                                address!!,
                                it.name ?: "Mobifind User",
                                it.photoUri
                                    ?: "https://st3.depositphotos.com/9998432/13335/v/600/depositphotos_133352154-stock-illustration-default-placeholder-profile-icon.jpg",
                                it.time
                            )
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))
                        }
                    }

                })
            } else {
                view?.showSnackBar("No Internet Connection")
            }
        })
    }

    private fun placeMarkerOnMap(
        location: LatLng,
        address: MutableList<Address>,
        name: String,
        photoUri: String,
        lastSeen: String?
    ) {
        val markerOptions =
            MarkerOptions().position(location).title("${name.trim()}${photoUri.trim()}")
                .snippet("Address: ${address[0].getAddressLine(0)}\n\n" +
                        "Last seen: ${timeConvert(lastSeen)}")
        map.clear()
        map.addMarker(markerOptions)
    }


}