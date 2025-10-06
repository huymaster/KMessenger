package com.github.huymaster.textguardian.core.security

import com.github.huymaster.textguardian.core.utils.SECRET_KEY_ALGORITHM
import com.github.huymaster.textguardian.core.utils.SECRET_KEY_SIZE
import org.bouncycastle.jcajce.SecretKeyWithEncapsulation
import org.bouncycastle.jcajce.spec.KEMExtractSpec
import org.bouncycastle.jcajce.spec.KEMGenerateSpec
import org.koin.core.component.KoinComponent
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

object KeyEncapsulation : KoinComponent {
    data class Encapsulated(val key: SecretKey, val encapsulation: ByteArray)

    fun encapsulate(publicKey: PublicKey): Encapsulated {
        val generator = KeyGenerator.getInstance(SECRET_KEY_ALGORITHM)
        generator.init(KEMGenerateSpec(publicKey, "AES", SECRET_KEY_SIZE))
        val secretKey = generator.generateKey() as SecretKeyWithEncapsulation
        return Encapsulated(secretKey, secretKey.encapsulation)
    }

    fun decapsulate(privateKey: PrivateKey, encapsulation: ByteArray): SecretKey {
        val generator = KeyGenerator.getInstance(SECRET_KEY_ALGORITHM)
        generator.init(KEMExtractSpec(privateKey, encapsulation, "AES", SECRET_KEY_SIZE))
        return generator.generateKey()
    }
}