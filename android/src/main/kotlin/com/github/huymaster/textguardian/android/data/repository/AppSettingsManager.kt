package com.github.huymaster.textguardian.android.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class AppSettingsManager(context: Context) {
    companion object {
        private const val TAG = "AppSettingsManager"
    }

    data class Settings<T : Any>(val key: String, val defaultValue: T)

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor get() = sharedPreferences.edit()

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(settings: Settings<T>): T {
        if (!exists(settings)) return settings.defaultValue
        try {
            val value = when (settings.defaultValue) {
                is Boolean -> sharedPreferences.getBoolean(settings.key, settings.defaultValue)
                is Int -> sharedPreferences.getInt(settings.key, settings.defaultValue)
                is Long -> sharedPreferences.getLong(settings.key, settings.defaultValue)
                is Float -> sharedPreferences.getFloat(settings.key, settings.defaultValue)
                is Double -> sharedPreferences.getLong(settings.key, settings.defaultValue.toRawBits()).toDouble()
                is String -> sharedPreferences.getString(settings.key, settings.defaultValue)

                else -> throw IllegalArgumentException("Unsupported type: ${settings.defaultValue::class.java}")
            } as? T
            return value ?: settings.defaultValue
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get value for key: ${settings.key}", e)
            return settings.defaultValue
        }
    }

    fun <T : Any> set(settings: Settings<T>, value: T?) {
        if (value == null) return remove(settings)
        if (value == settings.defaultValue) return
        try {
            when (value) {
                is Boolean -> editor.putBoolean(settings.key, value).apply()
                is Int -> editor.putInt(settings.key, value).apply()
                is Long -> editor.putLong(settings.key, value).apply()
                is Float -> editor.putFloat(settings.key, value).apply()
                is Double -> editor.putLong(settings.key, value.toRawBits()).apply()
                is String -> editor.putString(settings.key, value).apply()
                else -> throw IllegalArgumentException("Unsupported type: ${value::class.java}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to set value for key: ${settings.key}", e)
        }
    }

    fun <T : Any> remove(settings: Settings<T>) {
        editor.remove(settings.key).apply()
    }

    fun <T : Any> exists(settings: Settings<T>) = sharedPreferences.contains(settings.key)

    fun clear() = editor.clear().apply()
}