package com.github.huymaster.textguardian.core.converter

import com.github.huymaster.textguardian.core.type.User
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.util.*

class UserConverter : EntityConverterImpl<User>() {
    override fun write(entity: User, element: JsonElement): JsonObject {
        val obj: JsonObject = run { element.asJsonObject }
        return obj.apply {
            addProperty(User.ID_FIELD, entity.id.toString())
            addProperty(User.PHONE_NUMBER_FIELD, entity.phoneNumber)
            add(User.USERNAME_FIELD, if (entity.username == null) JsonNull.INSTANCE else JsonPrimitive(entity.username))
            add(
                User.DISPLAY_NAME_FIELD,
                if (entity.displayName == null) JsonNull.INSTANCE else JsonPrimitive(entity.displayName)
            )
            addProperty(User.LAST_SEEN_FIELD, entity.lastSeen)
            addProperty(User.CREATED_AT_FIELD, entity.createdAt)
            addProperty(User.AVATAR_URL_FIELD, entity.avatarUrl)
            addProperty(User.BIO_FIELD, entity.bio)
        }
    }

    override fun read(element: JsonElement, entity: User): User {
        val obj: JsonObject = run { element.asJsonObject }
        return entity.apply {
            id = UUID.fromString(obj.getOrThrow(User.ID_FIELD, "id field required").asString)
            phoneNumber = obj.getOrThrow(User.PHONE_NUMBER_FIELD, "phone number field required").asString
            username = obj.getOrNull(User.USERNAME_FIELD)?.asString
            displayName = obj.getOrNull(User.DISPLAY_NAME_FIELD)?.asString
            lastSeen = obj.getOrDefault(User.LAST_SEEN_FIELD, JsonPrimitive(0L)).asLong
            createdAt = obj.getOrDefault(User.CREATED_AT_FIELD, JsonPrimitive(0L)).asLong
            avatarUrl = obj.getOrNull(User.AVATAR_URL_FIELD)?.asString
            bio = obj.getOrNull(User.BIO_FIELD)?.asString
        }
    }
}