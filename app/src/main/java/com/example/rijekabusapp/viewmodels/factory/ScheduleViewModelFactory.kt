package com.example.rijekabusapp.viewmodels.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rijekabusapp.viewmodels.ScheduleViewModel

class ScheduleViewModelFactory(
    private val ctx: Application,
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ScheduleViewModel(ctx) as T
    }
}
