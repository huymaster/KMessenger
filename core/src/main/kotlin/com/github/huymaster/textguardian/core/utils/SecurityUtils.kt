package com.github.huymaster.textguardian.core.utils

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider
import java.security.Security

const val KEY_PAIR_ALGORITHM = "KYBER"
const val SECRET_KEY_ALGORITHM = "KYBER"
const val SECRET_KEY_SIZE_BITS = 256

fun addPQCProvider() {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null)
        Security.addProvider(BouncyCastleProvider())
    if (Security.getProvider(BouncyCastlePQCProvider.PROVIDER_NAME) == null)
        Security.addProvider(BouncyCastlePQCProvider())
}