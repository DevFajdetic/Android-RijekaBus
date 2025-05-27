package com.example.rijekabusapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.airbnb.lottie.LottieDrawable
import com.example.rijekabusapp.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    // View binding
    private lateinit var binding: ActivityLoginBinding

    // Google Sign-In
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val firebaseAuth = FirebaseAuth.getInstance()

    // ActivityResultLauncher for Google Sign-In
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        // Login bus animation
        binding.loginImage.apply {
            repeatCount = LottieDrawable.INFINITE
            speed = 0.5f
            scaleX = 1.4f
            scaleY = 1.3f
            postDelayed({ animate() }, 1000)
        }

        // Switch theme from Splash Screen to postSplashScreen
        installSplashScreen()
        supportActionBar?.hide()

        // Bind view
        setContentView(binding.root)

        // Google sign-in options
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id)) // Replace with your actual web client ID
                .requestEmail()
                .requestProfile()
                .build()

        // Initialize GoogleSignInClient
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set up the ActivityResultLauncher for Google Sign-In
        googleSignInLauncher =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult(),
            ) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    handleResult(task)
                } else {
                    Log.d("login", result.toString())
                    Toast.makeText(this, R.string.google_login_failed, Toast.LENGTH_SHORT).show()
                }
            }

        // Set click listeners for buttons
        binding.googleSignIn.setOnClickListener {
            signInGoogle()
        }

        binding.anonymousSignIn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    // Check if user is already signed in when the activity starts
    override fun onStart() {
        super.onStart()
        if (GoogleSignIn.getLastSignedInAccount(this) != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    // Google Sign-In function using ActivityResultLauncher
    private fun signInGoogle() {
        val signInIntent = mGoogleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    // Handle the result of Google Sign-In
    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                updateUI(account)
            }
        } catch (e: ApiException) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    // Update UI after successful Google Sign-In
    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                SavedPreference.setEmail(this, account.email.toString())
                SavedPreference.setPictureUrl(this, account.photoUrl.toString())
                SavedPreference.setGivenName(this, account.givenName.toString())
                SavedPreference.setFamilyName(this, account.familyName.toString())
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}
