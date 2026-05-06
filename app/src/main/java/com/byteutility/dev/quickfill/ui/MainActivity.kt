package com.byteutility.dev.quickfill.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteutility.dev.quickfill.ui.snippets.SnippetViewModel
import com.byteutility.dev.quickfill.ui.theme.QuickFillTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: SnippetViewModel by viewModels()

    companion object {
        private const val ACTION_ADD_SPECIFIC_SNIPPET = "ACTION_ADD_SPECIFIC_SNIPPET"
        private const val EXTRA_TARGET_PACKAGE = "TARGET_PACKAGE"
    }

    private val targetPackage = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleAutofillIntent(intent)

        setContent {
            val isDarkModeOverride by viewModel.isDarkMode.collectAsStateWithLifecycle()
            val darkTheme = isDarkModeOverride ?: isSystemInDarkTheme()

            QuickFillTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    QuickFillApp(
                        targetPackage = targetPackage.value,
                        viewModel = viewModel
                    )
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
