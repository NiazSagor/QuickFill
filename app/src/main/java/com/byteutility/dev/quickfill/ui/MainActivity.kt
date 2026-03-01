package com.byteutility.dev.quickfill.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleAutofillIntent(intent)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.Companion.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    QuickFillApp()
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
        if (intent?.action == "ACTION_ADD_SPECIFIC_SNIPPET") {
            val targetPackage = intent.getStringExtra("TARGET_PACKAGE")
            // navigate to your add snippet screen
        }
    }
}
