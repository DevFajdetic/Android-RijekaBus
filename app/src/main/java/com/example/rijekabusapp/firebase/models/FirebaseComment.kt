package com.example.rijekabusapp.firebase.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class FirebaseComment(
    val id: String? = null,
    val objectId: String? = null,
    val objectType: String? = null,
    val userId: String? = null,
    val username: String? = null,
    val comment: String? = null,
    val timestamp: Long = 0,
    val date: String? = null,
    val likes: Int = 0,
    val likedBy: Map<String, Boolean> = mapOf()
) 