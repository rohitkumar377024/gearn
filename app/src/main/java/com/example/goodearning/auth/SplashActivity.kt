package com.example.goodearning.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.example.goodearning.R
import com.example.goodearning.nav.MainActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    /* Handles Logged-In-Or-Not State */
    private var loggedInAlready: Boolean? = null

    /* Shared Instance of FirebaseAuth */
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        /* Getting the Instance of FirebaseAuth Just Once in OnCreate() */
        auth = FirebaseAuth.getInstance()

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

    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            //Take to Login Screen
            loggedInAlready = false
            Log.d("splash_activity", "not logged in already")
        } else {
            //Take to Main Screen
            loggedInAlready = true
            Log.d("splash_activity", "logged in already")
        }
    }

}
