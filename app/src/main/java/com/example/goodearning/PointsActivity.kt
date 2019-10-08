package com.example.goodearning

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_points.*

class PointsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_points)

        redeem_more_btn.setOnClickListener {
            startActivity(Intent(this, ReferralActivity::class.java))
        }
    }
}
