package com.byteutility.dev.quickfill.service

import android.content.pm.ApplicationInfo
import org.junit.Assert.assertEquals
import org.junit.Test
import io.mockk.every
import io.mockk.mockk

class MyQuickFillServiceLogicTest {

    // Note: We need a way to test detectCategory without a full service instance 
    // since it's a regular function in MyQuickFillService.
    // For testing purposes, we can mock ApplicationInfo.
    
    private val service = MyQuickFillService()

    @Test
    fun detectCategory_SocialApps() {
        val info = mockk<ApplicationInfo>()
        every { info.category } returns ApplicationInfo.CATEGORY_UNDEFINED
        
        assertEquals("SOCIAL", service.detectCategory(info, "com.whatsapp"))
        assertEquals("SOCIAL", service.detectCategory(info, "com.facebook.orca")) // messenger
    }

    @Test
    fun detectCategory_SystemCategories() {
        val info = mockk<ApplicationInfo>()
        
        every { info.category } returns ApplicationInfo.CATEGORY_SOCIAL
        assertEquals("SOCIAL", service.detectCategory(info, "any.package"))
        
        every { info.category } returns ApplicationInfo.CATEGORY_MAPS
        assertEquals("MAPS", service.detectCategory(info, "any.package"))
        
        every { info.category } returns ApplicationInfo.CATEGORY_PRODUCTIVITY
        assertEquals("WORK", service.detectCategory(info, "any.package"))
    }

    @Test
    fun detectCategory_FinanceApps() {
        val info = mockk<ApplicationInfo>()
        every { info.category } returns ApplicationInfo.CATEGORY_UNDEFINED
        
        assertEquals("FINANCE", service.detectCategory(info, "com.some.bank.app"))
        assertEquals("FINANCE", service.detectCategory(info, "my.wallet.app"))
    }

    @Test
    fun detectCategory_GeneralFallback() {
        val info = mockk<ApplicationInfo>()
        every { info.category } returns ApplicationInfo.CATEGORY_UNDEFINED
        
        assertEquals("GENERAL", service.detectCategory(info, "com.example.unknown"))
    }
}
