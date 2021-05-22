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
import android.content.Intent
import com.firebase.ui.auth.AuthUI

import com.google.firebase.auth.FirebaseUser




class ProfileFragment : Fragment() {
    private var _binding : ProfileFragmentBinding? = null
    private val binding
    get() = _binding!!

    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ProfileFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
         //   startActivity(Intent(this, FirebaseAuthActivity::class.java))
            return
        }

        binding.fragmentProfileSignOut.setOnClickListener {
            AuthUI.getInstance().signOut(requireActivity()).addOnCompleteListener {
                if (it.isSuccessful){
                    findNavController().navigate(R.id.signupFragment)
                }
            }
         //   AuthUI.getInstance().delete(requireActivity()).addOnCompleteListener {  }

        }


    }
}