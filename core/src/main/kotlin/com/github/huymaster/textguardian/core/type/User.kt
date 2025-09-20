package com.github.huymaster.textguardian.core.type

import java.util.*

interface User : Convertable<User> {
    companion object {
        const val ID_FIELD = "userId"
        const val PHONE_NUMBER_FIELD = "phoneNumber"
        const val USERNAME_FIELD = "username"
        const val DISPLAY_NAME_FIELD = "displayName"
        const val LAST_SEEN_FIELD = "lastSeen"
        const val CREATED_AT_FIELD = "createdAt"
        const val AVATAR_URL_FIELD = "avatarUrl"
        const val BIO_FIELD = "bio"
    }

    var id: UUID
    var phoneNumber: String
    var username: String?
    var displayName: String?
    var lastSeen: Long
    var createdAt: Long
    var avatarUrl: String?
    var bio: String?
}