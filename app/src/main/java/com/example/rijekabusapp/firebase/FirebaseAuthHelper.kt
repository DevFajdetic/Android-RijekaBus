package com.example.rijekabusapp.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class FirebaseAuthHelper {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getUserId(): String {
        return auth.currentUser?.uid ?: "anonymous"
    }

    fun getUsername(): String {
        return auth.currentUser?.displayName ?: "Anonymous User"
    }


    suspend fun updateUserProfile(displayName: String): Boolean {
        return try {
            val user = auth.currentUser ?: return false
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()

            user.updateProfile(profileUpdates).await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseAuthHelper", "Error updating user profile", e)
            false
        }
    }

    suspend fun signInAnonymously(): Boolean {
        return try {
            val result = auth.signInAnonymously().await()
            result.user != null
        } catch (e: Exception) {
            Log.e("FirebaseAuthHelper", "Error signing in anonymously", e)
            false
        }
    }

    suspend fun signInWithEmailAndPassword(email: String, password: String): Boolean {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user != null
        } catch (e: Exception) {
            Log.e("FirebaseAuthHelper", "Error signing in with email/password", e)
            false
        }
    }

    suspend fun createUserWithEmailAndPassword(email: String, password: String, displayName: String): Boolean {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            if (result.user != null) {
                updateUserProfile(displayName)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthHelper", "Error creating user with email/password", e)
            false
        }
    }

    fun signOut() {
        auth.signOut()
    }


} 