package com.github.huymaster.textguardian.core.api.type

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.github.huymaster.textguardian.core.security.KeyReconstruct
import org.koin.java.KoinJavaComponent.get
import java.security.PublicKey
import java.util.*

data class UserPublicKey(
    @get:JsonProperty("userId") val userId: UUID,
    @get:JsonProperty("key") val key: String
) {
    companion object {
        private val encoder = get<Base64.Encoder>(Base64.Encoder::class.java)
        private val decoder = get<Base64.Decoder>(Base64.Decoder::class.java)
    }

    init {
        require(key.isNotBlank()) { "Public key must not empty" }
        require(runCatching { decoder.decode(key) }.isSuccess) { "Invalid public key" }
        require(
            runCatching {
                KeyReconstruct.reconstructPublicKey(decoder.decode(key))
            }.isSuccess
        )
        { "Invalid key algorithm" }
    }

    @JsonIgnore
    val bytes: ByteArray = decoder.decode(key)

    @JsonIgnore
    val public: PublicKey = KeyReconstruct.reconstructPublicKey(bytes)

    constructor(userId: UUID, key: ByteArray) : this(userId, encoder.encodeToString(key))
    constructor(userId: UUID, key: Array<Byte>) : this(userId, key.toByteArray())
    constructor(userId: UUID, key: Collection<Byte>) : this(userId, key.toByteArray())
}