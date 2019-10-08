package com.example.goodearning.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.goodearning.R
import com.example.goodearning.nav.MainActivity

class SplashActivity : AppCompatActivity() {

    //TODO -> Modifies State
    var loggedInAlready = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        /* Handles Where to Go from Splash Screen */
        Handler().postDelayed({
            when (loggedInAlready) {
                true -> takeToMainScreen()
                false -> takeToLoginScreen()
            }
        }, 2000)
    }

    /* Intent for Main Screen */
    private fun takeToMainScreen() {
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    /* Intent for Login Screen */
    private fun takeToLoginScreen() {
        startActivity(Intent(this, LoginActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

}
