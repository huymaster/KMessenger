package com.github.huymaster.textguardian.android.app

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class CryptoManager private constructor() {
    companion object {
        private const val ANDROID_KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "kmessenger_auth_key"
        private const val AES_GCM_NO_PADDING =
            "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_GCM}/${KeyProperties.ENCRYPTION_PADDING_NONE}"
        private const val TAG_SIZE_BITS = 128
        private const val IV_SIZE_BYTES = 12

        val INSTANCE = CryptoManager()
    }

    data class EncryptedData(
        val iv: ByteArray,
        val encryptedData: ByteArray
    ) {
        // Chỉ để tiện hiển thị trên UI
        override fun toString(): String {
            return "IV: ${Base64.encodeToString(iv, Base64.DEFAULT)}\nData: ${
                Base64.encodeToString(encryptedData, Base64.DEFAULT)
            }"
        }
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE_PROVIDER)
        keyStore.load(null)
        keyStore.getKey(KEY_ALIAS, null)?.let {
            return it as SecretKey
        }

        val keyGenSpecBuilder =
            KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(true)
                .setInvalidatedByBiometricEnrollment(true)

        val keyGenSpec = keyGenSpecBuilder.build()

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE_PROVIDER)
        keyGenerator.init(keyGenSpec)
        return keyGenerator.generateKey()
    }

    fun encryptData(data: String): EncryptedData {
        val key = getOrCreateKey()
        val iv = ByteArray(IV_SIZE_BYTES)
        val random = SecureRandom()
        random.nextBytes(iv)

        val cipher = Cipher.getInstance(AES_GCM_NO_PADDING)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_SIZE_BITS, iv))

        val encryptedData = cipher.doFinal(data.toByteArray())
        return EncryptedData(iv, encryptedData)
    }

    fun getDecryptCipher(encryptedData: EncryptedData): Cipher {
        val key = getOrCreateKey()
        val cipher = Cipher.getInstance(AES_GCM_NO_PADDING)
        val spec = GCMParameterSpec(TAG_SIZE_BITS, encryptedData.iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        return cipher
    }

    fun decryptData(encryptedData: EncryptedData): String {
        val cipher = getDecryptCipher(encryptedData)
        val decryptedData = cipher.doFinal(encryptedData.encryptedData)
        return String(decryptedData)
    }
}