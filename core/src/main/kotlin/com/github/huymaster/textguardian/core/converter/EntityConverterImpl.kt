package com.github.huymaster.textguardian.core.converter

import com.github.huymaster.textguardian.core.type.Convertable
import com.google.gson.JsonElement
import com.google.gson.JsonObject

abstract class EntityConverterImpl<T : Convertable<T>> : EntityConverter<T> {

    protected fun JsonObject.getOrNull(member: String): JsonElement? {
        if (!has(member)) return null
        val value: JsonElement = get(member) ?: return null
        return if (value.isJsonNull) null else value
    }

    protected fun JsonObject.getOrDefault(member: String, defaultValue: JsonElement): JsonElement {
        return getOrNull(member) ?: defaultValue
    }

    protected fun JsonObject.getOrThrow(member: String, message: String? = null): JsonElement {
        return getOrNull(member) ?: throw IllegalArgumentException(message ?: "Missing field $member")
    }
}