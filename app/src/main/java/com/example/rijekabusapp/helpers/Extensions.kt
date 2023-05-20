package com.example.rijekabusapp.helpers

import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.core.content.getSystemService
import androidx.preference.PreferenceManager
import com.example.rijekabusapp.R
import com.example.rijekabusapp.network.models.Station
import com.google.android.material.textfield.TextInputEditText

fun TextInputEditText.customValidate(context: Context): Boolean {
    val valid = true

    if (this.text!!.isBlank()) {
        this.error = context.getString(R.string.please_insert_value)
        this.requestFocus()
        return false
    }

    when (this.tag) {
        "URL" -> {
            if (!this.text!!
                .contains
                (
                        "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"
                            .toRegex()
                    )
            ) {
                this.error = context.getString(R.string.enter_valid_url)
                return false
            }
        }
        "YTURL" -> {
            if (!this.text!!
                .contains(
                        "^(https?://)?(www\\.youtube\\.com|youtu\\.be)/.+\$"
                            .toRegex()
                    )
            ) {
                this.error = context.getString(R.string.youtube_url)
                return false
            }
        }
    }
    return valid
}

fun showCustomDialog(title: String, context: Context) {
    val dialog = Dialog(context).apply {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.setCancelable(true)
        this.setContentView(R.layout.custom_dialog)
        this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    val tvTitle = dialog.findViewById<TextView>(R.id.tvDialogTitle)
    val btnOk = dialog.findViewById<Button>(R.id.btnDialogOk)
    val btnCancel = dialog.findViewById<Button>(R.id.btnDialogCancel)

    tvTitle.text = title
    btnOk.text = context.getText(R.string.ok)
    btnCancel.visibility = View.GONE

    btnOk.setOnClickListener {
        dialog.dismiss()
    }

    dialog.show()
}

fun Context.isOnline(): Boolean {
    val connectivityManager = getSystemService<ConnectivityManager>()
    connectivityManager?.activeNetwork?.let { network ->
        connectivityManager.getNetworkCapabilities(network)?.let { networkCapabilities ->
            return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                networkCapabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                )
        }
    }
    return false
}

inline fun <reified T : Activity> Context.startActivity() =
    startActivity(
        Intent(this, T::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    )

inline fun <reified T : Activity> Context.startActivity(key: String, value: ArrayList<Station>) =
    startActivity(
        Intent(this, T::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(key, value)
        }
    )

inline fun <reified T : BroadcastReceiver> Context.sendBroadcast() =
    sendBroadcast(Intent(this, T::class.java))

fun Context.setBooleanPreference(key: String, value: Boolean) =
    PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(key, value).apply()

fun Context.getBooleanPreference(key: String) =
    PreferenceManager.getDefaultSharedPreferences(this).getBoolean(key, false)

fun View.startAnimation(animationId: Int) =
    startAnimation(AnimationUtils.loadAnimation(context, animationId))

fun callDelayed(delay: Long, function: Runnable) {
    Handler(Looper.getMainLooper()).postDelayed(
        function, delay
    )
}
