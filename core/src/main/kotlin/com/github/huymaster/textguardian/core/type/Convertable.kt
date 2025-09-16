package com.github.huymaster.textguardian.core.type

import com.google.gson.JsonObject

sealed interface Convertable<T : Convertable<T>> {
    fun write(): JsonObject
    fun read(obj: JsonObject)
}