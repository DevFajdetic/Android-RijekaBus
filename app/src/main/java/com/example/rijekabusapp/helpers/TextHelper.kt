package com.example.rijekabusapp.helpers

fun titleCase(text: String): String {
    return text.split(" ")
        .joinToString(" ") { it.toLowerCase().capitalize() }
}
