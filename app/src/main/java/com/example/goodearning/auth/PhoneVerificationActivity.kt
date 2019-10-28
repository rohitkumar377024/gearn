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
import android.view.View
import com.heetch.countrypicker.CountryPickerCallbacks
import com.heetch.countrypicker.CountryPickerDialog

class PhoneVerificationActivity: AppCompatActivity() {

    /* Shared Instance of FirebaseAuth */
    private lateinit var auth: FirebaseAuth

    /* Get updates about the state of the verification process */
    private lateinit var mCallback: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private var verificationCode: String? = null

    private var dialingCode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_verification)

        /* Getting the Instance of FirebaseAuth Just Once in OnCreate() */
        auth = FirebaseAuth.getInstance()

        /* Show Send OTP LL and Hide Verify OTP LL at Start */
        showSendOTPLL()
        hideVerifyOTPLL()

        /* Country Picker Functionality */
        phone_verification_country_picker_button.setOnClickListener {
            val countryPicker = CountryPickerDialog(this, CountryPickerCallbacks { country, flagResId ->
                phone_verification_country_picker_button.text = "Dialing Code: +${country.dialingCode}, ISO Code: ${country.isoCode}"
                dialingCode = country.dialingCode.toInt()
            })
            countryPicker.show()
        }

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

                /* Show Verify OTP LL and Hide Send OTP LL Now Once OTP Code Sent */
                showVerifyOTPLL()
                hideSendOTPLL()
            }
        }

        phone_send_otp_btn.setOnClickListener {
            val phoneNumber= phone_verification_mobile_number_edittext.text.toString()

            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+${dialingCode}$phoneNumber",    // Phone number to verify
                60,                              // Timeout duration
                TimeUnit.SECONDS,                // Unit of timeout
                this,                            // Activity (for callback binding)
                mCallback)                       // OnVerificationStateChangedCallbacks
        }

        phone_verify_otp_btn.setOnClickListener {
            val otp = phone_enter_otp_edittext.text.toString()
            val credential = PhoneAuthProvider.getCredential(verificationCode!!, otp)
            signInWithPhone(credential)
        }
    }

    private fun signInWithPhone(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            when {
                task.isSuccessful -> {
                    Toast.makeText(baseContext, "OTP Verified Successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(baseContext, ProfileSetupActivity::class.java)); finish() }
                else -> Toast.makeText(baseContext, "Incorrect OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /* Layout Visibility Helper Methods */
    private fun showSendOTPLL() { send_otp_ll.visibility = View.VISIBLE }
    private fun hideSendOTPLL() { send_otp_ll.visibility = View.GONE }
    private fun showVerifyOTPLL() { phone_verify_otp_ll.visibility = View.VISIBLE }
    private fun hideVerifyOTPLL() { phone_verify_otp_ll.visibility = View.GONE }
}