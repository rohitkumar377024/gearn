package com.example.goodearning.auth

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.goodearning.R
import kotlinx.android.synthetic.main.activity_login.*
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.api.ApiException

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private val RC_SIGN_IN: Int = 111

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        /* Setting Up Click Listeners */
        login_google_auth_btn.setOnClickListener(this)
    }

    /* Handles Click for All Buttons on this Screen */
    override fun onClick(v: View?) {
        when (v) {
            login_google_auth_btn -> googleSignIn() /* Google Sign In */
        }
    }

    /* Handles Google Sign In Procedure */
    private fun googleSignIn() {
        /* Configure sign-in to request the user's ID, email address, and basic profile. ID and basic profile are included in DEFAULT_SIGN_IN. */
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1075878459414-1vc1s7m1gjb8qpq49bhm0c2sg3a3ojud.apps.googleusercontent.com") //todo -> place it in a central location
            .requestEmail()
            .build()

        /* Build a GoogleSignInClient with the options specified by gso. */
        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            Log.d("login_activity -> acc", "${account?.displayName}")
            Log.d("login_activity -> acc", "${account?.email}")
            Log.d("login_activity -> acc", "${account?.familyName}")
            Log.d("login_activity -> acc", "${account?.givenName}")
            Log.d("login_activity -> acc", "${account?.id}")
            Log.d("login_activity -> acc", "${account?.idToken}")
            Log.d("login_activity -> acc", "${account?.photoUrl}")

            //TODO -> UPDATE UI
            // Signed in successfully, show authenticated UI.
            //updateUI(account)
            Toast.makeText(this, "Signed In Successfully.", Toast.LENGTH_SHORT).show()
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("login_activity", "signInResult:failed code=" + e.statusCode)
            //TODO -> UPDATE UI
            //updateUI()
            Toast.makeText(this, "Signed In Failed.", Toast.LENGTH_SHORT).show()
        }
    }

}
