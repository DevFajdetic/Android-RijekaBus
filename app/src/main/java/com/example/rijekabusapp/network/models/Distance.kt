package com.example.rijekabusapp.network.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Distance(
    @SerializedName("text")
    val text: String,
    @SerializedName("value")
    val value: Int,
) : Serializable
