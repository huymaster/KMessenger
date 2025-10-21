package com.github.huymaster.textguardian.server.data.repository

import com.github.huymaster.textguardian.core.entity.PublicKeyEntity
import com.github.huymaster.textguardian.server.data.table.PublicKeyTable

class PublicKeyRepository : BaseRepository<PublicKeyEntity, PublicKeyTable>(PublicKeyTable) {
}