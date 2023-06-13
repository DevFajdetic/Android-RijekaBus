package com.example.rijekabusapp.helpers

import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun stringToTime(timeString: String): LocalTime {
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSSS")
    return LocalTime.parse(timeString, formatter)
}

fun compareWithCurrentTime(time: LocalTime): Int {
    val currentTime = LocalTime.now()
    println(currentTime)
    return when {
        time.isBefore(currentTime) -> -1
        time.isAfter(currentTime) -> 1
        else -> 0
    }
}

fun main() {
    val str = "12:57:13.0000000"
    println(compareWithCurrentTime(stringToTime(str)))
}
