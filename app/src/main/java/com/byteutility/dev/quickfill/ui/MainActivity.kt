package com.byteutility.dev.quickfill.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val ACTION_ADD_SPECIFIC_SNIPPET = "ACTION_ADD_SPECIFIC_SNIPPET"
        private const val EXTRA_TARGET_PACKAGE = "TARGET_PACKAGE"
    }

    private val targetPackage = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleAutofillIntent(intent)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    QuickFillApp(targetPackage = targetPackage.value)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAutofillIntent(intent)
    }

    private fun handleAutofillIntent(intent: Intent?) {
        if (intent?.action == ACTION_ADD_SPECIFIC_SNIPPET) {
            targetPackage.value = intent.getStringExtra(EXTRA_TARGET_PACKAGE)
        }
    }
}
