package com.byteutility.dev.quickfill.ui

import android.content.Context
import android.view.autofill.AutofillManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.byteutility.dev.quickfill.ui.setup.QuickFillSetupScreen
import com.byteutility.dev.quickfill.ui.snippets.AddSnippetScreen
import com.byteutility.dev.quickfill.ui.snippets.SnippetListScreen
import com.byteutility.dev.quickfill.ui.snippets.SnippetViewModel

object Dest {
    const val SETUP = "setup"
    const val SNIPPET_LIST = "snippet_list"
    const val ADD_SNIPPET = "add_snippet?targetPackage={targetPackage}"
}

@Composable
fun QuickFillApp(
    targetPackage: String?,
    viewModel: SnippetViewModel = viewModel()
) {
    val context = LocalContext.current
    val navController = rememberNavController()

    var isEnabled by remember {
        mutableStateOf(isAutofillServiceEnabled(context))
    }

    LifecycleResumeEffect(Unit) {
        isEnabled = isAutofillServiceEnabled(context)
        onPauseOrDispose { }
    }

    LaunchedEffect(isEnabled, targetPackage) {
        if (!isEnabled) {
            navController.navigate(Dest.SETUP) {
                popUpTo(0)
            }
        } else if (targetPackage != null) {
            navController.navigate("add_snippet?targetPackage=$targetPackage")
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (isEnabled) Dest.SNIPPET_LIST else Dest.SETUP
    ) {
        composable(Dest.SETUP) {
            QuickFillSetupScreen()
        }

        composable(Dest.SNIPPET_LIST) {
            SnippetListScreen(
                viewModel = viewModel,
                onAddClick = { navController.navigate(Dest.ADD_SNIPPET) }
            )
        }

        composable(
            route = Dest.ADD_SNIPPET,
            arguments = listOf(
                navArgument("targetPackage") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val pkg = backStackEntry.arguments?.getString("targetPackage")
            AddSnippetScreen(
                viewModel = viewModel,
                targetPackage = pkg,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

fun isAutofillServiceEnabled(context: Context): Boolean {
    val autofillManager = context.getSystemService(AutofillManager::class.java)
    return autofillManager != null && autofillManager.hasEnabledAutofillServices()
}
