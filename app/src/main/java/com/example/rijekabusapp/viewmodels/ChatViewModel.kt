package com.example.rijekabusapp.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.rijekabusapp.helpers.getCurrentDateString
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val database = FirebaseDatabase.getInstance("https://rijekabusapp-default-rtdb.europe-west1.firebasedatabase.app")
    private val chatMessagesRef = database.getReference("chatMessages")
    private val directMessagesRef = database.getReference("directMessages")
    private val usersRef = database.getReference("users")
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _messages = MutableLiveData<List<Map<String, Any>>>(emptyList())
    val messages: LiveData<List<Map<String, Any>>> = _messages

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val _chatUsers = MutableLiveData<List<Map<String, Any>>>(emptyList())
    val chatUsers: LiveData<List<Map<String, Any>>> = _chatUsers

    // Keep track of chat listener to remove when no longer needed
    private var chatListener: ValueEventListener? = null
    private var directChatListener: ValueEventListener? = null

    init {
        // Update user's last seen when ViewModel is created
        updateUserLastSeen()
    }

    fun loadChatMessages(lineNumber: String) {
        _isLoading.value = true

        // Remove previous listener if exists
        chatListener?.let {
            val currentDate = getCurrentDateString()
            val dateChatRef = chatMessagesRef.child(currentDate)
            val lineChatRef = dateChatRef.child(lineNumber)
            lineChatRef.removeEventListener(it)
        }

        val messagesList = mutableListOf<Map<String, Any>>()
        var completedQueries = 0
        val totalQueries = 7 // Load messages from the last 7 days

        // Get date format for querying past days
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        for (i in 0 until totalQueries) {
            val dateString = dateFormat.format(calendar.time)
            val dateChatRef = chatMessagesRef.child(dateString)
            val lineChatRef = dateChatRef.child(lineNumber)

            lineChatRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
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

                        completedQueries++

                        if (completedQueries == totalQueries) {
                            // Sort by timestamp ascending (oldest first)
                            messagesList.sortBy { it["timestamp"] as Long }
                            _messages.postValue(messagesList)
                            _isLoading.postValue(false)

                            // Set up real-time listener for current date
                            setupRealtimeListener(lineNumber)
                        }
                    } catch (e: Exception) {
                        Log.e("ChatViewModel", "Error processing chat messages", e)
                        _errorMessage.postValue("Error loading chat: ${e.message}")
                        _isLoading.postValue(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatViewModel", "Error loading chat messages", error.toException())
                    completedQueries++

                    if (completedQueries == totalQueries) {
                        // Sort by timestamp ascending (oldest first)
                        messagesList.sortBy { it["timestamp"] as Long }
                        _messages.postValue(messagesList)
                        _isLoading.postValue(false)

                        // Set up real-time listener for current date
                        setupRealtimeListener(lineNumber)
                    }
                }
            })

            // Move to previous day
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
    }

    private fun setupRealtimeListener(lineNumber: String) {
        val currentDate = getCurrentDateString()
        val dateChatRef = chatMessagesRef.child(currentDate)
        val lineChatRef = dateChatRef.child(lineNumber)

        chatListener = lineChatRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()
                    val newMessages = mutableListOf<Map<String, Any>>()

                    for (messageSnapshot in snapshot.children) {
                        val messageId = messageSnapshot.key ?: ""

                        // Check if message already exists in our list
                        if (currentMessages.none { it["id"] == messageId }) {
                            val message = mutableMapOf<String, Any>()
                            message["id"] = messageId
                            message["lineNumber"] = messageSnapshot.child("lineNumber").getValue(String::class.java) ?: ""
                            message["userId"] = messageSnapshot.child("userId").getValue(String::class.java) ?: ""
                            message["username"] = messageSnapshot.child("username").getValue(String::class.java) ?: ""
                            message["message"] = messageSnapshot.child("message").getValue(String::class.java) ?: ""
                            message["timestamp"] = messageSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                            message["date"] = messageSnapshot.child("date").getValue(String::class.java) ?: ""

                            newMessages.add(message)
                        }
                    }

                    if (newMessages.isNotEmpty()) {
                        currentMessages.addAll(newMessages)
                        currentMessages.sortBy { it["timestamp"] as Long }
                        _messages.postValue(currentMessages)
                    }
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Error processing real-time chat messages", e)
                    _errorMessage.postValue("Error updating chat: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatViewModel", "Real-time chat updates cancelled", error.toException())
            }
        })
    }

    fun loadDirectMessages(userId: String, otherUserId: String) {
        _isLoading.value = true

        // Create a unique conversation ID by sorting the two user IDs and concatenating them
        val userIds = listOf(userId, otherUserId).sorted()
        val conversationId = "${userIds[0]}_${userIds[1]}"

        // Remove previous listener if exists
        directChatListener?.let {
            val conversationRef = directMessagesRef.child(conversationId)
            conversationRef.removeEventListener(it)
        }

        val conversationRef = directMessagesRef.child(conversationId)

        directChatListener = conversationRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val messagesList = mutableListOf<Map<String, Any>>()

                    for (messageSnapshot in snapshot.children) {
                        val message = mutableMapOf<String, Any>()
                        message["id"] = messageSnapshot.key ?: ""
                        message["senderId"] = messageSnapshot.child("senderId").getValue(String::class.java) ?: ""
                        message["receiverId"] = messageSnapshot.child("receiverId").getValue(String::class.java) ?: ""
                        message["username"] = messageSnapshot.child("username").getValue(String::class.java) ?: ""
                        message["message"] = messageSnapshot.child("message").getValue(String::class.java) ?: ""
                        message["timestamp"] = messageSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                        message["date"] = messageSnapshot.child("date").getValue(String::class.java) ?: ""

                        messagesList.add(message)
                    }

                    // Sort by timestamp ascending (oldest first)
                    messagesList.sortBy { it["timestamp"] as Long }

                    _messages.postValue(messagesList)
                    _isLoading.postValue(false)
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Error processing direct messages", e)
                    _errorMessage.postValue("Error loading direct messages: ${e.message}")
                    _isLoading.postValue(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatViewModel", "Error loading direct messages", error.toException())
                _errorMessage.postValue("Error loading direct messages: ${error.message}")
                _isLoading.postValue(false)
            }
        })
    }

    fun sendMessage(lineNumber: String, userId: String, username: String, messageText: String) {
        if (messageText.isBlank()) {
            _errorMessage.value = "Message cannot be empty"
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
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

                // Update user's last seen after sending message
                updateUserLastSeen()

                _isLoading.postValue(false)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message", e)
                _errorMessage.postValue("Error sending message: ${e.message}")
                _isLoading.postValue(false)
            }
        }
    }

    fun sendDirectMessage(senderId: String, senderUsername: String, receiverId: String, messageText: String) {
        if (messageText.isBlank()) {
            _errorMessage.value = "Message cannot be empty"
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Create a unique conversation ID by sorting the two user IDs and concatenating them
                val userIds = listOf(senderId, receiverId).sorted()
                val conversationId = "${userIds[0]}_${userIds[1]}"

                val messageId = UUID.randomUUID().toString()
                val currentDate = getCurrentDateString()

                val messageMap = hashMapOf(
                    "id" to messageId,
                    "senderId" to senderId,
                    "receiverId" to receiverId,
                    "username" to senderUsername,
                    "message" to messageText,
                    "timestamp" to Date().time,
                    "date" to currentDate
                )

                val conversationRef = directMessagesRef.child(conversationId)
                val messageRef = conversationRef.child(messageId)

                messageRef.setValue(messageMap).await()

                // Update user's last seen after sending message
                updateUserLastSeen()

                _isLoading.postValue(false)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending direct message", e)
                _errorMessage.postValue("Error sending message: ${e.message}")
                _isLoading.postValue(false)
            }
        }
    }

    fun loadChatUsers() {
        _isLoading.value = true

        Log.d("ChatViewModel", "Loading chat users...")

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    Log.d("ChatViewModel", "Users snapshot received, children count: ${snapshot.childrenCount}")
                    val usersList = mutableListOf<Map<String, Any>>()

                    for (userSnapshot in snapshot.children) {
                        val user = mutableMapOf<String, Any>()
                        user["id"] = userSnapshot.key ?: ""
                        user["username"] = userSnapshot.child("username").getValue(String::class.java) ?: "Unknown User"
                        user["email"] = userSnapshot.child("email").getValue(String::class.java) ?: ""
                        user["photoUrl"] = userSnapshot.child("photoUrl").getValue(String::class.java) ?: ""
                        user["lastSeen"] = userSnapshot.child("lastSeen").getValue(Long::class.java) ?: 0L
                        user["isAnonymous"] = userSnapshot.child("isAnonymous").getValue(Boolean::class.java) ?: false

                        usersList.add(user)
                        Log.d("ChatViewModel", "Added user: ${user["username"]} (${user["id"]})")
                    }

                    // Sort by last seen (most recent first)
                    usersList.sortByDescending { it["lastSeen"] as Long }
                    Log.d("ChatViewModel", "Total users loaded: ${usersList.size}")

                    _chatUsers.postValue(usersList)
                    _isLoading.postValue(false)
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Error loading chat users", e)
                    _errorMessage.postValue("Error loading users: ${e.message}")
                    _isLoading.postValue(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatViewModel", "Error loading chat users", error.toException())
                _errorMessage.postValue("Error loading users: ${error.message}")
                _isLoading.postValue(false)
            }
        })
    }

    private fun updateUserLastSeen() {
        viewModelScope.launch {
            try {
                val currentUser = firebaseAuth.currentUser
                if (currentUser != null) {
                    val updates = hashMapOf<String, Any>(
                        "lastSeen" to Date().time
                    )

                    usersRef.child(currentUser.uid).updateChildren(updates).await()
                    Log.d("ChatViewModel", "User last seen updated successfully")
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error updating user last seen", e)
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()

        // Remove chat listener when ViewModel is cleared
        chatListener?.let {
            val currentDate = getCurrentDateString()
            val dateChatRef = chatMessagesRef.child(currentDate)
            dateChatRef.removeEventListener(it)
        }

        // Remove direct chat listener when ViewModel is cleared
        directChatListener?.let {
            directMessagesRef.removeEventListener(it)
        }
    }
}