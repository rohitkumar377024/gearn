package com.example.goodearning.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.goodearning.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_phone_verification.*
import java.util.concurrent.TimeUnit
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import android.content.Intent

class PhoneVerificationActivity: AppCompatActivity() {

    /* Shared Instance of FirebaseAuth */
    private lateinit var auth: FirebaseAuth

    /* Get updates about the state of the verification process */
    private lateinit var mCallback: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private var verificationCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_verification)

        /* Getting the Instance of FirebaseAuth Just Once in OnCreate() */
        auth = FirebaseAuth.getInstance()

        mCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                Toast.makeText(baseContext, "Verification Completed", Toast.LENGTH_SHORT).show()
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(baseContext, "Verification Failed", Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(s: String, forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(s, forceResendingToken)
                verificationCode = s
                Toast.makeText(baseContext, "OTP Code Sent", Toast.LENGTH_SHORT).show()
            }
        }

        phone_send_otp_btn.setOnClickListener {
            val phoneNumber= phone_verification_mobile_number_edittext.text.toString()

            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,                     // Phone number to verify
                60,                              // Timeout duration
                TimeUnit.SECONDS,                // Unit of timeout
                this,                            // Activity (for callback binding)
                mCallback)                       // OnVerificationStateChangedCallbacks
        }

        phone_verify_otp_btn.setOnClickListener {
            val otp = phone_enter_otp_edittext.text.toString();
            val credential = PhoneAuthProvider.getCredential(verificationCode!!, otp)
            signInWithPhone(credential)
        }
    }

    private fun signInWithPhone(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            when {
                task.isSuccessful -> { startActivity(Intent(baseContext, ProfileSetupActivity::class.java)); finish() }
                else -> Toast.makeText(baseContext, "Incorrect OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }

}