package com.example.rijekabusapp.helpers

import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

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

fun getCurrentDateTime(): String {
    val croatianLocale = Locale.ENGLISH
    val croatianZoneId = ZoneId.of("Europe/Zagreb")

    val currentDateTime = ZonedDateTime.now(croatianZoneId)
    val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", croatianLocale)
    return currentDateTime.format(formatter).toUpperCase(croatianLocale)
}
