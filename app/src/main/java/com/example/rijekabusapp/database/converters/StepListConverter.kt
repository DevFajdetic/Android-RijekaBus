package com.example.rijekabusapp.database.converters

import androidx.room.TypeConverter
import com.example.rijekabusapp.network.models.Step
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StepListConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromStepList(steps: List<Step>): String {
        return gson.toJson(steps)
    }

    @TypeConverter
    fun toStepList(stepsString: String): List<Step> {
        if (stepsString.isBlank()) return emptyList()
        val type = object : TypeToken<List<Step>>() {}.type
        return gson.fromJson(stepsString, type)
    }
}