package com.github.huymaster.textguardian.core.dto

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.huymaster.textguardian.core.entity.BaseEntity

interface BaseDTO<E : BaseEntity<E>> {
    fun write(output: ObjectNode)
    fun read(input: JsonNode)

    fun toEntity(): E
    fun toDTO(entity: E): BaseDTO<E>
    fun mergeTo(entity: E)
    fun importFrom(entity: E)
}