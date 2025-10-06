package com.github.huymaster.textguardian.core.utils

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Security
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

const val KEY_PAIR_ALGORITHM = "KYBER"
const val SECRET_KEY_ALGORITHM = "KYBER"
const val SECRET_KEY_SIZE = 256

fun addPQCProvider() {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null)
        Security.addProvider(BouncyCastleProvider())
    if (Security.getProvider(BouncyCastlePQCProvider.PROVIDER_NAME) == null)
        Security.addProvider(BouncyCastlePQCProvider())
}

fun loadPublicKey(input: InputStream): PublicKey? {
    val bytes = input.readBytes()
    val spec = X509EncodedKeySpec(bytes)
    val factory = KeyFactory.getInstance(KEY_PAIR_ALGORITHM)
    return runCatching { factory.generatePublic(spec) }.getOrNull()
}

fun savePublicKey(publicKey: PublicKey, output: OutputStream) {
    val bytes = publicKey.encoded
    output.write(bytes)
}

fun loadPrivateKey(input: InputStream): PrivateKey? {
    val bytes = input.readBytes()
    val spec = PKCS8EncodedKeySpec(bytes)
    val factory = KeyFactory.getInstance(KEY_PAIR_ALGORITHM)
    return runCatching { factory.generatePrivate(spec) }.getOrNull()
}

fun savePrivateKey(privateKey: PrivateKey, output: OutputStream) {
    val bytes = privateKey.encoded
    output.write(bytes)
}