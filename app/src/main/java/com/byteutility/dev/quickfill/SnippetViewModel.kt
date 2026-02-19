package com.byteutility.dev.quickfill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    val allSnippets: StateFlow<List<Snippet>> = snippetDao.getAllSnippets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveSnippet(content: String, category: String) {}

    /**
     * Creates and saves a snippet.
     * @param label: Friendly name (e.g., "Personal Email")
     * @param value: The actual text (e.g., "me@example.com")
     * @param category: Filter tag (e.g., "Social", "Work")
     */
    fun saveSnippet(label: String, value: String, category: String) {
        if (label.isBlank() || value.isBlank()) return

        viewModelScope.launch {
            val snippet = Snippet(
                label = label.trim(),
                value = value.trim(),
                category = category
            )
            snippetDao.insertSnippet(snippet)
        }
    }

    fun deleteSnippet(snippet: Snippet) {
        viewModelScope.launch {
            snippetDao.deleteSnippet(snippet)
        }
    }
}