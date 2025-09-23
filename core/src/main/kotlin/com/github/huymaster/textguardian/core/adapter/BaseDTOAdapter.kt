package com.github.huymaster.textguardian.core.adapter

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.huymaster.textguardian.core.dto.BaseDTO
import com.github.huymaster.textguardian.core.dto.BaseDTOImpl
import com.github.huymaster.textguardian.core.utils.DEFAULT_OBJECT_MAPPER
import java.util.*

object BaseDTOAdapter {
    private const val IDENTIFIER_FIELD = "identifier"
    private const val DATA_FIELD = "data"
    private val classloader: ClassLoader = BaseDTOAdapter::class.java.classLoader

    private val encoder = Base64.getEncoder()
    private val decoder = Base64.getDecoder()

    private fun String.encode(): String =
        encoder.encodeToString(this.toByteArray(Charsets.UTF_8))

    private fun String.decode(): String =
        String(decoder.decode(this), Charsets.UTF_8)

    class BaseDTOSerializer : JsonSerializer<BaseDTO<*>>() {
        override fun serialize(
            value: BaseDTO<*>,
            gen: JsonGenerator,
            provider: SerializerProvider?
        ) {
            gen.writeStartObject()
            writeIdentifier(value.javaClass, gen)
            writeData(value, gen)
            gen.writeEndObject()
        }

        private fun writeIdentifier(clazz: Class<*>, gen: JsonGenerator) {
            gen.writeFieldName(IDENTIFIER_FIELD)
            val classname = classloader.loadClass(clazz.name).name
            val encodedClassname = classname.encode()
            gen.writeString(encodedClassname)
        }

        private fun writeData(value: BaseDTO<*>, gen: JsonGenerator) {
            val node = ObjectNode(JsonNodeFactory.instance)
            value.write(node)
            val nodeString = node.toString()
            val encodedNodeString = nodeString.encode()

            gen.writeFieldName(DATA_FIELD)
            gen.writeString(encodedNodeString)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class BaseDTODeserializer : JsonDeserializer<BaseDTO<*>?>() {
        override fun deserialize(
            parser: JsonParser,
            ctx: DeserializationContext?
        ): BaseDTO<*> {
            val root = parser.readValueAsTree<JsonNode>()
            val identifier = root.get(IDENTIFIER_FIELD).asText()
            val classname = identifier.decode()
            val clazz = classloader.loadClass(classname) as Class<BaseDTO<*>>
            val dto = BaseDTOImpl.getInstance(clazz)
            val nodeString = root.get(DATA_FIELD).asText()
            val decodedNodeString = nodeString.decode()
            val node = DEFAULT_OBJECT_MAPPER.readTree(decodedNodeString)
            dto.read(node)
            return dto
        }
    }
}