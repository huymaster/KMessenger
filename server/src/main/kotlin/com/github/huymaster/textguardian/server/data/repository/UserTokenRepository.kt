package com.github.huymaster.textguardian.server.data.repository

import com.github.huymaster.textguardian.core.entity.UserTokenEntity
import com.github.huymaster.textguardian.server.data.table.UserTokenTable

class UserTokenRepository() : BaseRepository<UserTokenEntity, UserTokenTable>(UserTokenTable) {

}