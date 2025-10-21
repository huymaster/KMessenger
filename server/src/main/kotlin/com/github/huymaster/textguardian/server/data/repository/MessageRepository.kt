package com.github.huymaster.textguardian.server.data.repository

import com.github.huymaster.textguardian.core.entity.MessageEntity
import com.github.huymaster.textguardian.server.data.table.MessageTable

class MessageRepository : BaseRepository<MessageEntity, MessageTable>(MessageTable) {

}