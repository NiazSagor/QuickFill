package com.byteutility.dev.quickfill.data.repository

interface AutofillSettingsRepository {
    fun isQuickFillAutofillEnabled(): Boolean
    fun openQuickFillAutofillSettings(): Boolean
}
