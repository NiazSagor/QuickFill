package com.byteutility.dev.quickfill.ui.snippets

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteutility.dev.quickfill.data.local.Snippet
import com.byteutility.dev.quickfill.data.local.SnippetDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SnippetViewModel @Inject constructor(
    private val snippetDao: SnippetDao
) : ViewModel() {

    var targetPackage by mutableStateOf<String?>(null)

    val allSnippets: StateFlow<List<Snippet>> = snippetDao.getAllSnippets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Creates and saves a snippet.
     * @param label: Friendly name (e.g., "Personal Email")
     * @param value: The actual text (e.g., "me@example.com")
     * @param category: Filter tag (e.g., "Social", "Work")
     */
    fun saveSnippet(label: String, value: String, category: String, packageName: String? = null) {
        if (label.isBlank() || value.isBlank()) return

        viewModelScope.launch {
            val snippet = Snippet(
                label = label.trim(),
                value = value.trim(),
                category = category,
                targetPackage = packageName
            )
            snippetDao.insertSnippet(snippet)
        }
    }

    fun setInitialPackage(packageName: String?) {
        targetPackage = packageName
    }

    fun deleteSnippet(snippet: Snippet) {
        viewModelScope.launch {
            snippetDao.deleteSnippet(snippet)
        }
    }
}