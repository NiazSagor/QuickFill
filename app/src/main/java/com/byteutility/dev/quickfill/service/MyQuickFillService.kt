package com.byteutility.dev.quickfill.service


import android.app.PendingIntent
import android.app.assist.AssistStructure
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
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
import androidx.core.graphics.drawable.toBitmap
import com.byteutility.dev.quickfill.R
import com.byteutility.dev.quickfill.data.local.Snippet
import com.byteutility.dev.quickfill.data.repository.SnippetRepository
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
    lateinit var snippetRepository: SnippetRepository

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
                // ARCHITECTURAL DECISION: Save metadata here because we have visibility.
                snippetRepository.saveAppMetadataFromSystem(packageName)

                val category = runCatching {
                    val appInfo = packageManager.getApplicationInfo(packageName, 0)
                    detectCategory(appInfo, appInfo.packageName)
                }.getOrDefault("GENERAL")

                val snippets = getSnippetsForAutofill(packageName, category)

                val datasets = snippets.map { buildSnippetDataset(it, fillId, request) } +
                        buildAddSnippetDataset(packageName, fillId, request)

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

    private fun getAppIcon(packageName: String): Bitmap? {
        return runCatching {
            val drawable = packageManager.getApplicationIcon(packageName)
            drawable.toBitmap() // requires androidx.core.graphics.drawable.toBitmap
        }.getOrNull()
    }

    private fun buildAddSnippetDataset(
        packageName: String,
        fillId: AutofillId,
        request: FillRequest
    ): Dataset {
        val appLabel = getAppLabelForPackage(packageName)
        val appIcon = getAppIcon(packageName) // Fetch the icon here

        val addSnippet = Snippet(
            id = -1,
            label = "Add for $appLabel",
            value = "ACTION_ADD_SNIPPET::$packageName",
            category = "GENERAL"
        )

        val pendingIntent = buildTrampolinePendingIntent(packageName, addSnippet.id.hashCode())

        val presentations = buildAddSnippetPresentations(addSnippet, request, pendingIntent, appIcon)

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
        val appIcon = snippet.targetPackage?.let { getAppIcon(it) }
        val presentations = buildPresentations(snippet, request, pendingIntent = null, appIcon)

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
        pendingIntent: PendingIntent?,
        appIcon: Bitmap?
    ): Presentations {
        val menuPresentation = RemoteViews(this@MyQuickFillService.packageName, R.layout.autofill_item).apply {
            setTextViewText(R.id.autofill_title, snippet.label)
            setTextViewText(R.id.autofill_subtitle, getAutofillSubtitle(snippet))
            if (appIcon != null) {
                setImageViewBitmap(R.id.autofill_icon, appIcon)
            } else {
                setImageViewResource(R.id.autofill_icon, getAutofillIconResource(snippet))
            }
        }


        val presBuilder = Presentations.Builder()
            .setMenuPresentation(menuPresentation)

        request.inlineSuggestionsRequest?.let { inlineReq ->
            val inlinePres = createInlinePresentation(snippet, inlineReq, pendingIntent)
            if (inlinePres != null) presBuilder.setInlinePresentation(inlinePres)
        }

        return presBuilder.build()
    }

    private fun buildAddSnippetPresentations(
        snippet: Snippet,
        request: FillRequest,
        pendingIntent: PendingIntent,
        appIcon: Bitmap?
    ): Presentations {
        val menuPresentation = RemoteViews(
            this@MyQuickFillService.packageName,
            R.layout.autofill_action_item
        ).apply {
            setTextViewText(R.id.autofill_action_text, snippet.label)
            if (appIcon != null) {
                setImageViewBitmap(R.id.autofill_action_icon, appIcon)
            } else {
                setImageViewResource(R.id.autofill_action_icon, android.R.drawable.ic_menu_add)
            }
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
            .setTitle(getAutofillDisplayLabel(snippet))
            .build()
            .slice

        return InlinePresentation(slice, spec, false)
    }

    private fun getAutofillDisplayLabel(snippet: Snippet): String {
        return if (snippet.targetPackage == null && snippet.id != -1) {
            "${snippet.label} - Global"
        } else {
            snippet.label
        }
    }

    private fun getAutofillSubtitle(snippet: Snippet): String {
        return when {
            snippet.id == -1 -> "App-specific"
            snippet.targetPackage == null -> "Global - ${snippet.category}"
            else -> "App-specific"
        }
    }

    private fun getAutofillIconResource(snippet: Snippet): Int {
        return when (snippet.category.uppercase()) {
            "WORK" -> android.R.drawable.ic_dialog_email
            "SOCIAL" -> android.R.drawable.ic_menu_share
            "FINANCE" -> android.R.drawable.ic_menu_manage
            "IDENTITY" -> android.R.drawable.ic_menu_myplaces
            "GAME" -> android.R.drawable.ic_media_play
            else -> android.R.drawable.ic_menu_edit
        }
    }

    private fun getAppLabelForPackage(packageName: String): String {
        return runCatching {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        }.getOrDefault(packageName.split(".").last())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun detectCategory(info: ApplicationInfo, packageName: String): String {
        return CategoryDetector.detectCategory(info.category, packageName)
    }

    private suspend fun getGlobalSnippetsForCategory(category: String): List<Snippet> {
        return withContext(Dispatchers.IO) {
            snippetRepository.getGlobalSnippetsForCategoryStream(category).first()
        }
    }

    private suspend fun getSnippetsForPackage(p: String): List<Snippet> {
        return withContext(Dispatchers.IO) {
            val specific = snippetRepository.getSnippetsForPackageStream(p).first()
            (specific).distinctBy { it.id }
        }
    }

    private suspend fun getSnippetsForAutofill(packageName: String, category: String): List<Snippet> {
        val appSpecific = getSnippetsForPackage(packageName)
        return appSpecific.ifEmpty {
            getGlobalSnippetsForCategory(category).distinctBy { it.id }
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
