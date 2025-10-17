@file:Suppress("DEPRECATION")

package com.github.huymaster.textguardian.core.utils

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.StreamReadFeature
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.cfg.JsonNodeFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.github.huymaster.textguardian.core.adapter.BaseDTOAdapter
import com.github.huymaster.textguardian.core.adapter.InstantAdapter

fun initJsonMapper() = initJsonMapper(JsonMapper.builder())

fun initJsonMapper(builder: JsonMapper.Builder): ObjectMapper = builder.init()

fun JsonMapper.Builder.init(): ObjectMapper {
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

    configure(MapperFeature.AUTO_DETECT_FIELDS, true)
    configure(MapperFeature.AUTO_DETECT_GETTERS, true)
    configure(MapperFeature.AUTO_DETECT_IS_GETTERS, true)
    configure(MapperFeature.AUTO_DETECT_SETTERS, true)
    configure(MapperFeature.AUTO_DETECT_CREATORS, true)
    configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)

    configure(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION, true)

    val mapper = build()
    mapper.registerModule(BaseDTOAdapter)
    mapper.registerModule(InstantAdapter)
    mapper.findAndRegisterModules()

    return mapper
}

val DEFAULT_OBJECT_MAPPER = initJsonMapper()

inline fun <reified T> typeReference(): TypeReference<T> = object : TypeReference<T>() {}