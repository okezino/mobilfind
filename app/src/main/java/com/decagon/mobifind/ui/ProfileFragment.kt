package com.decagon.mobifind.ui

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.decagon.mobifind.R
import com.decagon.mobifind.databinding.ProfileFragmentBinding
import com.decagon.mobifind.viewModel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.decagon.mobifind.model.data.Track
import com.decagon.mobifind.utils.load
import com.decagon.mobifind.viewModel.MobifindViewModel
import com.firebase.ui.auth.AuthUI

class ProfileFragment : Fragment() {
    private var _binding : ProfileFragmentBinding? = null
    private val binding
    get() = _binding!!

    private lateinit var viewModel: MobifindViewModel
    private val profileViewModel by viewModels<ProfileViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    activity?.finish()
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ProfileFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(MobifindViewModel::class.java)
        viewModel.setUpFirebaseUser(FirebaseAuth.getInstance().currentUser!!)

//        val currentUser = FirebaseAuth.getInstance().currentUser
//        if (currentUser == null){
//            findNavController().navigate(R.id.welcomeFragment)
//        }

        viewModel.userLocation.observe(viewLifecycleOwner,{
            Log.d("ProfileFragment", "onViewCreated: ${it.latLng.latitude}")
        })

        profileViewModel.userPhotoUrl.observe(viewLifecycleOwner,{
            it?.let {photo->
                binding.fragmentProfileUserImageIv.load(photo)
            }

        })

        binding.fragmentProfileUserImageIv.setOnClickListener {
//            viewModel.pushToTrackers(Track("Tolulope Longe Adeola","08090539526"))
        }
        viewModel.readFromTrackers()



//        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
//        val defaultValue = ""
//        val remoteUri = sharedPref.getString(REMOTE_URI, defaultValue)
//        Log.d("Shared", "onViewCreated: $remoteUri")
//


        // Signs out a user
        binding.fragmentProfileSignOut.setOnClickListener {
            AuthUI.getInstance().signOut(requireActivity()).addOnCompleteListener {
                if (it.isSuccessful){
                    findNavController().navigate(R.id.welcomeFragment)
                }
            }
         // To delete a user account  AuthUI.getInstance().delete(requireActivity()).addOnCompleteListener {  }

        }
    }
}