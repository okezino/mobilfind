package com.decagon.mobifind.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class VerificationViewModel : ViewModel() {

    private var firebaseAuth : FirebaseAuth = FirebaseAuth.getInstance()
    private var _signedInStatus = MutableLiveData<String>()
    val signedInStatus : LiveData<String>
    get() = _signedInStatus

    private fun signInWithCredential(credential: PhoneAuthCredential){
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    _signedInStatus.value = "Successful Authentication"
                }else{
                    it.exception?.message?.let { it1 ->
                        _signedInStatus.value = it1
                    }
                }
            }
    }


    /**
     * Verifies received code and calls sign in with credential
     */
    fun verifyCode(code: String, verificationId : String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithCredential(credential)
    }

}