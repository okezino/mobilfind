package com.decagon.mobifind.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.decagon.mobifind.utils.PICK_IMAGE
import com.decagon.mobifind.databinding.SignupFragmentBinding
import com.decagon.mobifind.utils.showSnackBar
import com.decagon.mobifind.viewModel.SignupViewModel
import java.io.IOException
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import com.decagon.mobifind.R
import com.decagon.mobifind.utils.countryCodes
import com.decagon.mobifind.utils.listOfCountries
import com.google.firebase.auth.FirebaseAuth


class SignupFragment : Fragment() {
    private var _binding : SignupFragmentBinding? = null
    private val binding
    get() = _binding!!


    private lateinit var viewModel: SignupViewModel
    private var imageUri: Uri? = null

    override fun onStart() {
        super.onStart()
        if(FirebaseAuth.getInstance().currentUser != null){
            findNavController().navigate(R.id.profileFragment)
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
            changeImageFromGallery()
        }

        binding.fragmentSignupSpinner.adapter = ArrayAdapter(requireContext(),android.R.layout.simple_spinner_dropdown_item,
            listOfCountries)

        binding.fragmentSignupGetOTPBtn.setOnClickListener { 
            val code = countryCodes[binding.fragmentSignupSpinner.selectedItemPosition]
            val number = binding.fragmentSignupPhoneNumberEt.text.toString().trim()

            if (number.isEmpty() || number.length < 10) {
                binding.fragmentSignupPhoneNumberEt.error = "Valid number is required"
                binding.fragmentSignupPhoneNumberEt.requestFocus()
                return@setOnClickListener
            }

            val phoneNumber = "+$code$number"
            val action = SignupFragmentDirections.actionSignupFragmentToVerificationFragment(phoneNumber)
            findNavController().navigate(action)

            Log.d(TAG, "onViewCreated: $phoneNumber")

        }





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

    // Checks for the request code and
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PICK_IMAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                changeImageFromGallery()
            } else {
                binding.signupFragmentLayout.showSnackBar("Permission Denied")

            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {

            try {
                imageUri = data?.data!!
                binding.fragmentSignupUsersIv.setImageURI(imageUri)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    companion object {
        private const val TAG = "SignupFragment"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}