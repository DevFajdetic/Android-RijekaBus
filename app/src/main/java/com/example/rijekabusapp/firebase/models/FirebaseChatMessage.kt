package com.example.rijekabusapp.firebase.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class FirebaseChatMessage(
    val id: String? = null,
    val lineNumber: String? = null,
    val userId: String? = null,
    val username: String? = null,
    val message: String? = null,
    val timestamp: Long = 0,
    val date: String? = null
) 