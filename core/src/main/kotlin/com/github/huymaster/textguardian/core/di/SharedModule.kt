package com.github.huymaster.textguardian.core.di

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.huymaster.textguardian.core.api.APIVersion1Service
import com.github.huymaster.textguardian.core.utils.DEFAULT_OBJECT_MAPPER
import com.github.huymaster.textguardian.core.utils.KEY_PAIR_ALGORITHM
import com.github.huymaster.textguardian.core.utils.addPQCProvider
import com.github.huymaster.textguardian.core.utils.createService
import org.bouncycastle.pqc.jcajce.spec.KyberParameterSpec
import org.koin.dsl.module
import java.security.KeyPair
import java.security.KeyPairGenerator

object SharedModule {
    init {
        addPQCProvider()
    }

    val objectMapper = module { single<ObjectMapper> { DEFAULT_OBJECT_MAPPER } }
    val security = module {
        single<KeyPairGenerator> {
            KeyPairGenerator.getInstance(KEY_PAIR_ALGORITHM).apply {
                initialize(KyberParameterSpec.kyber1024)
            }
        }
        factory<KeyPair> { get<KeyPairGenerator>().generateKeyPair() }
    }
    val api = module {
        factory { createService(APIVersion1Service::class) }
    }
}