package com.github.huymaster.textguardian.server.data.repository

import com.github.huymaster.textguardian.core.entity.CredentialEntity
import com.github.huymaster.textguardian.server.data.table.CredentialTable

class CredentialRepository() : BaseRepository<CredentialEntity, CredentialTable>(CredentialTable) {
}