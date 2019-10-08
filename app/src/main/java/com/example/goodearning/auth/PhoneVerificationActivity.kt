package com.example.goodearning.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.goodearning.R
import kotlinx.android.synthetic.main.activity_phone_verification.*

class PhoneVerificationActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_verification)

        login_google_auth_btn.setOnClickListener {
            startActivity(Intent(this, TakeSelfieActivity::class.java))
        }
    }

}