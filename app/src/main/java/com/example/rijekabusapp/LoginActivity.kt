package com.example.rijekabusapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieDrawable
import com.example.rijekabusapp.base.BaseActivity
import com.example.rijekabusapp.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class LoginActivity : BaseActivity() {
    // View binding
    private lateinit var binding: ActivityLoginBinding

    // Google Sign-In
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val firebaseAuth = FirebaseAuth.getInstance()

    // Firebase Database
    private val database = FirebaseDatabase.getInstance("https://rijekabusapp-default-rtdb.europe-west1.firebasedatabase.app")
    private val usersRef = database.getReference("users")

    // ActivityResultLauncher for Google Sign-In
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    // Firebase Auth Helper from Application
    private val firebaseAuthHelper by lazy { (application as RijekaBusApplication).firebaseAuthHelper }

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
            // Use our FirebaseAuthHelper for anonymous sign-in
            lifecycleScope.launch {
                try {
                    val success = firebaseAuthHelper.signInAnonymously()
                    if (success) {
                        // Successfully signed in anonymously
                        Log.d("login", "Anonymous sign-in successful")

                        // Save anonymous user to Firebase Database
                        saveAnonymousUserToDatabase()

                        startMainActivity()
                    } else {
                        Toast.makeText(this@LoginActivity, R.string.anonymous_login_failed, Toast.LENGTH_SHORT).show()
                        // Continue to MainActivity anyway
                        Log.e("login", "Anonymous sign-in failed")
                        startMainActivity()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    // Continue to MainActivity anyway
                    Log.e("login", "Error: ${e.message}")
                    startMainActivity()
                }
            }
        }
    }

    // Check if user is already signed in when the activity starts
    override fun onStart() {
        super.onStart()
        // Check if user is already signed in with Google
        if (GoogleSignIn.getLastSignedInAccount(this) != null) {
            Log.d("login", "GoogleSignIn.getLastSignedInAccount(this) != null")
            startMainActivity()
        }
        // Check if user is signed in with Firebase (could be anonymous)
        else if (firebaseAuthHelper.isUserSignedIn()) {
            Log.d("login", "firebaseAuthHelper user is signed in")
            startMainActivity()
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

                // Save Google user to Firebase Database
                lifecycleScope.launch {
                    saveGoogleUserToDatabase(account)
                }

                Log.d("login", "Google sign-in successful")
                startMainActivity()
            }
        }
    }

    // Save Google user data to Firebase Database
    private suspend fun saveGoogleUserToDatabase(account: GoogleSignInAccount) {
        try {
            val userId = firebaseAuth.currentUser?.uid ?: return
            val username = "${account.givenName} ${account.familyName}".trim()

            val userMap = hashMapOf(
                "id" to userId,
                "username" to username,
                "email" to (account.email ?: ""),
                "photoUrl" to (account.photoUrl?.toString() ?: ""),
                "givenName" to (account.givenName ?: ""),
                "familyName" to (account.familyName ?: ""),
                "lastSeen" to Date().time,
                "isAnonymous" to false,
                "createdAt" to Date().time
            )

            usersRef.child(userId).setValue(userMap).await()
            Log.d("login", "Google user saved to database successfully")

        } catch (e: Exception) {
            Log.e("login", "Error saving Google user to database", e)
        }
    }

    // Save anonymous user data to Firebase Database
    private suspend fun saveAnonymousUserToDatabase() {
        try {
            val userId = firebaseAuth.currentUser?.uid ?: return
            val username = "Anonymous User ${userId.take(6)}" // Create a simple anonymous username

            val userMap = hashMapOf(
                "id" to userId,
                "username" to username,
                "email" to "",
                "photoUrl" to "",
                "givenName" to "",
                "familyName" to "",
                "lastSeen" to Date().time,
                "isAnonymous" to true,
                "createdAt" to Date().time
            )

            usersRef.child(userId).setValue(userMap).await()
            Log.d("login", "Anonymous user saved to database successfully")

        } catch (e: Exception) {
            Log.e("login", "Error saving anonymous user to database", e)
        }
    }

    // Helper method to start MainActivity
    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}