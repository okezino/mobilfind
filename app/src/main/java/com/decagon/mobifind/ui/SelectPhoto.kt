package com.decagon.mobifind.ui

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.decagon.mobifind.viewModel.SelectPhotoViewModel
import java.io.IOException
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import com.decagon.mobifind.R
import com.decagon.mobifind.databinding.SelectphotoFragmentBinding
import com.decagon.mobifind.model.data.MobifindUser
import com.decagon.mobifind.model.data.Photo
import com.decagon.mobifind.utils.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


//class SelectPhoto : Fragment() {
//    private var _binding : SelectphotoFragmentBinding? = null
//    private val binding
//    get() = _binding!!
//
//    private lateinit var viewModel: SelectPhotoViewModel
//
//
//
//
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = SelectphotoFragmentBinding.inflate(layoutInflater)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        viewModel = ViewModelProvider(this).get(SelectPhotoViewModel::class.java)
//
//
//        /**
//         * Uses the camera to take a picture and save to a file provider path
//         */
//        binding.fragmentSelectPhotoTakePhotoBtn.setOnClickListener {
//            prepTakePhoto()
//        }
//
//        binding.fragmentSelectPhotoLoadDashboardBtn.setOnClickListener {
//            binding.selectPhotoFragmentLayout.setBackgroundColor(resources.getColor(R.color.grey))
//            binding.fragmentSelectPhotoProgressBar.visibility = View.VISIBLE
//            saveMobifundUser()
//
//        }
//        binding.fragmentSelectPhotoChoosePhotoBtn.setOnClickListener {
//            prepOpenImageGallery()
//        }
//
//    }


//
//    /**
//     * Uses camera to take photo
//     */
//    private fun prepTakePhoto(){
//        if(ContextCompat.checkSelfPermission(
//                requireContext(),
//                android.Manifest.permission.CAMERA)
//            == PackageManager.PERMISSION_GRANTED){
//            takePhoto()
//        }else{
//            val permissionRequest = arrayOf(android.Manifest.permission.CAMERA)
//            requestPermissions(permissionRequest, CAMERA_PERMISSION_REQUEST_CODE)
//        }
//    }
//
//    private fun takePhoto() {
//        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
//            takePictureIntent -> takePictureIntent.resolveActivity(requireContext().packageManager)
//            val photoFile = createImageFile()
//            photoFile.also {
//               photoUri = FileProvider.getUriForFile(requireContext(),"com.decagon.mobifind.android.fileprovider",it)
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri)
//                startActivityForResult(takePictureIntent,SAVE_IMAGE_REQUEST_CODE)
//            }
//        }
//    }
//
//    private fun createImageFile() : File {
//        // Generate a unique file name with date
//        val timestamp = SimpleDateFormat("yyyyMMMdd_HHmmss", Locale.ROOT).format(Date())
//        // Get access to the directory where we can write pictures
//        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//        return File.createTempFile("Mobifind${timestamp}", ".jpg", storageDir?.apply {
//            currentPhotoPath =  absolutePath
//        })
//    }

//    // Checks for the request code
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when(requestCode){
//            CAMERA_PERMISSION_REQUEST_CODE -> {
//                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                    takePhoto()
//                }else{
//                    binding.selectPhotoFragmentLayout.showToast("Unable to take photo without permission")
//                }
//            }
//            PICK_IMAGE -> {
//                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    prepOpenImageGallery()
//                } else {
//                    binding.selectPhotoFragmentLayout.showSnackBar("Permission Denied")
//                }
//            }
//        }
//    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == RESULT_OK){
//            when(requestCode){
//                PICK_IMAGE -> {
//
//                }
//                SAVE_IMAGE_REQUEST_CODE -> {
//                    photo = Photo(localUri = photoUri.toString())
//                    binding.fragmentSignupUsersIv.setImageURI(photoUri)
//                    binding.selectPhotoFragmentLayout.showToast("Image Saved")
//                }
//            }
//        }
//    }




//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
