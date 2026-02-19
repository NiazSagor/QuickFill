package com.byteutility.dev.quickfill

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AddSnippetScreen(viewModel: SnippetViewModel = viewModel()) {
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Snippet Text") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Simple Category Picker
        Text("Category: $category")
        Row {
            listOf("Social", "Shopping", "General").forEach { cat ->
                Button(onClick = { category = cat }, modifier = Modifier.padding(4.dp)) {
                    Text(cat)
                }
            }
        }

        Button(
            onClick = {
                viewModel.saveSnippet(content, category)
                content = "" // Clear after save
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Save to Vault")
        }
    }
}