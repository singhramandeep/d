package com.example.pricetracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import com.example.pricetracker.ui.PriceTrackerRoot
import com.example.pricetracker.ui.theme.PriceTrackerTheme
import com.example.pricetracker.worker.BackgroundPollingScheduler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class MainActivity : ComponentActivity() {
    private val sharedUrlFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)
    private var initialProductId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()
        BackgroundPollingScheduler.schedule(this)
        handleIntent(intent)
        val app = application as PriceTrackerApp
        setContent {
            PriceTrackerTheme {
                PriceTrackerRoot(
                    container = app.container,
                    sharedUrls = sharedUrlFlow.asSharedFlow(),
                    initialProductId = initialProductId
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent ?: return
        extractSharedUrl(intent)?.let { sharedUrlFlow.tryEmit(it) }
        initialProductId = intent.getLongExtra("product_id", -1L).takeIf { it > 0L }
    }

    private fun extractSharedUrl(intent: Intent): String? {
        val isShareIntent = intent.action == Intent.ACTION_SEND
        val isText = intent.type?.startsWith("text/") == true
        if (!isShareIntent || !isText) return null
        return intent.getStringExtra(Intent.EXTRA_TEXT)?.trim()?.takeIf { it.startsWith("http") }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
    }
}
