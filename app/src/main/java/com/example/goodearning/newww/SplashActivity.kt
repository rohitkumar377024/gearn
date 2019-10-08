package com.example.goodearning.newww

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.example.goodearning.R

class SplashActivity : AppCompatActivity() {

    var loggedInAlready = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            when (loggedInAlready) {
                true -> takeToMainScreen()
                false -> takeToLoginScreen()
            }
        }, 2000)
    }

    private fun takeToMainScreen() {
        Log.d("splash_activity", "logged in already")
        //todo -> Take to MainActivity once Created Later
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun takeToLoginScreen() {
        Log.d("splash_activity", "take to login screen")
        startActivity(Intent(this, LoginActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

}
