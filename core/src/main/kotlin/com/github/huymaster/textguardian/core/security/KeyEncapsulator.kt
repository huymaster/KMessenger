package com.github.huymaster.textguardian.core.security

import com.github.huymaster.textguardian.core.utils.SECRET_KEY_ALGORITHM
import com.github.huymaster.textguardian.core.utils.SECRET_KEY_SIZE_BITS
import org.bouncycastle.jcajce.SecretKeyWithEncapsulation
import org.bouncycastle.jcajce.spec.KEMExtractSpec
import org.bouncycastle.jcajce.spec.KEMGenerateSpec
import org.koin.core.component.KoinComponent
import java.nio.ByteBuffer
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object KeyEncapsulator : KoinComponent {
    private val cipher: Cipher
        @Suppress("GetInstance")
        get() = Cipher.getInstance("AES")

    fun encapsulate(publicKey: PublicKey, secretKey: SecretKey): ByteArray {
        val generator = KeyGenerator.getInstance(SECRET_KEY_ALGORITHM)
        generator.init(KEMGenerateSpec(publicKey, "AES", SECRET_KEY_SIZE_BITS))
        val lock = generator.generateKey() as SecretKeyWithEncapsulation
        val encrypted = cipher.apply { init(Cipher.ENCRYPT_MODE, lock) }.doFinal(secretKey.encoded)

        val buffer = ByteBuffer.allocate(Int.SIZE_BYTES * 2 + encrypted.size + lock.encapsulation.size)
        buffer.putInt(encrypted.size)
        buffer.putInt(lock.encapsulation.size)
        buffer.put(encrypted)
        buffer.put(lock.encapsulation)

        return buffer.array()
    }

    fun decapsulate(privateKey: PrivateKey, bytes: ByteArray): SecretKey {
        val buffer = ByteBuffer.wrap(bytes)
        val encryptedSize = buffer.getInt()
        val encapsulationSize = buffer.getInt()

        val encrypted = ByteArray(encryptedSize)
        val encapsulation = ByteArray(encapsulationSize)

        buffer.get(encrypted)
        buffer.get(encapsulation)

        val generator = KeyGenerator.getInstance(SECRET_KEY_ALGORITHM)
        generator.init(KEMExtractSpec(privateKey, encapsulation, "AES", SECRET_KEY_SIZE_BITS))
        val lock = generator.generateKey()

        val secretBytes = cipher.apply { init(Cipher.DECRYPT_MODE, lock) }.doFinal(encrypted)
        val secretKey = SecretKeySpec(secretBytes, SECRET_KEY_ALGORITHM)

        return secretKey
    }
}