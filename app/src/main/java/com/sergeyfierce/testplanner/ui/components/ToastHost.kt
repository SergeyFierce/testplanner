package com.sergeyfierce.testplanner.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sergeyfierce.testplanner.ui.PlannerToast
import com.sergeyfierce.testplanner.ui.ToastType
import kotlinx.coroutines.delay

@Composable
fun PlannerToastHost(toast: PlannerToast?, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    var visible by remember(toast) { mutableStateOf(toast != null) }

    LaunchedEffect(toast) {
        if (toast != null) {
            visible = true
            delay(3000)
            visible = false
            onDismiss()
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
            val containerColor = when (toast?.type) {
                ToastType.SUCCESS -> MaterialTheme.colorScheme.primary
                ToastType.ERROR -> MaterialTheme.colorScheme.error
                ToastType.INFO, null -> MaterialTheme.colorScheme.secondary
            }
            Surface(
                color = containerColor,
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = toast?.message ?: "",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

