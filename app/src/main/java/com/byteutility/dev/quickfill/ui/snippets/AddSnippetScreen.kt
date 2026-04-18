package com.byteutility.dev.quickfill.ui.snippets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSnippetScreen(
    viewModel: SnippetViewModel,
    onBack: () -> Unit,
    targetPackage: String?
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // NAVIGATION LOGIC: If a package is passed via navigation, it's an "Autofill" flow
    // which takes precedence and locks the scope.
    val isFromAutofill = targetPackage != null

    LaunchedEffect(targetPackage) {
        viewModel.setInitialPackage(targetPackage)
    }

    var label by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("GENERAL") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var appExpanded by remember { mutableStateOf(false) }

    val categories = listOf("GENERAL", "SOCIAL", "FINANCE", "WORK", "IDENTITY", "GAME")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Snippet") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            when (val state = uiState) {
                is SnippetsUiState.Loading -> { /* Handled by parent or show placeholder */ }
                is SnippetsUiState.Success -> {
                    
                    // ARCHITECTURAL DECISION: We allow the user to toggle scope only if not 
                    // in the middle of an Autofill flow.
                    if (!isFromAutofill) {
                        Text("Scope", style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(8.dp))
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = state.targetPackage == null,
                                onClick = { viewModel.updateSelectedPackage(null) },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                            ) { Text("General") }
                            SegmentedButton(
                                selected = state.targetPackage != null,
                                onClick = { 
                                    // Default to first known app if available when switching to app-specific
                                    if (state.targetPackage == null && state.knownPackages.isNotEmpty()) {
                                        viewModel.updateSelectedPackage(state.knownPackages.first())
                                    }
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                            ) { Text("App Specific") }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    // App Selection UI
                    AnimatedVisibility(visible = state.targetPackage != null) {
                        Column {
                            if (!isFromAutofill && state.knownPackages.isNotEmpty()) {
                                ExposedDropdownMenuBox(
                                    expanded = appExpanded,
                                    onExpandedChange = { appExpanded = !appExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = getAppLabel(state.targetPackage!!),
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Select App") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(appExpanded) },
                                        modifier = Modifier.menuAnchor().fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = appExpanded,
                                        onDismissRequest = { appExpanded = false }
                                    ) {
                                        state.knownPackages.forEach { pkg ->
                                            DropdownMenuItem(
                                                text = { Text(getAppLabel(pkg)) },
                                                onClick = {
                                                    viewModel.updateSelectedPackage(pkg)
                                                    appExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            } else {
                                AppSpecificHeader(state.targetPackage ?: "")
                            }
                            
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "App-specific snippets take priority.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                    }

                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("Label (e.g., My Email)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = value,
                        onValueChange = { value = it },
                        label = { Text("Value") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    Spacer(Modifier.height(16.dp))

                    if (state.targetPackage == null) {
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = !categoryExpanded }
                        ) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                categories.forEach { sel ->
                                    DropdownMenuItem(
                                        text = { Text(sel) },
                                        onClick = {
                                            category = sel
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            if (label.isNotBlank() && value.isNotBlank()) {
                                viewModel.saveSnippet(label, value, category, state.targetPackage)
                                onBack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Check, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Save to Vault")
                    }
                }
                is SnippetsUiState.Error -> { /* Show error UI */ }
            }
        }
    }
}

/**
 * PERFORMANCE DECISION: Context lookups for App Labels and Icons are expensive.
 * We use 'remember' to ensure the lookup only happens when the packageName changes,
 * preventing UI stutter during scroll or irrelevant recompositions.
 */
@Composable
fun getAppLabel(packageName: String): String {
    val context = LocalContext.current
    return remember(packageName) {
        runCatching {
            val pm = context.packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
        }.getOrDefault(packageName.split(".").last())
    }
}

@Composable
fun AppSpecificHeader(packageName: String) {
    val context = LocalContext.current
    val (appLabel, appIcon) = remember(packageName) {
        runCatching {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString() to pm.getApplicationIcon(info)
        }.getOrDefault(packageName.split(".").last() to null)
    }

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (appIcon != null) {
                Image(bitmap = appIcon.toBitmap().asImageBitmap(), contentDescription = null, modifier = Modifier.size(32.dp))
            } else {
                Icon(Icons.Default.Settings, contentDescription = null)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(appLabel, style = MaterialTheme.typography.titleMedium)
        }
    }
}
