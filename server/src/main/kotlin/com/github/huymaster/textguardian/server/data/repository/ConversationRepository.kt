package com.github.huymaster.textguardian.server.data.repository

import com.github.huymaster.textguardian.core.entity.ConversationEntity
import com.github.huymaster.textguardian.server.data.table.ConversationTable

class ConversationRepository : BaseRepository<ConversationEntity, ConversationTable>(ConversationTable) {
}