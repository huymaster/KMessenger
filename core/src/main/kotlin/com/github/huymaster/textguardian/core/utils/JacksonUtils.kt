package com.github.huymaster.textguardian.core.utils

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.cfg.JsonNodeFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.github.huymaster.textguardian.core.adapter.BaseDTOAdapter
import com.github.huymaster.textguardian.core.dto.BaseDTO

fun initObjectMapper() = initObjectMapper(ObjectMapper())

fun initObjectMapper(builder: ObjectMapper): ObjectMapper = builder.init()

fun ObjectMapper.init(): ObjectMapper {
    configure(SerializationFeature.INDENT_OUTPUT, true)
    configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
    configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false)
    configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false)
    configure(SerializationFeature.FAIL_ON_ORDER_MAP_BY_INCOMPARABLE_KEY, false)

    configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
    configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
    configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    configure(JsonNodeFeature.READ_NULL_PROPERTIES, true)
    configure(JsonNodeFeature.WRITE_NULL_PROPERTIES, true)

    configure(JsonParser.Feature.ALLOW_COMMENTS, true)
    configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
    configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
    configure(JsonParser.Feature.ALLOW_MISSING_VALUES, true)

    configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true)
    configure(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS, true)
    configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
    configure(JsonGenerator.Feature.WRITE_HEX_UPPER_CASE, true)

    val module = SimpleModule()
    module.addSerializer(BaseDTO::class.java, BaseDTOAdapter.BaseDTOSerializer())
    module.addDeserializer(BaseDTO::class.java, BaseDTOAdapter.BaseDTODeserializer())
    registerModule(module)

    return this
}

val DEFAULT_OBJECT_MAPPER = initObjectMapper()