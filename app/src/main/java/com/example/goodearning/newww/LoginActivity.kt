package com.example.goodearning.newww

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.goodearning.PhoneVerificationActivity
import com.example.goodearning.R
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_google_auth_btn.setOnClickListener {
            startActivity(Intent(this, PhoneVerificationActivity::class.java))
        }
    }

}
