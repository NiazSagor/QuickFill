package com.byteutility.dev.quickfill.service

import android.R
import android.app.PendingIntent
import android.app.assist.AssistStructure
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.Dataset
import android.service.autofill.Field
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.InlinePresentation
import android.service.autofill.Presentations
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import android.util.Log
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.view.inputmethod.InlineSuggestionsRequest
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.autofill.inline.v1.InlineSuggestionUi
import com.byteutility.dev.quickfill.data.local.Snippet
import com.byteutility.dev.quickfill.data.local.SnippetDao
import com.byteutility.dev.quickfill.ui.AutofillTrampolineActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "MyQuickFillService"

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.O)
class MyQuickFillService : AutofillService() {

    @Inject
    lateinit var snippetDao: SnippetDao

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())


    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        val structure = request.fillContexts.last().structure
        val packageName = structure.activityComponent.packageName

        Log.d(TAG, "onFillRequest: packageName $packageName")

        val focusedField = findFocusedNode(structure)
        if (focusedField == null) {
            callback.onSuccess(null)
            return
        }

        val fillId = focusedField.autofillId ?: run {
            callback.onSuccess(null)
            return
        }

        serviceScope.launch {
            try {
                val category = runCatching {
                    val appInfo = packageManager.getApplicationInfo(packageName, 0)
                    detectCategory(appInfo, appInfo.packageName)
                }.getOrDefault("GENERAL")

                val snippets = getSnippetsForCategory(category)

                val datasets = if (snippets.isEmpty()) {
                    listOf(buildAddSnippetDataset(packageName, fillId, request))
                } else {
                    snippets.map { buildSnippetDataset(it, fillId, request) }
                }

                val response = FillResponse.Builder()
                    .apply { datasets.forEach { addDataset(it) } }
                    .build()

                callback.onSuccess(response)
            } catch (e: Exception) {
                Log.e(TAG, "onFillRequest error", e)
                callback.onFailure(e.message)
            }
        }
    }

    override fun onSaveRequest(
        request: SaveRequest,
        callback: SaveCallback
    ) {
    }

    private fun buildAddSnippetDataset(
        packageName: String,
        fillId: AutofillId,
        request: FillRequest
    ): Dataset {
        val addSnippet = Snippet(
            id = -1,
            label = "➕ Add for ${packageName.split(".").last()}",
            value = "ACTION_ADD_SNIPPET::$packageName",
            category = "GENERAL"
        )

        val pendingIntent = buildTrampolinePendingIntent(packageName, addSnippet.id.hashCode())

        val presentations = buildPresentations(addSnippet, request, pendingIntent)

        val field = Field.Builder()
            .setValue(AutofillValue.forText(addSnippet.value))
            .setPresentations(presentations)
            .build()

        return Dataset.Builder()
            .setAuthentication(pendingIntent.intentSender)
            .setField(fillId, field)
            .build()
    }

    private fun buildSnippetDataset(
        snippet: Snippet,
        fillId: AutofillId,
        request: FillRequest
    ): Dataset {
        val presentations = buildPresentations(snippet, request, pendingIntent = null)

        val field = Field.Builder()
            .setValue(AutofillValue.forText(snippet.value))
            .setPresentations(presentations)
            .build()

        return Dataset.Builder()
            .setField(fillId, field)
            .build()
    }

    private fun buildPresentations(
        snippet: Snippet,
        request: FillRequest,
        pendingIntent: PendingIntent?
    ): Presentations {
        val menuPresentation = RemoteViews(packageName, R.layout.simple_list_item_1).apply {
            setTextViewText(R.id.text1, snippet.label)
        }

        val presBuilder = Presentations.Builder()
            .setMenuPresentation(menuPresentation)

        request.inlineSuggestionsRequest?.let { inlineReq ->
            val inlinePres = createInlinePresentation(snippet, inlineReq, pendingIntent)
            if (inlinePres != null) presBuilder.setInlinePresentation(inlinePres)
        }

        return presBuilder.build()
    }

    private fun buildTrampolinePendingIntent(packageName: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, AutofillTrampolineActivity::class.java).apply {
            putExtra("TARGET_PACKAGE", packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createInlinePresentation(
        snippet: Snippet,
        inlineRequest: InlineSuggestionsRequest,
        pendingIntent: PendingIntent? = null
    ): InlinePresentation? {
        val spec = inlineRequest.inlinePresentationSpecs.firstOrNull() ?: return null

        val pi = pendingIntent ?: PendingIntent.getActivity(
            this, 0, Intent(), PendingIntent.FLAG_IMMUTABLE
        )

        val slice = InlineSuggestionUi.newContentBuilder(pi)
            .setTitle(snippet.label)
            .build()
            .slice

        return InlinePresentation(slice, spec, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun detectCategory(info: ApplicationInfo, packageName: String): String {
        return when (info.category) {
            ApplicationInfo.CATEGORY_SOCIAL -> "SOCIAL"
            ApplicationInfo.CATEGORY_MAPS -> "MAPS"
            ApplicationInfo.CATEGORY_PRODUCTIVITY -> "WORK"
            ApplicationInfo.CATEGORY_GAME -> "GAME"
            ApplicationInfo.CATEGORY_AUDIO -> "AUDIO"
            ApplicationInfo.CATEGORY_VIDEO -> "VIDEO"
            ApplicationInfo.CATEGORY_IMAGE -> "IMAGE"
            ApplicationInfo.CATEGORY_NEWS -> "NEWS"
            else -> {
                when {
                    packageName.contains("whatsapp") || packageName.contains("messenger") -> "SOCIAL"
                    packageName.contains("amazon") || packageName.contains("ebay") -> "SHOPPING"
                    packageName.contains("bank") || packageName.contains("wallet") -> "FINANCE"
                    else -> "GENERAL"
                }
            }
        }
    }

    private suspend fun getSnippetsForCategory(category: String): List<Snippet> {
        return withContext(Dispatchers.IO) {
            val specific = snippetDao.getSnippetsByCategory(category).first()
            val general = snippetDao.getSnippetsByCategory("GENERAL").first()
            (specific + general).distinctBy { it.id }
        }
    }

    private fun findFocusedNode(structure: AssistStructure): AssistStructure.ViewNode? {
        val windowCount = structure.windowNodeCount
        for (i in 0 until windowCount) {
            val node = structure.getWindowNodeAt(i).rootViewNode
            val focused = searchForFocused(node)
            if (focused != null) return focused
        }
        return null
    }

    private fun searchForFocused(node: AssistStructure.ViewNode): AssistStructure.ViewNode? {
        val isTextInput = node.autofillId != null &&
                (node.className?.contains("EditText") == true ||
                        node.inputType != 0)

        if (isTextInput) return node

        for (i in 0 until node.childCount) {
            val found = searchForFocused(node.getChildAt(i))
            if (found != null) return found
        }
        return null
    }
}
