package com.hp.aiitvideo

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val sharedPref = getSharedPreferences("VideoAppPrefs", Context.MODE_PRIVATE)

        // 1. Khôi phục trạng thái Dark Mode
        val isDarkMode = sharedPref.getBoolean("DARK_MODE", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // 2. Khôi phục Ngôn ngữ (Mặc định là Tiếng Việt 'vi')
        val langCode = sharedPref.getString("LANGUAGE", "vi") ?: "vi"
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(langCode))
    }
}