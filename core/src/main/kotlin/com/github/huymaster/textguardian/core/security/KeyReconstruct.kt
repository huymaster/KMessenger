package com.github.huymaster.textguardian.core.security

import com.github.huymaster.textguardian.core.api.type.UserPublicKey
import com.github.huymaster.textguardian.core.utils.KEY_PAIR_ALGORITHM
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

object KeyReconstruct {
    private val factory = KeyFactory.getInstance(KEY_PAIR_ALGORITHM)

    fun reconstructPublicKey(bytes: ByteArray): PublicKey {
        val specs = X509EncodedKeySpec(bytes)
        return factory.generatePublic(specs)
    }

    fun reconstructPublicKey(bytes: Array<Byte>): PublicKey =
        reconstructPublicKey(bytes.toByteArray())

    fun reconstructPublicKey(publicKey: UserPublicKey) =
        reconstructPublicKey(publicKey.key)

    fun reconstructPrivateKey(bytes: ByteArray): PrivateKey {
        val specs = PKCS8EncodedKeySpec(bytes)
        return factory.generatePrivate(specs)
    }

    fun reconstructPrivateKey(bytes: Array<Byte>): PrivateKey =
        reconstructPrivateKey(bytes.toByteArray())
}