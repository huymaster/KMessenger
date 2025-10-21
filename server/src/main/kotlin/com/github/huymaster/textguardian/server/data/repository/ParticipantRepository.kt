package com.github.huymaster.textguardian.server.data.repository

import com.github.huymaster.textguardian.core.entity.ParticipantEntity
import com.github.huymaster.textguardian.server.data.table.ParticipantTable

class ParticipantRepository : BaseRepository<ParticipantEntity, ParticipantTable>(ParticipantTable) {
}