package com.byteutility.dev.quickfill

import android.content.Context
import android.view.autofill.AutofillManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun QuickFillApp(viewModel: SnippetViewModel = viewModel()) {
    val context = LocalContext.current

    // Track the enabled state
    var isEnabled by remember { mutableStateOf(isAutofillServiceEnabled(context)) }
    var currentScreen by remember { mutableStateOf("LIST") }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isEnabled = isAutofillServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (!isEnabled) {
        QuickFillSetupScreen()
    } else {
        when (currentScreen) {
            "LIST" -> SnippetListScreen(
                viewModel = viewModel,
                onAddClick = { currentScreen = "ADD" }
            )
            "ADD" -> AddSnippetScreen(
                viewModel = viewModel,
                onBack = { currentScreen = "LIST" }
            )
        }
    }
}

fun isAutofillServiceEnabled(context: Context): Boolean {
    val autofillManager = context.getSystemService(AutofillManager::class.java)
    return autofillManager != null && autofillManager.hasEnabledAutofillServices()
}
