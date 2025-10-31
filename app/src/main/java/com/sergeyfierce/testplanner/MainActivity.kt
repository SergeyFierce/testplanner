package com.sergeyfierce.testplanner

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.sergeyfierce.testplanner.data.TaskRepository
import com.sergeyfierce.testplanner.data.local.TaskDatabase
import com.sergeyfierce.testplanner.data.preferences.CalendarPreferencesRepository
import com.sergeyfierce.testplanner.ui.theme.TestplannerTheme

class MainActivity : ComponentActivity() {
    private val database by lazy { TaskDatabase.build(applicationContext) }
    private val taskRepository by lazy { TaskRepository(database.taskDao()) }
    private val preferencesRepository by lazy { CalendarPreferencesRepository(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ВКЛЮЧАЕМ edge-to-edge и задаём прозрачные бары
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            )
        )

        // Отключаем контрастную заливку навбара (Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        // Чтобы иконки навигации не были светлыми на тёмном фоне
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightNavigationBars = false

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
