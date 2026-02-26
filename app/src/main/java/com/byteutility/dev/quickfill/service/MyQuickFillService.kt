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
import android.view.autofill.AutofillValue
import android.view.inputmethod.InlineSuggestionsRequest
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.autofill.inline.v1.InlineSuggestionUi
import com.byteutility.dev.quickfill.data.local.Snippet
import com.byteutility.dev.quickfill.data.local.SnippetDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.collections.forEach

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

        val category = runCatching {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            detectCategory(appInfo, appInfo.packageName)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) getSnippetCategory(appInfo.category) else getSnippetsForCategory(-1)
        }.getOrDefault("GENERAL")

        val focusedField = findFocusedNode(structure)
        if (focusedField == null) {
            callback.onSuccess(null)
            return
        }
        val fillId = focusedField.autofillId ?: return

        serviceScope.launch {
            try {
                val snippets = getSnippetsForCategory(category)

                if (snippets.isEmpty()) {
                    callback.onSuccess(null)
                    return@launch
                }

                val finalResponseBuilder = FillResponse.Builder()

                snippets.forEach { snippet ->

                    val menuPresentation =
                        RemoteViews(
                            this@MyQuickFillService.packageName,
                            R.layout.simple_list_item_1
                        ).apply {
                            setTextViewText(R.id.text1, snippet.label)
                        }

                    val presBuilder = Presentations.Builder()
                        .setMenuPresentation(menuPresentation)

                    request.inlineSuggestionsRequest?.let { inlineReq ->
                        val inlinePres = createInlinePresentation(snippet, inlineReq)
                        if (inlinePres != null) {
                            presBuilder.setInlinePresentation(inlinePres)
                        }
                    }

                    val field = Field.Builder()
                        .setValue(AutofillValue.forText(snippet.value))
                        .setPresentations(presBuilder.build())
                        .build()

                    val dataset = Dataset.Builder()
                        .setField(fillId, field)
                        .build()

                    finalResponseBuilder.addDataset(dataset)
                }

                callback.onSuccess(finalResponseBuilder.build())

            } catch (e: Exception) {
                callback.onFailure(e.message)
            }
        }
    }

    override fun onSaveRequest(
        request: SaveRequest,
        callback: SaveCallback
    ) {
        callback.onSuccess()
    }

    private fun createInlinePresentation(
        snippet: Snippet,
        inlineRequest: InlineSuggestionsRequest
    ): InlinePresentation? {
        val spec = inlineRequest.inlinePresentationSpecs.firstOrNull() ?: return null

        val sliceBuilder = InlineSuggestionUi.newContentBuilder(
            PendingIntent.getActivity(this, 0, Intent(), PendingIntent.FLAG_IMMUTABLE)
        )
        sliceBuilder.setTitle(snippet.label)

        return InlinePresentation(sliceBuilder.build().slice, spec, false)
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
        if (node.isFocused) return node
        for (i in 0 until node.childCount) {
            val found = searchForFocused(node.getChildAt(i))
            if (found != null) return found
        }
        return null
    }
}