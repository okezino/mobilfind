package com.decagon.mobifind.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.decagon.mobifind.R
import com.decagon.mobifind.adapter.ViewPagerAdapter
import com.decagon.mobifind.databinding.FragmentDashBoardBinding
import com.decagon.mobifind.model.data.MobifindUser
import com.decagon.mobifind.model.data.Photo
import com.decagon.mobifind.utils.*
import com.decagon.mobifind.viewModel.MobifindViewModel
import com.decagon.mobifind.viewModel.SelectPhotoViewModel
import com.firebase.ui.auth.AuthUI
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.io.IOException

class DashBoardFragment : Fragment() {
    private var _binding: FragmentDashBoardBinding? = null
    private val binding
        get() = _binding!!
    private var imageUri: Uri? = null
    private var photo: Photo? = null
    private var currentUser: FirebaseUser? = null
    private lateinit var sharedPref: SharedPreferences
    private val viewModel by viewModels<SelectPhotoViewModel>()
    private lateinit var mobifindViewModel: MobifindViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDashBoardBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mobifindViewModel = ViewModelProvider(requireActivity()).get(MobifindViewModel::class.java)

        currentUser = FirebaseAuth.getInstance().currentUser

        // Sign user out of the app
        binding.logout.setOnClickListener {
            AuthUI.getInstance().signOut(requireActivity()).addOnCompleteListener {
                if (it.isSuccessful) {
                    findNavController().navigate(R.id.welcomeFragment)
                }
            }
        }

        binding.userImage.setOnClickListener { prepOpenImageGallery() }

        val viewPagerAdapter = ViewPagerAdapter(childFragmentManager, lifecycle)
        binding.viewPager.apply {
            adapter = viewPagerAdapter
            setPageTransformer(DepthPageTransformer())
        }

        TabLayoutMediator(binding.tabView, binding.viewPager) { tabs, position ->
            tabs.text = if (position == 0) "Tracking" else  "Trackers"
        }.attach()

        sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        val profilePhoto = sharedPref.getString(REMOTE_URI, null)
        profilePhoto?.let { binding.userImage.load(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun prepOpenImageGallery(){
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
            startActivityForResult(this, PICK_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == PICK_IMAGE){
                    try {
                        imageUri = data?.data!!
                        binding.userImage.setImageURI(imageUri)
                        photo = Photo(localUri = imageUri.toString())
                        binding.root.showToast("Image Saved")
                        uploadPhoto()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
            }
        }
    }

    private fun uploadPhoto() {
        if (currentUser == null) return

        val mobiUser = MobifindUser().apply {
            phoneNumber = currentUser!!.phoneNumber.toString()
        }
        photo?.let { mobifindViewModel.save(mobiUser, it, currentUser!!) }

        mobifindViewModel.uploadStatus.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                with(sharedPref.edit()) {
                    putString(REMOTE_URI, it)
                    apply()
                }
               // binding.fragmentSelectPhotoProgressBar.visibility = View.GONE
                binding.root.showSnackBar("Authentication and Image Upload Successful")
            }
        }

    }
}