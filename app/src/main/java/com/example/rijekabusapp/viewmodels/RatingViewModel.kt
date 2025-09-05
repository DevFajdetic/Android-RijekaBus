package com.example.rijekabusapp.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.rijekabusapp.helpers.getCurrentDateString
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class RatingViewModel(application: Application) : AndroidViewModel(application) {
    private val viewModelScope = CoroutineScope(Dispatchers.IO)
    private val database = FirebaseDatabase.getInstance("https://rijekabusapp-default-rtdb.europe-west1.firebasedatabase.app")
    private val ratingsRef = database.getReference("ratings")
    
    private val _averageRating = MutableLiveData<Float>(0f)
    val averageRating: LiveData<Float> = _averageRating
    
    private val _userCanRate = MutableLiveData<Boolean>(true)
    val userCanRate: LiveData<Boolean> = _userCanRate
    
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage
    
    // Keep track of rating listeners to remove them when no longer needed
    private var ratingListener: ValueEventListener? = null
    
    fun loadBusRating(voznjaBusId: String) {
        _isLoading.value = true
        
        // Remove previous listener if exists
        ratingListener?.let {
            val currentDate = getCurrentDateString()
            val dateRatingsRef = ratingsRef.child(currentDate)
            val busRatingsRef = dateRatingsRef.child(voznjaBusId)
            busRatingsRef.removeEventListener(it)
        }
        
        // Create new listener
        val currentDate = getCurrentDateString()
        val dateRatingsRef = ratingsRef.child(currentDate)
        val busRatingsRef = dateRatingsRef.child(voznjaBusId)
        
        ratingListener = busRatingsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val ratings = mutableListOf<Float>()
                    
                    for (ratingSnapshot in snapshot.children) {
                        val rating = ratingSnapshot.child("rating").getValue(Float::class.java) ?: 0f
                        ratings.add(rating)
                    }
                    
                    val average = if (ratings.isNotEmpty()) {
                        ratings.average().toFloat()
                    } else {
                        0f
                    }
                    
                    _averageRating.postValue(average)
                    _isLoading.postValue(false)
                } catch (e: Exception) {
                    Log.e("RatingViewModel", "Error calculating average rating", e)
                    _errorMessage.postValue("Error loading ratings: ${e.message}")
                    _isLoading.postValue(false)
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e("RatingViewModel", "Error loading bus ratings", error.toException())
                _errorMessage.postValue("Error loading ratings: ${error.message}")
                _isLoading.postValue(false)
            }
        })
    }
    
    fun checkIfUserCanRate(voznjaBusId: String, userId: String) {
        viewModelScope.launch {
            try {
                val currentDate = getCurrentDateString()
                val dateRatingsRef = ratingsRef.child(currentDate)
                val busRatingsRef = dateRatingsRef.child(voznjaBusId)
                val userRatingRef = busRatingsRef.child(userId)
                
                val snapshot = userRatingRef.get().await()
                _userCanRate.postValue(!snapshot.exists())
            } catch (e: Exception) {
                Log.e("RatingViewModel", "Error checking if user can rate", e)
                _errorMessage.postValue("Error checking rating status: ${e.message}")
            }
        }
    }
    
    fun rateBus(voznjaBusId: String, userId: String, username: String, rating: Float) {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val currentDate = getCurrentDateString()
                val ratingId = UUID.randomUUID().toString()
                
                val ratingMap = hashMapOf(
                    "id" to ratingId,
                    "voznjaBusId" to voznjaBusId,
                    "userId" to userId,
                    "username" to username,
                    "rating" to rating,
                    "timestamp" to Date().time,
                    "date" to currentDate
                )
                
                val dateRatingsRef = ratingsRef.child(currentDate)
                val busRatingsRef = dateRatingsRef.child(voznjaBusId)
                val userRatingRef = busRatingsRef.child(userId)
                
                userRatingRef.setValue(ratingMap).await()
                
                _userCanRate.postValue(false)
                _isLoading.postValue(false)
            } catch (e: Exception) {
                Log.e("RatingViewModel", "Error rating bus", e)
                _errorMessage.postValue("Error submitting rating: ${e.message}")
                _isLoading.postValue(false)
            }
        }
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
        _isLoading.value = false
    }
    
    override fun onCleared() {
        super.onCleared()
        
        // Remove rating listener when ViewModel is cleared
        ratingListener?.let {
            val currentDate = getCurrentDateString()
            val dateRatingsRef = ratingsRef.child(currentDate)
            // We don't have voznjaBusId here, but we can find the node where the listener is attached
            ratingsRef.removeEventListener(it)
        }
    }
} 