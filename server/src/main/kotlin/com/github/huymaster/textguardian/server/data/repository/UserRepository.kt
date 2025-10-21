package com.github.huymaster.textguardian.server.data.repository

import com.github.huymaster.textguardian.core.entity.UserEntity
import com.github.huymaster.textguardian.server.data.table.UserTable

class UserRepository() : BaseRepository<UserEntity, UserTable>(UserTable) {
}