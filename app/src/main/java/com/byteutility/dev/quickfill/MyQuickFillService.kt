package com.byteutility.dev.quickfill

import android.app.PendingIntent
import android.app.assist.AssistStructure
import android.content.Intent
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
import androidx.autofill.inline.v1.InlineSuggestionUi

class MyQuickFillService : AutofillService() {

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        val structure = request.fillContexts.last().structure
        val packageName = structure.activityComponent.packageName
        val responseBuilder = FillResponse.Builder()

        val category = runCatching {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) appInfo.category else -1
        }.getOrDefault(-1)

        val focusedField = findFocusedNode(structure) ?: return
        val fillId = focusedField.autofillId ?: return

        val snippets = getSnippetsForCategory(category)

        snippets.forEach { snippet ->
            val menuPresentation =
                RemoteViews(this.packageName, android.R.layout.simple_list_item_1).apply {
                    setTextViewText(android.R.id.text1, snippet.label)
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

            responseBuilder.addDataset(dataset)
        }

        callback.onSuccess(responseBuilder.build())
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

    private fun getSnippetsForCategory(category: Int): List<Snippet> {
        return when (category) {
            // android.content.pm.ApplicationInfo.CATEGORY_SOCIAL
            4 -> listOf(
                Snippet(label = "Greeting", value = "Hey! How's it going?", categoryTag = "Social"),
                Snippet(
                    label = "Quick Reply",
                    value = "I'll be there in 5 mins!",
                    categoryTag = "Social"
                )
            )
            // android.content.pm.ApplicationInfo.CATEGORY_SHOPPING
            2 -> listOf(
                Snippet(
                    label = "Home Address",
                    value = "123 Tech Lane, NY 10001",
                    categoryTag = "Shopping"
                ),
                Snippet(label = "Discount Code", value = "SAVE20NOW", categoryTag = "Shopping")
            )
            // Default "General" snippets for any other app (like Search or Chrome)
            else -> listOf(
                Snippet(label = "My Email", value = "user@example.com", categoryTag = "General"),
                Snippet(label = "Phone", value = "+1-555-0199", categoryTag = "General"),
                Snippet(label = "ID Number", value = "ABC-123-XYZ", categoryTag = "General")
            )
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