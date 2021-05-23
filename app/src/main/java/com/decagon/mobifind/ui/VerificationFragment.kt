package com.decagon.mobifind.ui

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.decagon.mobifind.R
import com.decagon.mobifind.databinding.VerificationFragmentBinding
import com.decagon.mobifind.utils.showSnackBar
import com.decagon.mobifind.viewModel.VerificationViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class VerificationFragment : Fragment() {

    private var _binding : VerificationFragmentBinding? = null
    private val binding
    get() = _binding!!

    private val args : VerificationFragmentArgs by navArgs()

    private lateinit var mAuth : FirebaseAuth
    private lateinit var verificationId : String
    private lateinit var phoneNumber : String


    private lateinit var viewModel: VerificationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = VerificationFragmentBinding.inflate(layoutInflater)
        phoneNumber = args.phoneNumber
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(VerificationViewModel::class.java)
        mAuth = FirebaseAuth.getInstance()

        sendVerificationCode(phoneNumber)
        viewModel.signedInStatus.observe(viewLifecycleOwner,{
            if (it == "Successful Authentication"){
                findNavController().navigate(R.id.profileFragment)
            }
            binding.fragmentVerificationLayout.showSnackBar(it)
        })

        binding.fragmentVerificationSignInBtn.setOnClickListener {
            val code = binding.fragmentVerificationOTPEt.text.toString().trim()
            if (code.isEmpty() || code.length < 6){
                binding.fragmentVerificationOTPEt.apply {
                    error = "Enter Code..."
                }.also { it.requestFocus() }
                return@setOnClickListener
            }
           viewModel.verifyCode(code,verificationId)
        }

    }

    private fun sendVerificationCode(number : String){
        binding.fragmentVerificationProgressBar.visibility = View.VISIBLE
        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(number)
            .setTimeout(30L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                    val code = p0.smsCode
                    code?.let {
                        binding.fragmentVerificationOTPEt.setText(it)
                        viewModel.verifyCode(code,verificationId)
                    }
                }
                override fun onVerificationFailed(p0: FirebaseException) {
                    p0.message?.let { binding.fragmentVerificationLayout.showSnackBar(it)
                       }
                }

                override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                    super.onCodeSent(p0, p1)
                    verificationId = p0
                }


            }).build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

}