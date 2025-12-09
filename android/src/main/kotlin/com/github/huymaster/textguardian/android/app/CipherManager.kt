package com.github.huymaster.textguardian.android.app

import android.annotation.SuppressLint
import android.content.Context
import com.github.huymaster.textguardian.core.api.type.Message
import com.github.huymaster.textguardian.core.security.KeyEncapsulator
import com.github.huymaster.textguardian.core.security.KeyReconstruct
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.io.File
import java.nio.file.Files
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class CipherManager(
    private val ctx: Context
) : KoinComponent {
    private val applicationFileDir: File = ctx.filesDir
    private val privateKeyFile = File(applicationFileDir, "private.pkcs8")
    private val publicKeyFile = File(applicationFileDir, "public.der")
    private lateinit var privateKey: PrivateKey
    lateinit var publicKey: PublicKey
        private set

    init {
        if (privateKeyFile.exists() && publicKeyFile.exists()) {
            val prk = runCatching { KeyReconstruct.reconstructPrivateKey(privateKeyFile.readBytes()) }.getOrNull()
            val puk = runCatching { KeyReconstruct.reconstructPublicKey(publicKeyFile.readBytes()) }.getOrNull()
            if (prk != null && puk != null) {
                privateKey = prk
                publicKey = puk
            } else {
                generateNewKeyPair()
            }
        } else {
            generateNewKeyPair()
        }
    }

    fun generateNewKeyPair() {
        Files.deleteIfExists(privateKeyFile.toPath())
        Files.deleteIfExists(publicKeyFile.toPath())
        val keyPair = get<KeyPair>()
        privateKey = keyPair.private
        publicKey = keyPair.public

        if (!Files.exists(privateKeyFile.toPath()))
            Files.createFile(privateKeyFile.toPath())

        if (!Files.exists(publicKeyFile.toPath()))
            Files.createFile(publicKeyFile.toPath())

        privateKeyFile.writeBytes(privateKey.encoded)
        publicKeyFile.writeBytes(publicKey.encoded)
    }

    @SuppressLint("GetInstance")
    private fun getCipher() =
        Cipher.getInstance("AES/GCM/NoPadding")

    fun encapsulation(keys: Collection<PublicKey>): Pair<SecretKey, List<ByteArray>> {
        val key: SecretKey = KeyGenerator.getInstance("AES")
            .apply { init(256) }
            .generateKey()
        return Pair(key, keys.map { KeyEncapsulator.encapsulate(it, key) })
    }

    fun decapsulate(encapsulations: List<ByteArray>): SecretKey? =
        encapsulations.map { runCatching { KeyEncapsulator.decapsulate(privateKey, it) } }
            .firstOrNull { it.isSuccess }?.getOrNull()


    fun encrypt(data: String, key: SecretKey): ByteArray {
        val cipher = getCipher()
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.doFinal(data.toByteArray())
    }

    fun decrypt(data: ByteArray, key: SecretKey): String {
        val cipher = getCipher()
        cipher.init(Cipher.DECRYPT_MODE, key)
        return String(cipher.doFinal(data))
    }

    fun decrypt(data: ByteArray, keySet: Collection<ByteArray>): String {
        val key = decapsulate(keySet.toList()) ?: throw Exception("Can't decrypt data")
        return decrypt(data, key)
    }

    fun decrypt(message: Message): String =
        decrypt(message.content, message.sessionKeys.toList())
}