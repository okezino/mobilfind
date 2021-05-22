package com.decagon.mobifind.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.decagon.mobifind.R
import com.decagon.mobifind.databinding.FragmentWelcomeBinding
import com.decagon.mobifind.utils.AUTH_SIGN_IN
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth


class WelcomeFragment : Fragment() {
    private var _binding: FragmentWelcomeBinding? = null
    private val binding
        get() = _binding!!

    override fun onStart() {
        super.onStart()
        // Launches profile fragment if user is signed In already
        if(FirebaseAuth.getInstance().currentUser != null){
            findNavController().navigate(R.id.profileFragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            loginInFirebase()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun signInPhoneNumberFirebaseUI() {
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



}