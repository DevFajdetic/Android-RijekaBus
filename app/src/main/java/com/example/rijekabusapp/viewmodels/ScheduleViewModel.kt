package com.example.rijekabusapp.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rijekabusapp.network.Network
import com.example.rijekabusapp.network.models.Schedule
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ScheduleViewModel(
    application: Application,
) : ViewModel() {
    val scheduleList = MutableLiveData<ArrayList<Schedule>>()
    private val tag = "ScheduleViewModel"

    fun getScheduleList(s: String?) {
        viewModelScope.launch {
            try {
                val dayOfTheWeek = getDayOfWeek()
                if (s == null) {
                    when (dayOfTheWeek) {
                        "tjedan" -> scheduleList.value = Network().getBusService().getWeekSchedule()
                        "subota" -> scheduleList.value = Network().getBusService().getSaturdaySchedule()
                        "nedjelja" -> scheduleList.value = Network().getBusService().getSundaySchedule()
                    }
                } else {
                    when (s) {
                        "tjedan" -> scheduleList.value = Network().getBusService().getWeekSchedule()
                        "subota" -> scheduleList.value = Network().getBusService().getSaturdaySchedule()
                        "nedjelja" -> scheduleList.value = Network().getBusService().getSundaySchedule()
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Error getting schedule list: ${e.message}", e)
                // Return empty list instead of crashing
                scheduleList.value = ArrayList()
            }
        }
    }

    fun getDayOfWeek(): String {
        try {
            val today = Date()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = dateFormat.format(today)
            var dayOfWeek = "tjedan"

            val calendar = Calendar.getInstance()
            calendar.time = today

            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                dayOfWeek = "nedjelja"
            }
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                dayOfWeek = "subota"
            }

            // TODO Better handle this
            val praznici =
                arrayOf(
                    "2023-04-10", "2023-05-01",
                    "2023-05-30", "2023-06-08", "2023-06-22", "2023-08-05", "2023-08-15", "2023-11-01",
                    "2023-11-18", "2023-12-25", "2023-12-26",
                )

            praznici.forEach { date ->
                if (date == todayStr) {
                    dayOfWeek = "nedjelja"
                }
            }
            return dayOfWeek
        } catch (e: Exception) {
            Log.e(tag, "Error determining day of week: ${e.message}", e)
            return "tjedan" // Default to weekday schedule if there's an error
        }
    }
}
