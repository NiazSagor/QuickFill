package com.byteutility.dev.quickfill.ui.snippets

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
    val isAppSpecific = targetPackage != null

    LaunchedEffect(targetPackage) {
        viewModel.setInitialPackage(targetPackage)
    }
    var label by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("GENERAL") }
    var isExpanded by remember { mutableStateOf(false) }

    val categories = listOf(
        "GENERAL",
        "SOCIAL",
        "FINANCE",
        "WORK",
        "IDENTITY",
        "GAME",
        "AUDIO",
        "VIDEO",
        "IMAGE",
        "DOCUMENT",
        "SOCIAL",
        "NEWS",
        "MAPS",
        "PRODUCTIVITY",
        "ACCESSIBILITY"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Snippet") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        // Content of the screen
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(start = 16.dp, bottom = 16.dp, end = 16.dp)
        ) {
            if (isAppSpecific) {
                AppSpecificHeader(targetPackage!!)

                Text(
                    text = "This snippet will only appear when you are using this specific app. It will take priority over your general snippets.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // 1. Label Input
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Label (e.g., My Personal Email)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 2. Value Input
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text("Value (e.g., me@email.com)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            if (!isAppSpecific) {
                // 3. Category Selection (Dropdown)
                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = { isExpanded = !isExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false }
                    ) {
                        categories.forEach { selection ->
                            DropdownMenuItem(
                                text = { Text(selection) },
                                onClick = {
                                    category = selection
                                    isExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 4. Save Button
            Button(
                onClick = {
                    if (label.isNotBlank() && value.isNotBlank()) {
                        viewModel.saveSnippet(label, value, category, targetPackage)
                        onBack() // Navigate back after saving
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Save to Vault")
            }
        }
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

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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

            Column {
                Text(appLabel, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
