package com.byteutility.dev.quickfill

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun QuickFillApp(viewModel: SnippetViewModel = viewModel()) {
    var currentScreen by remember { mutableStateOf("LIST") }

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