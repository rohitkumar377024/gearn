package com.example.goodearning

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_take_selfie_activity.*

class TakeSelfieActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_take_selfie_activity)

        auth_login_loginbutton2.setOnClickListener {
            startActivity(Intent(this, OTPWaitingActivity::class.java))
        }
    }
}
