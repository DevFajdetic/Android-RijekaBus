package com.example.rijekabusapp.network.response

data class OSRMDirectionsResponse(
    val routes: List<Route>,
)

data class Route(
    val geometry: String,
    val legs: List<Leg>,
)

data class Leg(
    val steps: List<Step>,
    val distance: Double,
    val duration: Double,
)

data class Step(
    val maneuver: Maneuver,
    val distance: Double,
    val duration: Double,
)

data class Maneuver(
    val location: List<Double>,
    val instruction: String,
)
