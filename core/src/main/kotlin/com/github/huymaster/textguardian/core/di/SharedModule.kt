package com.github.huymaster.textguardian.core.di

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.huymaster.textguardian.core.api.APIBase
import com.github.huymaster.textguardian.core.api.APIVersion1Service
import com.github.huymaster.textguardian.core.utils.*
import org.bouncycastle.pqc.jcajce.spec.KyberParameterSpec
import org.koin.dsl.module
import java.security.KeyPair
import java.security.KeyPairGenerator
import javax.crypto.KEM

object SharedModule {
    init {
        addPQCProvider()
    }

    val objectMapper = module { single<ObjectMapper> { DEFAULT_OBJECT_MAPPER } }
    val security = module {
        single<KyberParameterSpec> { KyberParameterSpec.kyber1024 }
        single<KeyPairGenerator> { newKeyPairGenerator(get()) }
        single<KEM> { KEM.getInstance(ALGORITHM) }
        factory<KeyPair> { generateNewKeyPair(get()) }
    }
    val api = module {
        factory { createService(APIBase::class) }
        factory { createService(APIVersion1Service::class) }
    }
}