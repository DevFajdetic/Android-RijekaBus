package com.example.rijekabusapp.firebase

import android.util.Log
import com.example.rijekabusapp.database.models.ObjectType
import com.example.rijekabusapp.firebase.models.FirebaseBusRating
import com.example.rijekabusapp.firebase.models.FirebaseChatMessage
import com.example.rijekabusapp.firebase.models.FirebaseComment
import com.example.rijekabusapp.helpers.getCurrentDateString
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebaseRepository {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://rijekabusapp-default-rtdb.europe-west1.firebasedatabase.app")
    private val ratingsRef: DatabaseReference = database.getReference("ratings")
    private val commentsRef: DatabaseReference = database.getReference("comments")
    private val chatMessagesRef: DatabaseReference = database.getReference("chatMessages")
    
    // Bus Ratings
    suspend fun addBusRating(voznjaBusId: String, userId: String, username: String, rating: Float): Boolean {
        return try {
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
            true
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error adding bus rating", e)
            false
        }
    }
    
    suspend fun getBusRatingsForToday(voznjaBusId: String): List<Map<String, Any>> {
        return try {
            val currentDate = getCurrentDateString()
            val dateRatingsRef = ratingsRef.child(currentDate)
            val busRatingsRef = dateRatingsRef.child(voznjaBusId)
            
            suspendCoroutine { continuation ->
                busRatingsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val ratingsList = mutableListOf<Map<String, Any>>()
                        for (ratingSnapshot in snapshot.children) {
                            val rating = mutableMapOf<String, Any>()
                            rating["id"] = ratingSnapshot.child("id").getValue(String::class.java) ?: ""
                            rating["voznjaBusId"] = ratingSnapshot.child("voznjaBusId").getValue(String::class.java) ?: ""
                            rating["userId"] = ratingSnapshot.child("userId").getValue(String::class.java) ?: ""
                            rating["username"] = ratingSnapshot.child("username").getValue(String::class.java) ?: ""
                            rating["rating"] = ratingSnapshot.child("rating").getValue(Float::class.java) ?: 0f
                            rating["timestamp"] = ratingSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                            rating["date"] = ratingSnapshot.child("date").getValue(String::class.java) ?: ""
                            
                            ratingsList.add(rating)
                        }
                        continuation.resume(ratingsList)
                    }
                    
                    override fun onCancelled(error: DatabaseError) {
                        continuation.resumeWithException(error.toException())
                    }
                })
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting bus ratings", e)
            emptyList()
        }
    }
    
    // Get ratings with a callback for non-coroutine contexts
    fun getBusRatingsForTodayAsync(voznjaBusId: String, callback: (List<Map<String, Any>>) -> Unit) {
        val currentDate = getCurrentDateString()
        val dateRatingsRef = ratingsRef.child(currentDate)
        val busRatingsRef = dateRatingsRef.child(voznjaBusId)
        
        busRatingsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ratingsList = mutableListOf<Map<String, Any>>()
                for (ratingSnapshot in snapshot.children) {
                    val rating = mutableMapOf<String, Any>()
                    rating["id"] = ratingSnapshot.child("id").getValue(String::class.java) ?: ""
                    rating["voznjaBusId"] = ratingSnapshot.child("voznjaBusId").getValue(String::class.java) ?: ""
                    rating["userId"] = ratingSnapshot.child("userId").getValue(String::class.java) ?: ""
                    rating["username"] = ratingSnapshot.child("username").getValue(String::class.java) ?: ""
                    rating["rating"] = ratingSnapshot.child("rating").getValue(Float::class.java) ?: 0f
                    rating["timestamp"] = ratingSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                    rating["date"] = ratingSnapshot.child("date").getValue(String::class.java) ?: ""
                    
                    ratingsList.add(rating)
                }
                callback(ratingsList)
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepository", "Error getting bus ratings", error.toException())
                callback(emptyList())
            }
        })
    }
    
    suspend fun hasUserRatedBusToday(voznjaBusId: String, userId: String): Boolean {
        return try {
            val currentDate = getCurrentDateString()
            val dateRatingsRef = ratingsRef.child(currentDate)
            val busRatingsRef = dateRatingsRef.child(voznjaBusId)
            val userRatingRef = busRatingsRef.child(userId)
            
            val snapshot = userRatingRef.get().await()
            snapshot.exists()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error checking if user rated bus", e)
            false
        }
    }
    
    suspend fun getAverageBusRatingForToday(voznjaBusId: String): Float {
        val ratings = getBusRatingsForToday(voznjaBusId)
        if (ratings.isEmpty()) {
            return 0f
        }
        return ratings.map { it["rating"] as Float }.average().toFloat()
    }
    
    // Comments
    suspend fun addComment(objectId: String, objectType: ObjectType, userId: String, username: String, commentText: String): Boolean {
        return try {
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
            true
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error adding comment", e)
            false
        }
    }
    
    suspend fun getCommentsForToday(objectId: String, objectType: ObjectType): List<Map<String, Any>> {
        return try {
            val currentDate = getCurrentDateString()
            val dateCommentsRef = commentsRef.child(currentDate)
            val typeCommentsRef = dateCommentsRef.child(objectType.name)
            val objectCommentsRef = typeCommentsRef.child(objectId)
            
            suspendCoroutine { continuation ->
                objectCommentsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
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
                        continuation.resume(commentsList)
                    }
                    
                    override fun onCancelled(error: DatabaseError) {
                        continuation.resumeWithException(error.toException())
                    }
                })
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting comments", e)
            emptyList()
        }
    }
    
    suspend fun likeComment(commentId: String, objectId: String, objectType: ObjectType, userId: String): Boolean {
        return try {
            val currentDate = getCurrentDateString()
            val dateCommentsRef = commentsRef.child(currentDate)
            val typeCommentsRef = dateCommentsRef.child(objectType.name)
            val objectCommentsRef = typeCommentsRef.child(objectId)
            val commentRef = objectCommentsRef.child(commentId)
            val likedByRef = commentRef.child("likedBy").child(userId)
            
            // Check if user already liked this comment
            val snapshot = likedByRef.get().await()
            if (snapshot.exists()) {
                // User already liked, remove like
                likedByRef.removeValue().await()
                
                // Update like count
                val likesRef = commentRef.child("likes")
                val currentLikes = likesRef.get().await().getValue(Int::class.java) ?: 0
                likesRef.setValue(currentLikes - 1).await()
            } else {
                // User hasn't liked, add like
                likedByRef.setValue(true).await()
                
                // Update like count
                val likesRef = commentRef.child("likes")
                val currentLikes = likesRef.get().await().getValue(Int::class.java) ?: 0
                likesRef.setValue(currentLikes + 1).await()
            }
            
            true
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error liking comment", e)
            false
        }
    }
    
    suspend fun deleteComment(commentId: String, objectId: String, objectType: ObjectType): Boolean {
        return try {
            val currentDate = getCurrentDateString()
            val dateCommentsRef = commentsRef.child(currentDate)
            val typeCommentsRef = dateCommentsRef.child(objectType.name)
            val objectCommentsRef = typeCommentsRef.child(objectId)
            val commentRef = objectCommentsRef.child(commentId)
            
            commentRef.removeValue().await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error deleting comment", e)
            false
        }
    }
    
    // Chat Messages
    suspend fun sendChatMessage(lineNumber: String, userId: String, username: String, messageText: String): Boolean {
        return try {
            val currentDate = getCurrentDateString()
            val messageId = UUID.randomUUID().toString()
            
            val messageMap = hashMapOf(
                "id" to messageId,
                "lineNumber" to lineNumber,
                "userId" to userId,
                "username" to username,
                "message" to messageText,
                "timestamp" to Date().time,
                "date" to currentDate
            )
            
            val dateChatRef = chatMessagesRef.child(currentDate)
            val lineChatRef = dateChatRef.child(lineNumber)
            val messageRef = lineChatRef.child(messageId)
            
            messageRef.setValue(messageMap).await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error sending chat message", e)
            false
        }
    }
    
    suspend fun getChatMessagesForToday(lineNumber: String): List<Map<String, Any>> {
        return try {
            val currentDate = getCurrentDateString()
            val dateChatRef = chatMessagesRef.child(currentDate)
            val lineChatRef = dateChatRef.child(lineNumber)
            
            suspendCoroutine { continuation ->
                lineChatRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val messagesList = mutableListOf<Map<String, Any>>()
                        for (messageSnapshot in snapshot.children) {
                            val message = mutableMapOf<String, Any>()
                            message["id"] = messageSnapshot.key ?: ""
                            message["lineNumber"] = messageSnapshot.child("lineNumber").getValue(String::class.java) ?: ""
                            message["userId"] = messageSnapshot.child("userId").getValue(String::class.java) ?: ""
                            message["username"] = messageSnapshot.child("username").getValue(String::class.java) ?: ""
                            message["message"] = messageSnapshot.child("message").getValue(String::class.java) ?: ""
                            message["timestamp"] = messageSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                            message["date"] = messageSnapshot.child("date").getValue(String::class.java) ?: ""
                            
                            messagesList.add(message)
                        }
                        // Sort by timestamp ascending (oldest first)
                        messagesList.sortBy { it["timestamp"] as Long }
                        continuation.resume(messagesList)
                    }
                    
                    override fun onCancelled(error: DatabaseError) {
                        continuation.resumeWithException(error.toException())
                    }
                })
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting chat messages", e)
            emptyList()
        }
    }
    
    // Register real-time listeners
    fun listenForCommentsRealtime(objectId: String, objectType: ObjectType, callback: (List<Map<String, Any>>) -> Unit): ValueEventListener {
        val currentDate = getCurrentDateString()
        val dateCommentsRef = commentsRef.child(currentDate)
        val typeCommentsRef = dateCommentsRef.child(objectType.name)
        val objectCommentsRef = typeCommentsRef.child(objectId)
        
        val commentsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
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
                callback(commentsList)
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepository", "Error listening for comments", error.toException())
                callback(emptyList())
            }
        }
        
        objectCommentsRef.addValueEventListener(commentsListener)
        return commentsListener
    }
    
    fun listenForChatMessagesRealtime(lineNumber: String, callback: (List<Map<String, Any>>) -> Unit): ValueEventListener {
        val currentDate = getCurrentDateString()
        val dateChatRef = chatMessagesRef.child(currentDate)
        val lineChatRef = dateChatRef.child(lineNumber)
        
        val chatListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messagesList = mutableListOf<Map<String, Any>>()
                for (messageSnapshot in snapshot.children) {
                    val message = mutableMapOf<String, Any>()
                    message["id"] = messageSnapshot.key ?: ""
                    message["lineNumber"] = messageSnapshot.child("lineNumber").getValue(String::class.java) ?: ""
                    message["userId"] = messageSnapshot.child("userId").getValue(String::class.java) ?: ""
                    message["username"] = messageSnapshot.child("username").getValue(String::class.java) ?: ""
                    message["message"] = messageSnapshot.child("message").getValue(String::class.java) ?: ""
                    message["timestamp"] = messageSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                    message["date"] = messageSnapshot.child("date").getValue(String::class.java) ?: ""
                    
                    messagesList.add(message)
                }
                // Sort by timestamp ascending (oldest first)
                messagesList.sortBy { it["timestamp"] as Long }
                callback(messagesList)
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepository", "Error listening for chat messages", error.toException())
                callback(emptyList())
            }
        }
        
        lineChatRef.addValueEventListener(chatListener)
        return chatListener
    }
    
    fun listenForBusRatingsRealtime(voznjaBusId: String, callback: (List<Map<String, Any>>) -> Unit): ValueEventListener {
        val currentDate = getCurrentDateString()
        val dateRatingsRef = ratingsRef.child(currentDate)
        val busRatingsRef = dateRatingsRef.child(voznjaBusId)
        
        val ratingsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ratingsList = mutableListOf<Map<String, Any>>()
                for (ratingSnapshot in snapshot.children) {
                    val rating = mutableMapOf<String, Any>()
                    rating["id"] = ratingSnapshot.child("id").getValue(String::class.java) ?: ""
                    rating["voznjaBusId"] = ratingSnapshot.child("voznjaBusId").getValue(String::class.java) ?: ""
                    rating["userId"] = ratingSnapshot.child("userId").getValue(String::class.java) ?: ""
                    rating["username"] = ratingSnapshot.child("username").getValue(String::class.java) ?: ""
                    rating["rating"] = ratingSnapshot.child("rating").getValue(Float::class.java) ?: 0f
                    rating["timestamp"] = ratingSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                    rating["date"] = ratingSnapshot.child("date").getValue(String::class.java) ?: ""
                    
                    ratingsList.add(rating)
                }
                callback(ratingsList)
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepository", "Error listening for ratings", error.toException())
                callback(emptyList())
            }
        }
        
        busRatingsRef.addValueEventListener(ratingsListener)
        return ratingsListener
    }
    
    // Remove listeners
    fun removeListener(reference: DatabaseReference, listener: ValueEventListener) {
        reference.removeEventListener(listener)
    }
    
    // Helper method to get reference for removing listeners
    fun getBusRatingsReference(voznjaBusId: String): DatabaseReference {
        val currentDate = getCurrentDateString()
        val dateRatingsRef = ratingsRef.child(currentDate)
        return dateRatingsRef.child(voznjaBusId)
    }
    
    fun getCommentsReference(objectId: String, objectType: ObjectType): DatabaseReference {
        val currentDate = getCurrentDateString()
        val dateCommentsRef = commentsRef.child(currentDate)
        val typeCommentsRef = dateCommentsRef.child(objectType.name)
        return typeCommentsRef.child(objectId)
    }
    
    fun getChatMessagesReference(lineNumber: String): DatabaseReference {
        val currentDate = getCurrentDateString()
        val dateChatRef = chatMessagesRef.child(currentDate)
        return dateChatRef.child(lineNumber)
    }
} 