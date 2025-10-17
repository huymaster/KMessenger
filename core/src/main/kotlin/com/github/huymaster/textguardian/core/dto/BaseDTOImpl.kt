package com.github.huymaster.textguardian.core.dto

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.*
import com.github.huymaster.textguardian.core.entity.BaseEntity
import java.lang.reflect.Constructor
import java.lang.reflect.Modifier
import kotlin.reflect.KClass

abstract class BaseDTOImpl<E : BaseEntity<E>>(
    entity: E? = null
) : BaseDTO<E> {
    companion object DTOUtils {
        private val cache = mutableListOf<Class<out BaseDTO<*>>>()

        init {
            cache.add(UserDTO::class.java)
            cache.add(CredentialDTO::class.java)
        }

        fun <D : BaseDTO<*>> getInstance(clazz: KClass<out D>): D =
            getInstance(clazz.java)

        @Suppress("UNCHECKED_CAST")
        fun <D : BaseDTO<*>> getInstance(clazz: Class<out D>): D {
            check(clazz)
            return cache.first { it == clazz }.getConstructor().newInstance() as D
        }

        @Suppress("UNCHECKED_CAST")
        private fun <E : BaseEntity<E>, D : BaseDTO<*>> check(clazz: Class<out D>) {
            clazz as Class<out BaseDTO<E>>
            checkType(clazz)
            checkConstructor(clazz)
            if (!cache.contains(clazz))
                cache.add(clazz)
        }

        private fun <E : BaseEntity<E>> checkType(clazz: Class<out BaseDTO<E>>) {
            val modifiers = clazz.modifiers
            if (!BaseDTO::class.java.isAssignableFrom(clazz))
                throw IllegalArgumentException("Class $clazz must implement BaseDTO")
            if (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers))
                throw IllegalArgumentException("Class $clazz must not be abstract or interface")
            if (!Modifier.isPublic(modifiers))
                throw IllegalArgumentException("Class $clazz must be public")
        }

        private fun <E : BaseEntity<E>> checkConstructor(clazz: Class<out BaseDTO<E>>) {
            val constructor: Constructor<out BaseDTO<E>?> = runCatching { clazz.getDeclaredConstructor() }.getOrNull()
                ?: throw IllegalArgumentException("Class $clazz must have a default constructor")
            if (!Modifier.isPublic(constructor.modifiers))
                throw IllegalArgumentException("Non-argument constructor of class $clazz must be public")
            constructor.isAccessible = true
            if (!constructor.isAccessible)
                throw IllegalArgumentException("Non-argument constructor of class $clazz must be accessible")
        }
    }

    init {
        check(javaClass)
        if (entity != null) importFrom(entity)
    }

    protected fun JsonNode.getOrNull(member: String): JsonNode? {
        if (get(member) == null || get(member).isNull)
            return null
        return get(member)
    }

    protected fun JsonNode.getOrDefault(member: String, defaultValue: Boolean): JsonNode =
        getOrDefault(member, BooleanNode.valueOf(defaultValue))

    protected fun JsonNode.getOrDefault(member: String, defaultValue: Short = 0): JsonNode =
        getOrDefault(member, ShortNode(defaultValue))

    protected fun JsonNode.getOrDefault(member: String, defaultValue: Int = 0): JsonNode =
        getOrDefault(member, IntNode(defaultValue))

    protected fun JsonNode.getOrDefault(member: String, defaultValue: Long = 0L): JsonNode =
        getOrDefault(member, LongNode(defaultValue))

    protected fun JsonNode.getOrDefault(member: String, defaultValue: Float = 0.0f): JsonNode =
        getOrDefault(member, FloatNode(defaultValue))

    protected fun JsonNode.getOrDefault(member: String, defaultValue: Double = 0.0): JsonNode =
        getOrDefault(member, DoubleNode(defaultValue))

    protected fun JsonNode.getOrDefault(member: String, defaultValue: String = ""): JsonNode =
        getOrDefault(member, TextNode(defaultValue))

    protected fun JsonNode.getOrDefault(member: String, defaultValue: JsonNode): JsonNode =
        getOrNull(member) ?: defaultValue

    protected fun JsonNode.getOrThrow(member: String, message: String = "Member $member not found"): JsonNode =
        getOrNull(member) ?: throw IllegalArgumentException(message)
}