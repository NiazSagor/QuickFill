package com.byteutility.dev.quickfill.ui

import android.content.Context
import android.util.Log
import android.view.autofill.AutofillManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.byteutility.dev.quickfill.ui.setup.QuickFillSetupScreen
import com.byteutility.dev.quickfill.ui.snippets.AddSnippetScreen
import com.byteutility.dev.quickfill.ui.snippets.SnippetListScreen
import com.byteutility.dev.quickfill.ui.snippets.SnippetViewModel

sealed class Screen {
    object Setup : Screen()
    object List : Screen()
    object Add : Screen()
}

@Composable
fun QuickFillApp(
    targetPackage: String?,
    viewModel: SnippetViewModel = viewModel()
) {
    val context = LocalContext.current

    var isEnabled by remember {
        mutableStateOf(isAutofillServiceEnabled(context))
    }

    var currentScreen by remember(targetPackage, isEnabled) {
        mutableStateOf(
            when {
                !isEnabled -> Screen.Setup
                targetPackage != null -> Screen.Add
                else -> Screen.List
            }
        )
    }

    LifecycleResumeEffect(Unit) {
        isEnabled = isAutofillServiceEnabled(context)
        onPauseOrDispose { }
    }
    when {
        !isEnabled -> {
            QuickFillSetupScreen()
        }

        else -> {
            when (currentScreen) {
                Screen.List -> SnippetListScreen(
                    viewModel = viewModel,
                    onAddClick = { currentScreen = Screen.Add }
                )

                Screen.Add -> AddSnippetScreen(
                    viewModel = viewModel,
                    targetPackage = targetPackage,
                    onBack = { currentScreen = Screen.List }
                )

                Screen.Setup -> QuickFillSetupScreen()
            }
        }
    }
}

fun isAutofillServiceEnabled(context: Context): Boolean {
    val autofillManager = context.getSystemService(AutofillManager::class.java)
    return autofillManager != null && autofillManager.hasEnabledAutofillServices()
}
