package com.example.rijekabusapp.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rijekabusapp.network.Network
import com.example.rijekabusapp.network.models.Schedule
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

class ScheduleViewModel(
    application: Application,
) : ViewModel() {
    val scheduleList = MutableLiveData<ArrayList<Schedule>>()

    fun getScheduleList() {
        viewModelScope.launch {
            val dayOfTheWeek = getDayOfWeek()
            when (dayOfTheWeek) {
                "tjedan" -> scheduleList.value = Network().getBusService().getWeekSchedule()
                "subota" -> scheduleList.value = Network().getBusService().getSaturdaySchedule()
                "nedjelja" -> scheduleList.value = Network().getBusService().getSundaySchedule()
            }
        }
    }

    fun getDayOfWeek(): String {
        val today = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(today)
        var dayOfWeek = "tjedan"

        val calendar = Calendar.getInstance()
        calendar.time = today

        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
            dayOfWeek = "nedjelja"
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
            dayOfWeek = "subota"

        // TODO Better handle this
        val praznici = arrayOf(
            "2023-04-10", "2023-05-01",
            "2023-05-30", "2023-06-08", "2023-06-22", "2023-08-05", "2023-08-15", "2023-11-01",
            "2023-11-18", "2023-12-25", "2023-12-26"
        )

        praznici.forEach { date ->
            if (date == todayStr) {
                dayOfWeek = "nedjelja"
            }
        }
        return dayOfWeek
    }
}
