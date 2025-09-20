package com.github.huymaster.textguardian.core.converter

import com.github.huymaster.textguardian.core.type.Convertable
import com.google.gson.JsonObject
import org.ktorm.entity.Entity
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

@Suppress("UNCHECKED_CAST")
class EntityConverterFactory<T : Convertable<T>>(
    private val target: T,
    private val converter: EntityConverter<T>
) : InvocationHandler {
    companion object {
        inline fun <reified T : Convertable<T>> create(
            converter: EntityConverter<T>
        ): T {
            val entity = Entity.create<T>()
            val handler = EntityConverterFactory(entity, converter)
            return Proxy.newProxyInstance(
                T::class.java.classLoader,
                arrayOf(T::class.java),
                handler
            ) as T
        }
    }

    override fun invoke(
        proxy: Any,
        method: Method,
        args: Array<out Any?>?
    ): Any? {
        return when (method.name) {
            "write" -> writeJson()
            "read" -> {
                if (args == null)
                    throw IllegalArgumentException("Missing arguments")
                if (args.size != 1)
                    throw IllegalArgumentException("Invalid arguments")
                readJson(args[0] as JsonObject)
            }

            else -> defaultHandler(method, args)
        }
    }

    private fun readJson(obj: JsonObject): T {
        return converter.read(obj, target)
    }

    private fun writeJson(): JsonObject {
        return converter.write(target, JsonObject())
    }

    private fun defaultHandler(method: Method, args: Array<out Any?>?): Any? {
        return if (args == null)
            method.invoke(target)
        else
            method.invoke(target, *args)
    }
}