package com.example.rijekabusapp.helpers

import android.app.*
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.rijekabusapp.LineActivity
import com.example.rijekabusapp.R
import com.example.rijekabusapp.adapters.EXTRA_LINE
import com.example.rijekabusapp.network.models.Schedule
import java.text.SimpleDateFormat
import java.util.*

const val NOTIFICATION_CHANNEL_ID = "my_notification_channel"

// Method for setting the alarm
private fun setAlarm(context: Context, calendar: Calendar) {
    val selectedTimeInMillis = calendar.timeInMillis

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val alarmIntent = Intent(context, MyAlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        alarmIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.set(AlarmManager.RTC_WAKEUP, selectedTimeInMillis, pendingIntent)
}

// Method for creating the notification
private fun createNotification(
    context: Context,
    selectedTime: String,
    item: Schedule
): Notification {
    val notificationIntent = Intent(context, LineActivity::class.java).apply {
        putExtra(EXTRA_LINE, item.asLine())
    }
    val pendingNotificationIntent = PendingIntent.getActivity(
        context,
        0,
        notificationIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val dismissIntent = Intent(context, MyNotificationDismissReceiver::class.java)
    val pendingDismissIntent = PendingIntent.getBroadcast(
        context,
        0,
        dismissIntent,
        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setContentTitle(context.getString(R.string.alaram_set))
        .setContentText(context.getString(R.string.alaram_at) + selectedTime)
        .setSmallIcon(R.drawable.ic_bell)
        .setContentIntent(pendingNotificationIntent)
        .setDeleteIntent(pendingDismissIntent)
        .addAction(R.drawable.ic_delete, "Dismiss", pendingDismissIntent)
        .setAutoCancel(true)
        .build()
}

// Method for handling the time picker dialog
fun showTimePickerDialog(context: Context, item: Schedule) {
    val calendar = Calendar.getInstance()

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)

            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val selectedTime = dateFormat.format(calendar.time)

            setAlarm(context, calendar)

            val notification = createNotification(context, selectedTime, item)

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(1, notification)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        getBoolFromPreferences(PREF_HOUR, true, context)
    )

    timePickerDialog.show()
}
