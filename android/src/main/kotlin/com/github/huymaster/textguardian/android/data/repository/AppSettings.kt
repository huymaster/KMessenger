package com.github.huymaster.textguardian.android.data.repository

object AppSettings {
    val REFRESH_TOKEN = AppSettingsManager.Settings<String>("refresh_token", "")
    val ACCESS_TOKEN = AppSettingsManager.Settings<String>("access_token", "")
}