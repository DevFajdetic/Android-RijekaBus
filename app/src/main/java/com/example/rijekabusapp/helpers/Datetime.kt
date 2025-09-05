package com.example.rijekabusapp.helpers

import java.time.LocalDate
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

/**
 * Gets a formatted date string for today's date with optional offset days
 * @param offsetDays Number of days to offset from today (negative for past, positive for future)
 * @return Date string in format yyyy-MM-dd
 */
fun getCurrentDateString(offsetDays: Int = 0): String {
    val date = if (offsetDays == 0) {
        LocalDate.now()
    } else {
        LocalDate.now().plusDays(offsetDays.toLong())
    }
    return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}
