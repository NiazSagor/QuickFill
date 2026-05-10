package com.byteutility.dev.quickfill.service

import org.junit.Assert.assertEquals
import org.junit.Test

class MyQuickFillServiceLogicTest {

    @Test
    fun detectCategory_SocialApps() {
        // -1 is CAT_UNDEFINED
        assertEquals("SOCIAL", CategoryDetector.detectCategory(-1, "com.whatsapp"))
        // facebook.orca should map to SOCIAL because it contains "messenger" in original logic, 
        // but wait, does it? My refactored logic uses "messenger" check.
        assertEquals("SOCIAL", CategoryDetector.detectCategory(-1, "com.facebook.orca"))
    }

    @Test
    fun detectCategory_SystemCategories() {
        // CAT_SOCIAL = 4, CAT_MAPS = 5, CAT_PRODUCTIVITY = 1
        assertEquals("SOCIAL", CategoryDetector.detectCategory(4, "any.package"))
        assertEquals("MAPS", CategoryDetector.detectCategory(5, "any.package"))
        assertEquals("WORK", CategoryDetector.detectCategory(1, "any.package"))
    }

    @Test
    fun detectCategory_FinanceApps() {
        assertEquals("FINANCE", CategoryDetector.detectCategory(-1, "com.some.bank.app"))
        assertEquals("FINANCE", CategoryDetector.detectCategory(-1, "my.wallet.app"))
    }

    @Test
    fun detectCategory_GeneralFallback() {
        assertEquals("GENERAL", CategoryDetector.detectCategory(-1, "com.example.unknown"))
    }
}
