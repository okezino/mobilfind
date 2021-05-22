package com.decagon.mobifind.ui

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.decagon.mobifind.databinding.SignupFragmentBinding
import com.decagon.mobifind.viewModel.SignupViewModel
import java.io.IOException
import android.widget.ArrayAdapter
import androidx.core.content.FileProvider
import com.decagon.mobifind.R
import com.decagon.mobifind.model.data.MobifindUser
import com.decagon.mobifind.model.data.Photo
import com.decagon.mobifind.utils.*
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class SignupFragment : Fragment() {
    private var _binding : SignupFragmentBinding? = null
    private val binding
    get() = _binding!!


    private lateinit var viewModel: SignupViewModel
    private var imageUri: Uri? = null
    private lateinit var currentPhotoPath : String

    private var photos : ArrayList<Photo> = ArrayList()
    private var photoUri : Uri? = null

    private var user : FirebaseUser? = null


    override fun onStart() {
        super.onStart()
        if(FirebaseAuth.getInstance().currentUser != null){
          //  findNavController().navigate(R.id.profileFragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SignupFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(SignupViewModel::class.java)

        binding.fragmentSignupSelectImageBtn.setOnClickListener {
           // changeImageFromGallery()
            prepTakePhoto()
           // prepOpenImageGallery()
          //  saveMobifundUser()
        }

        binding.fragmentSignupSpinner.adapter = ArrayAdapter(requireContext(),android.R.layout.simple_spinner_dropdown_item,
            listOfCountries)

        binding.fragmentSignupGetOTPBtn.setOnClickListener { 
//            val code = countryCodes[binding.fragmentSignupSpinner.selectedItemPosition]
//            val number = binding.fragmentSignupPhoneNumberEt.text.toString().trim()
//
//            if (number.isEmpty() || number.length < 10) {
//                binding.fragmentSignupPhoneNumberEt.error = "Valid number is required"
//                binding.fragmentSignupPhoneNumberEt.requestFocus()
//                return@setOnClickListener
//            }
//
//            val phoneNumber = "+$code$number"
//            val action = SignupFragmentDirections.actionSignupFragmentToVerificationFragment(phoneNumber)
//            findNavController().navigate(action)
//
//            Log.d(TAG, "onViewCreated: $phoneNumber")
            saveMobifundUser()

        }

        viewModel.mobifindUser.observe(viewLifecycleOwner, {
            Log.d(TAG, "onViewCreated: $it")
        })





    }


    // Checks if permission is granted and if not permission is requested and image set to the imageView
    private fun changeImageFromGallery() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                PICK_IMAGE
            )
        } else {
            val galleryIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(galleryIntent, PICK_IMAGE)
        }
    }

    private fun prepOpenImageGallery(){
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
            startActivityForResult(this, PICK_IMAGE)
        }
    }

    private fun prepTakePhoto(){
        if(ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED){
            takePhoto()
        }else{
            val permissionRequest = arrayOf(android.Manifest.permission.CAMERA)
            requestPermissions(permissionRequest, CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    private fun takePhoto() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
            takePictureIntent -> takePictureIntent.resolveActivity(requireContext().packageManager)
            if (takePictureIntent == null){
                binding.signupFragmentLayout.showToast("Unable to save photo")
            }
           val photoFile = createImageFile()
            photoFile.also {
               photoUri = FileProvider.getUriForFile(requireContext(),"com.decagon.mobifind.android.fileprovider",it)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri)
                startActivityForResult(takePictureIntent,SAVE_IMAGE_REQUEST_CODE)
            }
//            ?.also {
//                startActivityForResult(takePictureIntent,CAMERA_REQUEST_CODE)
//        }
        }
    }

    // Checks for the request code and
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    takePhoto()
                }else{
                    binding.signupFragmentLayout.showToast("Unable to take photo without permission")
                }
            }
            PICK_IMAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    changeImageFromGallery()
                } else {
                    binding.signupFragmentLayout.showSnackBar("Permission Denied")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK){
            when(requestCode){
                CAMERA_REQUEST_CODE -> {
                    val imageBitmap = data!!.extras!!.get("data") as Bitmap
                    binding.fragmentSignupUsersIv.setImageBitmap(imageBitmap)
                }
                PICK_IMAGE -> {
                    try {
                        imageUri = data?.data!!
//                        val source = ImageDecoder.createSource(requireActivity().contentResolver,
//                            imageUri!!
//                        )
//                        val bitmap = ImageDecoder.decodeBitmap(source)
//                        binding.fragmentSignupUsersIv.setImageBitmap(bitmap)
                        binding.fragmentSignupUsersIv.setImageURI(imageUri)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                SAVE_IMAGE_REQUEST_CODE -> {
                    val photo = Photo(localUri = photoUri.toString())
                    photos.add(photo)
                    binding.signupFragmentLayout.showToast("Image Saved")
                }
                AUTH_SIGN_IN->{
                    val response = IdpResponse.fromResultIntent(data)
                    user = FirebaseAuth.getInstance().currentUser
                    if (response == null){
                        return
                    }
                }
            }
        }
    }

    private fun createImageFile() : File {
        // Generate a unique file name with date
        val timestamp = SimpleDateFormat("yyyyMMMdd_HHmmss", Locale.ROOT).format(Date())
        // Get access to the directory where we can write pictures
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("Mobifind${timestamp}", ".jpg", storageDir?.apply {
            currentPhotoPath =  absolutePath
        })
    }

    private fun saveMobifundUser(){
        if (user == null){
            logon()
        }
        user ?: return
        var mobiUser = MobifindUser().apply {
            latitude = 67.8
            longitude = 45.6
            userName = "Adebayo"
            phoneNumber = "234809876452"
        }
        viewModel.save(mobiUser, photos, user!!)
        mobiUser = MobifindUser()
        photos = ArrayList()


    }

    private fun logon() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build(),
        )
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.homeland)
                .setTheme(R.style.AuthenticationTheme)
                .build(),
            AUTH_SIGN_IN)
    }

    companion object {
        private const val TAG = "SignupFragment"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}