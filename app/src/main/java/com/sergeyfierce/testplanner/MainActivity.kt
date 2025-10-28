package com.sergeyfierce.testplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sergeyfierce.testplanner.ui.theme.TestplannerTheme
import com.sergeyfierce.testplanner.data.local.TaskDatabase
import com.sergeyfierce.testplanner.data.TaskRepository
import com.sergeyfierce.testplanner.data.preferences.CalendarPreferencesRepository

class MainActivity : ComponentActivity() {
    private val database by lazy { TaskDatabase.build(applicationContext) }
    private val taskRepository by lazy { TaskRepository(database.taskDao()) }
    private val preferencesRepository by lazy { CalendarPreferencesRepository(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestplannerTheme {
                PlannerApp(
                    repository = taskRepository,
                    preferencesRepository = preferencesRepository
                )
            }
        }
    }
}