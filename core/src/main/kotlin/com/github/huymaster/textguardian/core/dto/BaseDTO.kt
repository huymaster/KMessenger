package com.github.huymaster.textguardian.core.dto

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.huymaster.textguardian.core.adapter.BaseDTOAdapter
import com.github.huymaster.textguardian.core.entity.BaseEntity

@JsonSerialize(using = BaseDTOAdapter.BaseDTOSerializer::class)
@JsonDeserialize(using = BaseDTOAdapter.BaseDTODeserializer::class)
interface BaseDTO<E : BaseEntity<E>> {
    fun write(output: ObjectNode)
    fun read(input: JsonNode)

    fun toEntity(): E
    fun toDTO(entity: E): BaseDTO<E>
    fun mergeTo(entity: E)
    fun importFrom(entity: E)
}