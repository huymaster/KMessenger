package com.github.huymaster.textguardian.core.type

import com.google.gson.JsonElement
import org.ktorm.entity.Entity

interface Convertable<T : Convertable<T>> : Entity<T> {
    fun read(obj: JsonElement)
    fun write(): JsonElement
}