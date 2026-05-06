package com.byteutility.dev.quickfill.ui.snippets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteutility.dev.quickfill.data.local.Snippet
import com.byteutility.dev.quickfill.data.repository.SnippetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SnippetsUiState {
    object Loading : SnippetsUiState
    data class Success(
        val snippets: List<Snippet>,
        val targetPackage: String? = null,
        val knownPackages: List<String> = emptyList(),
        val editingSnippet: Snippet? = null
    ) : SnippetsUiState
    data class Error(val message: String) : SnippetsUiState
}

@HiltViewModel
class SnippetViewModel @Inject constructor(
    private val snippetRepository: SnippetRepository
) : ViewModel() {

    private val _targetPackage = MutableStateFlow<String?>(null)
    private val _editingSnippet = MutableStateFlow<Snippet?>(null)

    /**
     * ARCHITECTURAL DECISION: Using 'combine' ensures the UI always has a consistent snapshot 
     * of all required data (snippets, selected package, and known apps) simultaneously.
     * This prevents "race conditions" where the UI might show a selected package that 
     * hasn't been validated against the known packages list yet.
     */
    val uiState: StateFlow<SnippetsUiState> = combine(
        snippetRepository.getSnippetsStream(),
        snippetRepository.getKnownPackagesStream(),
        _targetPackage,
        _editingSnippet
    ) { snippets, knownPackages, targetPackage, editingSnippet ->
        SnippetsUiState.Success(
            snippets = snippets,
            knownPackages = knownPackages,
            targetPackage = targetPackage,
            editingSnippet = editingSnippet
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SnippetsUiState.Loading
    )

    fun saveSnippet(
        label: String,
        value: String,
        category: String,
        packageName: String? = null,
        id: Int = 0
    ) {
        if (label.isBlank() || value.isBlank()) return

        viewModelScope.launch {
            val snippet = Snippet(
                id = id,
                label = label.trim(),
                value = value.trim(),
                category = category,
                targetPackage = packageName
            )
            snippetRepository.insertSnippet(snippet)
        }
    }

    fun setInitialPackage(packageName: String?) {
        _targetPackage.value = packageName
    }

    fun loadSnippetForEditing(id: Int) {
        viewModelScope.launch {
            val snippet = snippetRepository.getSnippetById(id)
            _editingSnippet.value = snippet
            // Also sync the target package so the UI tabs/pickers show the right info
            _targetPackage.value = snippet?.targetPackage
        }
    }

    fun clearEditingSnippet() {
        _editingSnippet.value = null
        _targetPackage.value = null
    }

    /**
     * Updates the package associated with the new snippet.
     * Called when the user manually selects an app from the "Known Apps" list.
     */
    fun updateSelectedPackage(packageName: String?) {
        _targetPackage.value = packageName
    }

    fun getAppMetadata(packageName: String) = 
        snippetRepository.getAppMetadataStream(packageName)

    fun deleteSnippet(snippet: Snippet) {
        viewModelScope.launch {
            snippetRepository.deleteSnippet(snippet)
        }
    }
}
