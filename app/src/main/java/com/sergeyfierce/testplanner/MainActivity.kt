package com.sergeyfierce.testplanner

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import com.sergeyfierce.testplanner.lib.notifications.NotificationScheduler
import com.sergeyfierce.testplanner.lib.storage.PlannerRepository
import com.sergeyfierce.testplanner.lib.storage.PlannerStorage
import com.sergeyfierce.testplanner.ui.PlannerMainScreen
import com.sergeyfierce.testplanner.ui.PlannerViewModel
import com.sergeyfierce.testplanner.ui.PlannerViewModelFactory
import com.sergeyfierce.testplanner.ui.ToastType
import com.sergeyfierce.testplanner.ui.theme.TestplannerTheme

class MainActivity : ComponentActivity() {

    private val repository by lazy { PlannerRepository(PlannerStorage(applicationContext)) }
    private val notificationScheduler by lazy { NotificationScheduler(applicationContext) }
    private val viewModel: PlannerViewModel by viewModels {
        PlannerViewModelFactory(repository, notificationScheduler)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        notificationScheduler.ensureChannel()
        setContent {
            TestplannerTheme {
                val launcher = rememberNotificationPermissionLauncher()
                PlannerMainScreen(
                    viewModel = viewModel,
                    onRequestNotifications = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )
            }
        }
    }

    @Composable
    private fun rememberNotificationPermissionLauncher() =
        androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (!granted) {
                viewModel.notify("Уведомления отключены", ToastType.INFO)
            } else {
                notificationScheduler.ensureChannel()
            }
        }
}