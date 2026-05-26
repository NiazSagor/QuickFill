package com.byteutility.dev.quickfill.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.autofill.AutofillManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultAutofillSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : AutofillSettingsRepository {

    override fun isQuickFillAutofillEnabled(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false

        val autofillManager = context.getSystemService(AutofillManager::class.java)
        return autofillManager != null &&
                autofillManager.isAutofillSupported &&
                autofillManager.hasEnabledAutofillServices()
    }

    override fun openQuickFillAutofillSettings(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false

        val intent = Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            true
        } else {
            false
        }
    }
}
