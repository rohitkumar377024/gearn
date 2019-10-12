package com.example.goodearning.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.goodearning.R
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_phone_verification.*
import java.util.concurrent.TimeUnit
import android.widget.Toast

class PhoneVerificationActivity: AppCompatActivity(), View.OnClickListener {

    /* Shared Instance of FirebaseAuth */
    private lateinit var auth: FirebaseAuth

    //Get updates about the state of the verification process
    private lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private lateinit var mVerificationId: String
    private lateinit var mResendToken: PhoneAuthProvider.ForceResendingToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_verification)

        /* Getting the Instance of FirebaseAuth Just Once in OnCreate() */
        auth = FirebaseAuth.getInstance()

        initFireBaseCallbacks()

        phone_send_otp_btn.setOnClickListener(this)
        phone_resend_otp_btn.setOnClickListener(this)
        phone_verify_otp_btn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            phone_send_otp_btn -> {
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phone_verification_mobile_number_edittext.text.toString(),  // Phone number to verify
                    1,                                                          // Timeout duration
                    TimeUnit.MINUTES,                                           // Unit of timeout
                    this,                                                       // Activity (for callback binding)
                    mCallbacks)                                                 // OnVerificationStateChangedCallbacks
            }
            phone_resend_otp_btn -> {
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phone_verification_mobile_number_edittext.text.toString(),  // Phone number to verify
                    1,                                                          // Timeout duration
                    TimeUnit.MINUTES,                                           // Unit of timeout
                    this,                                                       // Activity (for callback binding)
                    mCallbacks,                                                 // OnVerificationStateChangedCallbacks
                    mResendToken)                                               // Force Resending Token from callbacks
            }
            phone_verify_otp_btn -> {
                val credential = PhoneAuthProvider.getCredential(mVerificationId, phone_enter_otp_edittext.text.toString())
                auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(applicationContext, "Verification Success", Toast.LENGTH_SHORT).show()
                        val user = task.result?.user

                        startActivity(Intent(this, ProfileSetupActivity::class.java))

                        //...
                    } else {
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(applicationContext, "Verification Failed, Invalid credentials", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    fun initFireBaseCallbacks() {
        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Toast.makeText(applicationContext, "Verification Complete", Toast.LENGTH_SHORT).show()
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(applicationContext, "Verification Failed", Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                Toast.makeText(applicationContext, "Code Sent", Toast.LENGTH_SHORT).show()
                mVerificationId = verificationId
                mResendToken = token
            }
        }
    }

//        otp_view.setOtpCompletionListener { otp ->
//            //            Toast.makeText(this, otp, Toast.LENGTH_SHORT).show()
////            startActivity(Intent(this, PointsActivity::class.java))
//        }

//        //todo -> do validation for phone number before later on
//        phone_send_otp_btn.setOnClickListener {
//
//            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
//                    // This callback will be invoked in two situations:
//                    // 1 - Instant verification. In some cases the phone number can be instantly
//                    //     verified without needing to send or enter a verification code.
//                    // 2 - Auto-retrieval. On some devices Google Play services can automatically
//                    //     detect the incoming verification SMS and perform verification without
//                    //     user action.
//                    Log.d("phone_verif_activity", "onVerificationCompleted:$credential")
//
//                    signInWithPhoneAuthCredential(credential)
//                }
//
//                override fun onVerificationFailed(e: FirebaseException) {
//                    // This callback is invoked in an invalid request for verification is made,
//                    // for instance if the the phone number format is not valid.
//                    Log.w("phone_verif_activity", "onVerificationFailed", e)
//
//                    if (e is FirebaseAuthInvalidCredentialsException) {
//                        // Invalid request
//                        // ...
//                    } else if (e is FirebaseTooManyRequestsException) {
//                        // The SMS quota for the project has been exceeded
//                        // ...
//                    }
//
//                    // Show a message and update the UI
//                    // ...
//                }
//
//                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
//                    // The SMS verification code has been sent to the provided phone number, we
//                    // now need to ask the user to enter the code and then construct a credential
//                    // by combining the code with a verification ID.
//                    Log.d("phone_verif_activity", "onCodeSent:$verificationId")
//
//                    // Save verification ID and resending token so we can use them later
//                    val storedVerificationId = verificationId
//                    val resendToken = token
//
//                    // ...
//                }
//            }
//
//            val phoneNumber = phone_verification_mobile_number_edittext.text.toString()
//            PhoneAuthProvider.getInstance().verifyPhoneNumber(
//                phoneNumber, // Phone number to verify
//                60, // Timeout duration
//                TimeUnit.SECONDS, // Unit of timeout
//                this, // Activity (for callback binding)
//                callbacks) // OnVerificationStateChangedCallbacks
//        }

//
//    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
//        auth.signInWithCredential(credential)
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    // Sign in success, update UI with the signed-in user's information
//                    Log.d("phone_verif_activity", "signInWithCredential:success")
//
//                    val user = task.result?.user
//                    // ...
//                } else {
//                    // Sign in failed, display a message and update the UI
//                    Log.w("phone_verif_activity", "signInWithCredential:failure", task.exception)
//                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
//                        // The verification code entered was invalid
//                    }
//                }
//            }
//    }

}