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
import com.byteutility.dev.quickfill.ui.setup.OnboardingScreen
import com.byteutility.dev.quickfill.ui.setup.QuickFillSetupScreen
import com.byteutility.dev.quickfill.ui.snippets.AddSnippetScreen
import com.byteutility.dev.quickfill.ui.snippets.SnippetListScreen
import com.byteutility.dev.quickfill.ui.snippets.SnippetViewModel

object Dest {
    const val ONBOARDING = "onboarding"
    const val SETUP = "setup"
    const val SNIPPET_LIST = "snippet_list"

    private const val ADD_SNIPPET_BASE = "add_snippet"
    const val ADD_SNIPPET = "$ADD_SNIPPET_BASE?targetPackage={targetPackage}"

    private const val EDIT_SNIPPET_BASE = "edit_snippet"
    const val EDIT_SNIPPET = "$EDIT_SNIPPET_BASE/{snippetId}"

    // Use this function to navigate instead of the constant
    fun passTargetPackage(pkg: String? = null): String {
        return if (pkg != null) {
            "$ADD_SNIPPET_BASE?targetPackage=$pkg"
        } else {
            ADD_SNIPPET_BASE
        }
    }

    fun passSnippetId(id: Int): String {
        return "$EDIT_SNIPPET_BASE/$id"
    }
}

@Composable
fun QuickFillApp(
    targetPackage: String?,
    viewModel: SnippetViewModel = viewModel()
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    var hasCompletedOnboarding by remember {
        mutableStateOf(hasCompletedOnboarding(context))
    }

    var isEnabled by remember {
        mutableStateOf(isAutofillServiceEnabled(context))
    }

    LifecycleResumeEffect(Unit) {
        isEnabled = isAutofillServiceEnabled(context)
        onPauseOrDispose { }
    }

    LaunchedEffect(isEnabled, targetPackage) {
        if (!isEnabled && !hasCompletedOnboarding) {
            navController.navigate(Dest.ONBOARDING) {
                popUpTo(0)
            }
        } else if (!isEnabled) {
            navController.navigate(Dest.SETUP) {
                popUpTo(0)
            }
        } else if (targetPackage != null) {
            navController.navigate("add_snippet?targetPackage=$targetPackage")
        }
    }

    QuickFillNavHost(
        navController = navController,
        hasCompletedOnboarding = hasCompletedOnboarding,
        isEnabled = isEnabled,
        viewModel = viewModel,
        onOnboardingComplete = {
            setHasCompletedOnboarding(context)
            hasCompletedOnboarding = true
        },
        onEnableClick = { isEnabled = isAutofillServiceEnabled(context) }
    )
}

@Composable
fun QuickFillNavHost(
    navController: androidx.navigation.NavHostController,
    hasCompletedOnboarding: Boolean,
    isEnabled: Boolean,
    viewModel: SnippetViewModel,
    onOnboardingComplete: () -> Unit,
    onEnableClick: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = when {
            isEnabled -> Dest.SNIPPET_LIST
            hasCompletedOnboarding -> Dest.SETUP
            else -> Dest.ONBOARDING
        }
    ) {
        composable(Dest.ONBOARDING) {
            OnboardingScreen(
                onComplete = {
                    onOnboardingComplete()
                    onEnableClick()
                }
            )
        }

        composable(Dest.SETUP) {
            QuickFillSetupScreen()
        }

        composable(Dest.SNIPPET_LIST) {
            SnippetListScreen(
                viewModel = viewModel,
                onAddClick = { navController.navigate(Dest.passTargetPackage()) },
                onEditClick = { id -> navController.navigate(Dest.passSnippetId(id)) }
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

        composable(
            route = Dest.EDIT_SNIPPET,
            arguments = listOf(
                navArgument("snippetId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("snippetId") ?: 0
            AddSnippetScreen(
                viewModel = viewModel,
                targetPackage = null,
                snippetId = id,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

fun isAutofillServiceEnabled(context: Context): Boolean {
    val autofillManager = context.getSystemService(AutofillManager::class.java)
    return autofillManager != null && autofillManager.hasEnabledAutofillServices()
}

private const val PREFS_NAME = "quickfill_prefs"
private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"

private fun hasCompletedOnboarding(context: Context): Boolean {
    return context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(KEY_ONBOARDING_COMPLETED, false)
}

private fun setHasCompletedOnboarding(context: Context) {
    context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(KEY_ONBOARDING_COMPLETED, true)
        .apply()
}
