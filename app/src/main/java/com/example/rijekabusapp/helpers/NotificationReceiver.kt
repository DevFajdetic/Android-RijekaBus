package com.example.rijekabusapp.helpers

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.rijekabusapp.R

class MyAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val dismissIntent = Intent(context, MyNotificationDismissReceiver::class.java)
        val pendingDismissIntent =
            PendingIntent.getBroadcast(
                context,
                0,
                dismissIntent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val notification =
            NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(context.getString(R.string.alaram))
                .setContentText(context.getString(R.string.alaram_ringing))
                .setSmallIcon(R.drawable.ic_bell)
                .setDeleteIntent(pendingDismissIntent)
                .addAction(
                    R.drawable.ic_delete,
                    context.getString(R.string.dismiss),
                    pendingDismissIntent,
                )
                .setAutoCancel(true)
                .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }
}

class MyNotificationDismissReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, MyAlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                0,
                alarmIntent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        alarmManager.cancel(pendingIntent)
    }
}
