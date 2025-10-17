package com.github.huymaster.textguardian.core.adapter

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.Instant

object InstantAdapter : SimpleModule() {
    private const val EPOCH_MILLIS_FIELD = "epochMillis"
    private fun readResolve(): Any = InstantAdapter

    init {
        addSerializer(Instant::class.java, InstantSerializer())
        addDeserializer(Instant::class.java, InstantDeserializer())
    }

    class InstantSerializer : JsonSerializer<Instant>() {
        override fun serialize(value: Instant, gen: JsonGenerator, provider: SerializerProvider?) {
            gen.writeNumber(value.toEpochMilli())
        }
    }

    class InstantDeserializer : JsonDeserializer<Instant>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Instant {
            return Instant.ofEpochMilli(p.valueAsLong)
        }
    }
}