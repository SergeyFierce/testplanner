package com.sergeyfierce.testplanner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sergeyfierce.testplanner.lib.notifications.NotificationScheduler
import com.sergeyfierce.testplanner.lib.storage.PlannerRepository

class PlannerViewModelFactory(
    private val repository: PlannerRepository,
    private val notifications: NotificationScheduler
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlannerViewModel::class.java)) {
            return PlannerViewModel(repository, notifications) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

