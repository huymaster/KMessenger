package com.github.huymaster.textguardian.core.converter

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.ktorm.entity.Entity

interface EntityConverter<T : Entity<T>> {
    fun write(entity: T, element: JsonElement): JsonObject
    fun read(element: JsonElement, entity: T): T
}