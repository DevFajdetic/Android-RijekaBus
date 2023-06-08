package com.example.rijekabusapp

import java.net.URL
import org.jsoup.Jsoup

fun main() {
    val url = "https://www.autotrolej.hr/linije/"

    val html = URL(url).openStream().bufferedReader().use { it.readText() }

    val doc = Jsoup.parse(html)
    val routes = doc.select(".routes-list .one-route")

    for (route in routes) {
        val number = route.select(".route-number").text()
        val name = route.select(".route-endpoints").text()
        println("Line: $number, Name: $name")
    }
}
