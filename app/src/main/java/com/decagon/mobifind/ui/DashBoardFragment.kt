package com.decagon.mobifind.ui

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.decagon.mobifind.R
import com.decagon.mobifind.SplashActivity
import com.decagon.mobifind.adapter.ViewPagerAdapter
import com.decagon.mobifind.databinding.FragmentDashBoardBinding
import com.decagon.mobifind.model.data.MobifindUser
import com.decagon.mobifind.model.data.Photo
import com.decagon.mobifind.services.NewMobifindService
import com.decagon.mobifind.utils.Actions
import com.decagon.mobifind.utils.PICK_IMAGE
import com.decagon.mobifind.utils.load
import com.decagon.mobifind.utils.showSnackBar
import com.decagon.mobifind.viewModel.MobifindViewModel
import com.firebase.ui.auth.AuthUI
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class DashBoardFragment : Fragment() {
    private var _binding: FragmentDashBoardBinding? = null
    private val binding
        get() = _binding!!
    private var imageUri: Uri? = null
    private var photo: Photo? = null
    private var photoUri: String? = null
    private var currentUser: FirebaseUser? = null
    private var currentUserName: String? = null
    private val viewModel by activityViewModels<MobifindViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // If a lateint property in viewModel haven't been  initialized,
        // navigate to SplashActivity
        navigateToSplashActivity()
        Intent(requireActivity(), NewMobifindService::class.java).also {
            it.action = Actions.START.name
            ContextCompat.startForegroundService(requireActivity(), it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.getPhotoInPhotos()
        viewModel.getCurrentUserName()
        _binding = FragmentDashBoardBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUser = FirebaseAuth.getInstance().currentUser
        viewModel.userLoc.observe(requireActivity(),{
            viewModel.updateLocationDetails()
        })

        // Sign user out of the app
        binding.logout.setOnClickListener {
            AuthUI.getInstance().signOut(requireActivity()).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Intent(requireActivity(), NewMobifindService::class.java).also {startIntent->
                        startIntent.action = Actions.STOP.name
                        ContextCompat.startForegroundService(requireActivity(), startIntent)
                    }
                    findNavController().popBackStack()
                    findNavController().navigate(R.id.welcomeFragment)
                }
            }
        }

        viewModel.photoUri.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.userImage.load(it)
                photoUri = it
            }
        }

        viewModel.currentUserName.observe(viewLifecycleOwner) {
            currentUserName = it
        }

        viewModel.uploadStatus.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.isNotEmpty()) {
                    binding.root.showSnackBar("Image Upload Successful")
                    binding.fragmentSelectPhotoProgressBar.visibility = View.GONE
                    viewModel.clearUploadStatus()
                }
            }
        }

        binding.userImage.setOnClickListener {
            AlertDialog.Builder(requireContext(), R.style.ImageDialog)
                .setItems(
                    arrayOf("Update Photo", "View Photo")
                ) { _, which ->
                    if (which == 0) {
                        prepOpenImageGallery()
                    } else if (which == 1) {
                        if (photoUri != null) {
                            val action = DashBoardFragmentDirections
                                .actionDashBoardFragmentToPhotoViewFragment(photoUri!!)
                            findNavController().navigate(action)
                        } else {
                            binding.root.showSnackBar("You haven't updated your profile image")
                        }
                    }
                }
                .setNegativeButton("Cancel") { _, _ -> }
                .create()
                .show()
        }

        val viewPagerAdapter = ViewPagerAdapter(childFragmentManager, lifecycle)
        binding.viewPager.adapter = viewPagerAdapter

        TabLayoutMediator(binding.tabView, binding.viewPager) { tabs, position ->
            tabs.text = if (position == 0) "Tracking" else "Trackers"
        }.attach()

    }

    override fun onResume() {
        super.onResume()
        navigateToSplashActivity()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun prepOpenImageGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            imageUri = data?.data!!
            photo = Photo(localUri = imageUri.toString())
            uploadPhoto()
        } else {
            binding.root.showSnackBar("Unable to pick image. Please try again")
        }
    }

    private fun uploadPhoto() {
        binding.fragmentSelectPhotoProgressBar.visibility = View.VISIBLE
        if (currentUser == null) {
            return
        }

        val mobiUser = MobifindUser().apply {
            phoneNumber = currentUser!!.phoneNumber.toString()
            name = currentUserName
        }
        photo?.let { viewModel.save(mobiUser, it, currentUser!!) }
    }

    private fun navigateToSplashActivity() {
        if (!viewModel.isDocumentRefInitialized()) {
            Intent(requireActivity(), SplashActivity::class.java).also {
                requireContext().startActivity(it)
            }
            activity?.finish()
        }
    }
}