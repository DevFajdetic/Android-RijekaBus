package com.example.rijekabusapp.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.rijekabusapp.database.models.ObjectType
import com.example.rijekabusapp.helpers.getCurrentDateString
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

class CommentViewModel(application: Application) : AndroidViewModel(application) {
    private val database = FirebaseDatabase.getInstance("https://rijekabusapp-default-rtdb.europe-west1.firebasedatabase.app")
    private val commentsRef = database.getReference("comments")
    
    private val _comments = MutableLiveData<List<Map<String, Any>>>(emptyList())
    val comments: LiveData<List<Map<String, Any>>> = _comments
    
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage
    
    // Keep track of comments listener to remove when no longer needed
    private var commentsListener: ValueEventListener? = null
    
    fun loadComments(objectId: String, objectType: ObjectType) {
        _isLoading.value = true
        
        // Remove previous listener if exists
        commentsListener?.let {
            val currentDate = getCurrentDateString()
            val dateCommentsRef = commentsRef.child(currentDate)
            val typeCommentsRef = dateCommentsRef.child(objectType.name)
            val objectCommentsRef = typeCommentsRef.child(objectId)
            objectCommentsRef.removeEventListener(it)
        }
        
        // Create new listener
        val currentDate = getCurrentDateString()
        val dateCommentsRef = commentsRef.child(currentDate)
        val typeCommentsRef = dateCommentsRef.child(objectType.name)
        val objectCommentsRef = typeCommentsRef.child(objectId)
        
        commentsListener = objectCommentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val commentsList = mutableListOf<Map<String, Any>>()
                    
                    for (commentSnapshot in snapshot.children) {
                        val comment = mutableMapOf<String, Any>()
                        comment["id"] = commentSnapshot.key ?: ""
                        comment["objectId"] = commentSnapshot.child("objectId").getValue(String::class.java) ?: ""
                        comment["objectType"] = commentSnapshot.child("objectType").getValue(String::class.java) ?: ""
                        comment["userId"] = commentSnapshot.child("userId").getValue(String::class.java) ?: ""
                        comment["username"] = commentSnapshot.child("username").getValue(String::class.java) ?: ""
                        comment["comment"] = commentSnapshot.child("comment").getValue(String::class.java) ?: ""
                        comment["timestamp"] = commentSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                        comment["date"] = commentSnapshot.child("date").getValue(String::class.java) ?: ""
                        comment["likes"] = commentSnapshot.child("likes").getValue(Int::class.java) ?: 0
                        
                        // Get the likedBy map if it exists
                        val likedBySnapshot = commentSnapshot.child("likedBy")
                        val likedBy = mutableMapOf<String, Boolean>()
                        if (likedBySnapshot.exists()) {
                            for (likedByUser in likedBySnapshot.children) {
                                likedBy[likedByUser.key ?: ""] = likedByUser.getValue(Boolean::class.java) ?: false
                            }
                        }
                        comment["likedBy"] = likedBy
                        
                        commentsList.add(comment)
                    }
                    
                    // Sort by timestamp descending (newest first)
                    commentsList.sortByDescending { it["timestamp"] as Long }
                    
                    _comments.postValue(commentsList)
                    _isLoading.postValue(false)
                } catch (e: Exception) {
                    Log.e("CommentViewModel", "Error processing comments", e)
                    _errorMessage.postValue("Error loading comments: ${e.message}")
                    _isLoading.postValue(false)
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e("CommentViewModel", "Error loading comments", error.toException())
                _errorMessage.postValue("Error loading comments: ${error.message}")
                _isLoading.postValue(false)
            }
        })
    }
    
    fun addComment(objectId: String, objectType: ObjectType, userId: String, username: String, commentText: String) {
        if (commentText.isBlank()) {
            _errorMessage.value = "Comment cannot be empty"
            return
        }
        
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val currentDate = getCurrentDateString()
                val commentId = UUID.randomUUID().toString()
                
                val commentMap = hashMapOf(
                    "id" to commentId,
                    "objectId" to objectId,
                    "objectType" to objectType.name,
                    "userId" to userId,
                    "username" to username,
                    "comment" to commentText,
                    "timestamp" to Date().time,
                    "date" to currentDate,
                    "likes" to 0
                )
                
                val dateCommentsRef = commentsRef.child(currentDate)
                val typeCommentsRef = dateCommentsRef.child(objectType.name)
                val objectCommentsRef = typeCommentsRef.child(objectId)
                val commentRef = objectCommentsRef.child(commentId)
                
                commentRef.setValue(commentMap).await()
                
                _isLoading.postValue(false)
            } catch (e: Exception) {
                Log.e("CommentViewModel", "Error adding comment", e)
                _errorMessage.postValue("Error adding comment: ${e.message}")
                _isLoading.postValue(false)
            }
        }
    }
    
    fun likeComment(commentMap: Map<String, Any>) {
        val commentId = commentMap["id"] as String
        val objectId = commentMap["objectId"] as String
        val objectTypeStr = commentMap["objectType"] as String
        val objectType = ObjectType.valueOf(objectTypeStr)
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentDate = getCurrentDateString()
                val dateCommentsRef = commentsRef.child(currentDate)
                val typeCommentsRef = dateCommentsRef.child(objectType.name)
                val objectCommentsRef = typeCommentsRef.child(objectId)
                val commentRef = objectCommentsRef.child(commentId)
                
                // Get current user ID
                val userId = "user123" // This should be retrieved from your auth system
                
                // Check if user already liked
                val userLikeRef = commentRef.child("likedBy").child(userId)
                val snapshot = userLikeRef.get().await()
                
                if (snapshot.exists()) {
                    // User already liked, remove the like
                    userLikeRef.removeValue().await()
                    
                    // Update like count
                    val likesRef = commentRef.child("likes")
                    val currentLikes = likesRef.get().await().getValue(Int::class.java) ?: 0
                    likesRef.setValue(currentLikes - 1).await()
                } else {
                    // User hasn't liked, add the like
                    userLikeRef.setValue(true).await()
                    
                    // Update like count
                    val likesRef = commentRef.child("likes")
                    val currentLikes = likesRef.get().await().getValue(Int::class.java) ?: 0
                    likesRef.setValue(currentLikes + 1).await()
                }
            } catch (e: Exception) {
                Log.e("CommentViewModel", "Error liking comment", e)
                _errorMessage.postValue("Error liking comment: ${e.message}")
            }
        }
    }
    
    fun deleteComment(commentMap: Map<String, Any>) {
        val commentId = commentMap["id"] as String
        val objectId = commentMap["objectId"] as String
        val objectTypeStr = commentMap["objectType"] as String
        val objectType = ObjectType.valueOf(objectTypeStr)
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentDate = getCurrentDateString()
                val dateCommentsRef = commentsRef.child(currentDate)
                val typeCommentsRef = dateCommentsRef.child(objectType.name)
                val objectCommentsRef = typeCommentsRef.child(objectId)
                val commentRef = objectCommentsRef.child(commentId)
                
                commentRef.removeValue().await()
            } catch (e: Exception) {
                Log.e("CommentViewModel", "Error deleting comment", e)
                _errorMessage.postValue("Error deleting comment: ${e.message}")
            }
        }
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
        
        // Remove comments listener when ViewModel is cleared
        commentsListener?.let {
            // We don't have objectId and objectType here, but Firebase will find the node
            commentsRef.removeEventListener(it)
        }
    }
} 