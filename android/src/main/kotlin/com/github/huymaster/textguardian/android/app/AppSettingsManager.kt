package com.github.huymaster.textguardian.android.app

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class AppSettingsManager private constructor(context: Context) {
    companion object : KoinComponent {
        private const val TAG = "AppSettingsManager"
        val INSTANCE by lazy { AppSettingsManager(get()) }
    }

    sealed class Settings<T : Any>(val key: String, val defaultValue: T) : MutableState<T> {
        object FINGERPRINT_ENABLED : Settings<Boolean>("fingerprint_enabled", false)
        object THEME : Settings<String>("theme", "System") {
            const val LIGHT = "Light"
            const val DARK = "Dark"
            const val SYSTEM = "System"
            val VALUES = listOf(LIGHT, DARK, SYSTEM)
        }

        object FCM_TOKEN : Settings<String>("fcm_token", "")

        private val state: MutableState<T> = mutableStateOf(INSTANCE.get(this))
        override var value: T
            get() = state.value
            set(value) {
                if (state.value != value) state.value = value
                INSTANCE.setNoNotify(this, state.value)
            }

        override fun component1(): T = value

        override fun component2(): (T) -> Unit = { value = it }

        override fun toString(): String {
            return "Settings(key=$key, defaultValue=$defaultValue)"
        }
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

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
        setNoNotify(settings, value)
        settings.value = value ?: settings.defaultValue
    }

    private fun <T : Any> setNoNotify(settings: Settings<T>, value: T?) {
        if (value == null) return remove(settings)
        if (value == get(settings)) return
        try {
            sharedPreferences.edit {
                when (value) {
                    is Boolean -> putBoolean(settings.key, value)
                    is Int -> putInt(settings.key, value)
                    is Long -> putLong(settings.key, value)
                    is Float -> putFloat(settings.key, value)
                    is Double -> putLong(settings.key, value.toRawBits())
                    is String -> putString(settings.key, value)
                    else -> throw IllegalArgumentException("Unsupported type: ${value::class.java}")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to set value for key: ${settings.key}", e)
        }
    }

    fun <T : Any> remove(settings: Settings<T>) {
        sharedPreferences.edit {
            remove(settings.key)
            settings.value = settings.defaultValue
        }
    }

    fun <T : Any> exists(settings: Settings<T>) = sharedPreferences.contains(settings.key)
}