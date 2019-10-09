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
import com.example.goodearning.nav.MainActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        /* Used for Google Sign In */
        const val RC_SIGN_IN: Int = 111
    }

    /* Shared Instance of FirebaseAuth */
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        /* Getting the Instance of FirebaseAuth Just Once in OnCreate() */
        auth = FirebaseAuth.getInstance()

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

    /* Handling Result */
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
                Toast.makeText(this, "Google Sign In Successful. Processing, Please Wait.", Toast.LENGTH_LONG).show()
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("login_activity", "Google sign in failed", e)
                Toast.makeText(this, "Google Sign In Failed.", Toast.LENGTH_LONG).show()
                // ...
            }
        }
    }

    /* Firebase Authentication with Google Sign In */
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d("login_activity", "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("login_activity", "signInWithCredential:success")
                    Snackbar.make(login_google_auth_btn, "Authentication Successful.", Snackbar.LENGTH_SHORT).show()
                    val user = auth.currentUser

                    Log.d("login_activity -> acc", "Display Name -> ${acct.displayName}")
                    Log.d("login_activity -> acc", "Email -> ${acct.email}")
                    Log.d("login_activity -> acc", "Photo Url -> ${acct.photoUrl}")

                    Log.d("login_activity -> acc", "Display Name USER -> ${user?.displayName}")
                    Log.d("login_activity -> acc", "Email USER-> ${user?.email}")
                    Log.d("login_activity -> acc", "Photo Url USER-> ${user?.photoUrl}")
                    Log.d("login_activity -> acc", "Phone Number USER-> ${user?.phoneNumber}")

                    /* Go to Main Screen Now once Successfully Authenticated */
                    startActivity(Intent(this, MainActivity::class.java))
//                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("login_activity", "signInWithCredential:failure", task.exception)
                    Snackbar.make(login_google_auth_btn, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
//                    updateUI(null)
                }

                // ...
            }
    }

}
