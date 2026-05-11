package com.byteutility.dev.quickfill.service

import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Logic for detecting app category based on system info or package name.
 * Isolated from Service for testability.
 */
object CategoryDetector {
    
    // Constant values from ApplicationInfo to avoid MockK dependency on final properties
    const val CAT_SOCIAL = 4
    const val CAT_MAPS = 5
    const val CAT_PRODUCTIVITY = 1
    const val CAT_GAME = 0
    const val CAT_AUDIO = 2
    const val CAT_VIDEO = 3
    const val CAT_IMAGE = 6
    const val CAT_NEWS = 7
    const val CAT_UNDEFINED = -1

    @RequiresApi(Build.VERSION_CODES.O)
    fun detectCategory(category: Int, packageName: String): String {
        val catString = when (category) {
            CAT_SOCIAL -> "SOCIAL"
            CAT_MAPS -> "MAPS"
            CAT_PRODUCTIVITY -> "WORK"
            CAT_GAME -> "GAME"
            CAT_AUDIO -> "AUDIO"
            CAT_VIDEO -> "VIDEO"
            CAT_IMAGE -> "IMAGE"
            CAT_NEWS -> "NEWS"
            else -> null
        }
        
        if (catString != null) return catString

        val lowerPackage = packageName.lowercase()
        return when {
            lowerPackage.contains("whatsapp") || 
            lowerPackage.contains("messenger") || 
            lowerPackage.contains("facebook.orca") -> "SOCIAL"
            lowerPackage.contains("amazon") || lowerPackage.contains("ebay") -> "SHOPPING"
            lowerPackage.contains("bank") || lowerPackage.contains("wallet") -> "FINANCE"
            else -> "GENERAL"
        }
    }
}
