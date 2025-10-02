package com.github.huymaster.textguardian.core.utils

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider
import org.bouncycastle.pqc.jcajce.spec.KyberParameterSpec
import java.io.InputStream
import java.io.OutputStream
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

const val PROVIDER = "BC"
const val ALGORITHM = "ML-KEM"

fun addPQCProvider() {
    Security.addProvider(BouncyCastleProvider())
    Security.addProvider(BouncyCastlePQCProvider())
}

fun newKeyPairGenerator(specs: KyberParameterSpec): KeyPairGenerator {
    return KeyPairGenerator.getInstance(ALGORITHM).apply { initialize(specs) }
}

fun generateNewKeyPair(keyPairGenerator: KeyPairGenerator): KeyPair = keyPairGenerator.generateKeyPair()

fun loadPublicKey(input: InputStream): PublicKey? {
    val bytes = input.readBytes()
    val spec = X509EncodedKeySpec(bytes)
    val factory = KeyFactory.getInstance(ALGORITHM, PROVIDER)
    return runCatching { factory.generatePublic(spec) }.getOrNull()
}

fun savePublicKey(publicKey: PublicKey, output: OutputStream) {
    val bytes = publicKey.encoded
    output.write(bytes)
}

fun loadPrivateKey(input: InputStream): PrivateKey? {
    val bytes = input.readBytes()
    val spec = PKCS8EncodedKeySpec(bytes)
    val factory = KeyFactory.getInstance(ALGORITHM, PROVIDER)
    return runCatching { factory.generatePrivate(spec) }.getOrNull()
}

fun savePrivateKey(privateKey: PrivateKey, output: OutputStream) {
    val bytes = privateKey.encoded
    output.write(bytes)
}