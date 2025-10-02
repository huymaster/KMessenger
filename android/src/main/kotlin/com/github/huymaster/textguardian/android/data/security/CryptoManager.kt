package com.github.huymaster.textguardian.android.data.security

import android.content.Context
import android.util.Base64
import android.util.Log
import com.google.crypto.tink.Aead
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager

class CryptoManager(context: Context) {
    companion object {
        private const val TAG = "CryptoManager"
    }

    private val keyName = "androidMasterKey"
    private val prefName = "encryptedPrefs"
    private val masterKeyUri = "android-keystore://$keyName"
    private val aead: Aead

    init {
        AeadConfig.register()
        aead = AndroidKeysetManager.Builder()
            .build()
            .keysetHandle
            .getPrimitive(RegistryConfiguration.get(), Aead::class.java)
    }

    fun encrypt(data: String, associatedData: ByteArray? = null): String {
        try {
            val bytes = data.toByteArray(Charsets.UTF_8)
            val encryptedData = aead.encrypt(bytes, associatedData)
            val encodedData = base64Encode(encryptedData)
            return encodedData
        } catch (e: Exception) {
            Log.w(TAG, "Failed to encrypt data", e)
            return data
        }
    }

    private fun base64Encode(data: ByteArray): String {
        return Base64.encodeToString(data, Base64.URL_SAFE)
    }

    fun decrypt(data: String, associatedData: ByteArray? = null): String {
        try {
            val decodedData = base64Decode(data)
            val decryptedBytes = aead.decrypt(decodedData, associatedData)
            val string = String(decryptedBytes, Charsets.UTF_8)
            return string
        } catch (e: Exception) {
            Log.w(TAG, "Failed to decrypt data", e)
            return data
        }
    }

    private fun base64Decode(data: String): ByteArray {
        return Base64.decode(data, Base64.URL_SAFE)
    }
}