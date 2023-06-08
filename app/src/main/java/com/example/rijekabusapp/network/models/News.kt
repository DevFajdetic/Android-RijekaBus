package com.example.rijekabusapp.network.models

import java.io.Serializable

data class News(
    val title: String?,
    val body: String?,
    val category: String?,
    val date: String?
) : Serializable
