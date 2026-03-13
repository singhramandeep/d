package com.example.pricetracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pricetracker.notifications.NotificationHelper
import com.example.pricetracker.ui.MainScreen
import com.example.pricetracker.ui.MainViewModel
import com.example.pricetracker.ui.MainViewModelFactory
import com.example.pricetracker.worker.PriceCheckWorker

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(AppContainer.repository(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.ensureChannel(this)
        PriceCheckWorker.schedule(this)
        maybeRequestNotificationPermission()
        consumeIntent(intent)

        setContent {
            val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
            MainScreen(
                state = uiState,
                onRefresh = viewModel::refreshNow,
                onTrackPending = viewModel::trackPendingUrl,
                onDismissPending = viewModel::clearPendingUrl,
                onUpdateTarget = viewModel::updateTargetPrice,
                onRemoveProduct = viewModel::removeProduct,
                onClearMessage = viewModel::clearMessage
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        consumeIntent(intent)
    }

    private fun consumeIntent(intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            Intent.ACTION_SEND -> {
                val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                viewModel.onSharedText(sharedText)
            }

            Intent.ACTION_VIEW -> {
                viewModel.onSharedText(intent.dataString)
            }
        }
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) return
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            1001
        )
    }
}
