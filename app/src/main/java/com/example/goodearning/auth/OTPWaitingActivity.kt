package com.example.goodearning.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.goodearning.R
import kotlinx.android.synthetic.main.activity_otpwaiting.*

class OTPWaitingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otpwaiting)

        otp_view.setOtpCompletionListener { otp ->
//            Toast.makeText(this, otp, Toast.LENGTH_SHORT).show()
//            startActivity(Intent(this, PointsActivity::class.java))
        }
    }
}
