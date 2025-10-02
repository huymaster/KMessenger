package com.github.huymaster.textguardian.android.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.github.huymaster.textguardian.android.data.security.CryptoManager
import org.koin.java.KoinJavaComponent.inject

class AppSettingsManager(context: Context) {
    companion object {
        private const val TAG = "AppSettingsManager"
    }

    data class Settings<T : Any>(val key: String, val defaultValue: T)

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor get() = sharedPreferences.edit()
    private val cryptoManager by inject<CryptoManager>(CryptoManager::class.java)

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(settings: Settings<T>): T {
        if (!exists(settings)) return settings.defaultValue
        try {
            val encryptedKey = cryptoManager.encrypt(settings.key)
            val value = when (settings.defaultValue) {
                is Boolean -> sharedPreferences.getBoolean(encryptedKey, settings.defaultValue)
                is Int -> sharedPreferences.getInt(encryptedKey, settings.defaultValue)
                is Long -> sharedPreferences.getLong(encryptedKey, settings.defaultValue)
                is Float -> sharedPreferences.getFloat(encryptedKey, settings.defaultValue)
                is Double -> sharedPreferences.getLong(encryptedKey, settings.defaultValue.toRawBits()).toDouble()
                is String -> cryptoManager.decrypt(
                    sharedPreferences.getString(encryptedKey, settings.defaultValue) ?: ""
                )

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
            val encryptedKey = cryptoManager.encrypt(settings.key)
            when (value) {
                is Boolean -> editor.putBoolean(encryptedKey, value).apply()
                is Int -> editor.putInt(encryptedKey, value).apply()
                is Long -> editor.putLong(encryptedKey, value).apply()
                is Float -> editor.putFloat(encryptedKey, value).apply()
                is Double -> editor.putLong(encryptedKey, value.toRawBits()).apply()
                is String -> editor.putString(encryptedKey, cryptoManager.encrypt(value)).apply()
                else -> throw IllegalArgumentException("Unsupported type: ${value::class.java}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to set value for key: ${settings.key}", e)
        }
    }

    fun <T : Any> remove(settings: Settings<T>) {
        editor.remove(settings.key).apply()
    }

    fun <T : Any> exists(settings: Settings<T>) = sharedPreferences.contains(cryptoManager.encrypt(settings.key))

    fun clear() = editor.clear().apply()
}