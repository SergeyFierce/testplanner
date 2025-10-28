package com.sergeyfierce.testplanner.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sergeyfierce.testplanner.data.TaskRepository
import com.sergeyfierce.testplanner.data.preferences.CalendarPreferencesRepository

class CalendarViewModelFactory(
    private val repository: TaskRepository,
    private val preferencesRepository: CalendarPreferencesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(CalendarViewModel::class.java))
        return CalendarViewModel(repository, preferencesRepository) as T
    }
}
