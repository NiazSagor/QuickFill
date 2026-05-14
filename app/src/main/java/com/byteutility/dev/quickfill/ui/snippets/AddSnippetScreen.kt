package com.byteutility.dev.quickfill.ui.snippets

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private const val TAG = "AddSnippetScreen"
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSnippetScreen(
    viewModel: SnippetViewModel,
    onBack: () -> Unit,
    targetPackage: String?,
    snippetId: Int? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isFromAutofill = targetPackage != null
    val isEditMode = snippetId != null && snippetId != 0

    // Local UI state for form inputs
    var label by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("GENERAL") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var appExpanded by remember { mutableStateOf(false) }
    
    // Initialized to true if from autofill, else false (Global)
    var isAppPinned by remember { mutableStateOf(isFromAutofill) }

    val categories = listOf("GENERAL", "SOCIAL", "FINANCE", "WORK", "IDENTITY", "GAME")

    LaunchedEffect(targetPackage, snippetId) {
        if (isEditMode) {
            viewModel.loadSnippetForEditing(snippetId!!)
        } else {
            viewModel.setInitialPackage(targetPackage)
            if (isFromAutofill) {
                isAppPinned = true
            }
        }
    }

    // Sync form with loaded snippet
    LaunchedEffect(uiState) {
        val state = uiState
        if (state is SnippetsUiState.Success && state.editingSnippet != null && isEditMode) {
            label = state.editingSnippet.label
            value = state.editingSnippet.value
            category = state.editingSnippet.category
            isAppPinned = state.editingSnippet.targetPackage != null
        }
    }

    // Clear state when leaving
    LaunchedEffect(Unit) {
        // onDispose doesn't exist in LaunchedEffect, using DisposableEffect for cleanup if needed
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Snippet" else "New Snippet", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearEditingSnippet()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxSize()
        ) {
            when (val state = uiState) {
                is SnippetsUiState.Success -> {

                    // --- SCOPE SELECTION ---
                    Text(
                        "Availability",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = !isAppPinned,
                            onClick = {
                                isAppPinned = false
                                viewModel.updateSelectedPackage(null)
                            },
                            enabled = !isFromAutofill && !isEditMode, // Disable if coming from specific app or in edit mode
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            icon = { Icon(Icons.Default.Build, null, Modifier.size(18.dp)) }
                        ) { Text("Global") }
                        SegmentedButton(
                            selected = isAppPinned,
                            onClick = {
                                isAppPinned = true
                            },
                            enabled = !isEditMode, // Disable changing scope in edit mode
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            icon = { Icon(Icons.Default.Star, null, Modifier.size(18.dp)) }
                        ) { Text("App-Pinned") }
                    }
                    Spacer(Modifier.height(24.dp))

                    // --- APP PICKER OR HEADER (Only for App-Pinned) ---
                    if (isAppPinned) {
                        Column {
                            if (!isFromAutofill && !isEditMode) {
                                ExposedDropdownMenuBox(
                                    expanded = appExpanded,
                                    onExpandedChange = { appExpanded = !appExpanded }
                                ) {
                                    val selectedPkg = state.targetPackage
                                    OutlinedTextField(
                                        value = if (selectedPkg != null) getAppLabel(selectedPkg) else "Select App",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Target App") },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(
                                                appExpanded
                                            )
                                        },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    
                                    ExposedDropdownMenu(
                                        expanded = appExpanded,
                                        onDismissRequest = {
                                            appExpanded = false
                                        }
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
                                // Locked Header when from Autofill OR in Edit Mode
                                AppSpecificHeader(state.targetPackage ?: targetPackage ?: "")
                            }

                            // Info Card
                            Card(
                                modifier = Modifier.padding(vertical = 12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "This snippet will automatically pop up only in ${
                                            getAppLabel(
                                                state.targetPackage ?: "the selected app"
                                            )
                                        }.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                        }
                    }

                    // --- INPUT FIELDS ---
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("Label") },
                        placeholder = { Text("e.g. Work Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = value,
                        onValueChange = { value = it },
                        label = { Text("Value / Content") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // --- CATEGORY (Only show for Global) ---
                    if (!isAppPinned) {
                        Column {
                            Spacer(Modifier.height(16.dp))
                            ExposedDropdownMenuBox(
                                expanded = categoryExpanded,
                                onExpandedChange = { categoryExpanded = !categoryExpanded }
                            ) {
                                OutlinedTextField(
                                    value = category,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Category") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            categoryExpanded
                                        )
                                    },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
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
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            if (label.isNotBlank() && value.isNotBlank()) {
                                // Decide category internally if it's app-specific
                                val finalCategory = if (isAppPinned) "APP_SPECIFIC" else category
                                val finalPackage = if (isAppPinned) state.targetPackage else null
                                
                                viewModel.saveSnippet(
                                    label = label,
                                    value = value,
                                    category = finalCategory,
                                    packageName = finalPackage,
                                    id = snippetId ?: 0
                                )
                                viewModel.clearEditingSnippet()
                                onBack()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(Icons.Default.Done, null)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            if (isEditMode) "Update Snippet" else "Save to Vault",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                else -> { /* Handle Loading/Error */ }
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
                Image(
                    bitmap = appIcon.toBitmap().asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Icon(Icons.Default.Settings, contentDescription = null)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(appLabel, style = MaterialTheme.typography.titleMedium)
        }
    }
}
