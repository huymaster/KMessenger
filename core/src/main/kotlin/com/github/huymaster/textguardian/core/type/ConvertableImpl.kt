package com.github.huymaster.textguardian.core.type

import com.google.gson.JsonObject
import java.lang.reflect.Constructor
import java.lang.reflect.Modifier

abstract class ConvertableImpl<T : Convertable<T>> : Convertable<T> {
    companion object {
        private fun checkConstructor(clazz: Class<*>) {
            val mod = clazz.modifiers
            if (Modifier.isAbstract(mod) || Modifier.isInterface(mod))
                throw IllegalArgumentException("Class ${clazz.name} is abstract or interface")
            if (!Modifier.isPublic(mod))
                throw IllegalArgumentException("Class ${clazz.name} is not public")
            val constructors: Array<Constructor<out Any>> = clazz.declaredConstructors
            if (constructors.isEmpty())
                throw IllegalArgumentException("Class ${clazz.name} has no constructor")
            if (constructors.none { it.parameterCount == 0 && it.canAccess(null) })
                throw IllegalArgumentException("Class ${clazz.name} has no public default constructor")
        }
    }

    init {
        precheck()
    }

    private fun precheck() {
        val clazz = this::class.java
        checkConstructor(clazz)
    }

    abstract override fun write(): JsonObject

    abstract override fun read(obj: JsonObject)
}