package com.example.rijekabusapp.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.content.ContextCompat
import com.example.rijekabusapp.R

const val USER_LOCATION_MARKER_TITLE = "My Location"

// Create a circular marker with the line number inside
fun createLineMarkerIcon(
    context: Context,
    lineNumber: String,
    lineDirection: String,
): Bitmap {
    val markerSize = context.resources.getDimensionPixelSize(R.dimen.marker_size)
    val bitmap = Bitmap.createBitmap(markerSize, markerSize, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Draw circle background
    val paint = Paint()
    if (lineDirection == "A") {
        paint.color = ContextCompat.getColor(context, R.color.wave)
    } else {
        paint.color = ContextCompat.getColor(context, R.color.some)
    }

    canvas.drawCircle(markerSize / 2f, markerSize / 2f, markerSize / 2f, paint)

    // Draw line number
    paint.color = ContextCompat.getColor(context, R.color.black)
    paint.textSize = context.resources.getDimensionPixelSize(R.dimen.marker_text_size).toFloat()
    paint.textAlign = Paint.Align.CENTER
    val x = markerSize / 2f
    val y = markerSize / 2f - (paint.descent() + paint.ascent()) / 2f
    canvas.drawText(lineNumber, x, y, paint)

    return bitmap
}

// Create a circular marker for station
fun createStationMarkerIcon(
    context: Context,
    stationDirection: String,
): Bitmap {
    val markerSize = context.resources.getDimensionPixelSize(R.dimen.marker_station_size)
    val bitmap = Bitmap.createBitmap(markerSize, markerSize, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Draw circle background
    val paint = Paint()
    if (stationDirection == "A") {
        paint.color = ContextCompat.getColor(context, R.color.greenish)
    } else {
        paint.color = ContextCompat.getColor(context, R.color.fade_blue)
    }

    canvas.drawCircle(markerSize / 2f, markerSize / 2f, markerSize / 2f, paint)

    // Draw line number
    paint.color = ContextCompat.getColor(context, R.color.black)
    paint.textSize =
        context.resources
            .getDimensionPixelSize(R.dimen.marker_station_text_size).toFloat()
    paint.textAlign = Paint.Align.CENTER
    val x = markerSize / 2f
    val y = markerSize / 2f - (paint.descent() + paint.ascent()) / 2f
    canvas.drawText(stationDirection, x, y, paint)

    return bitmap
}
