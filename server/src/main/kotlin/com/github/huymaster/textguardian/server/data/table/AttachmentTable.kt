package com.github.huymaster.textguardian.server.data.table

import com.github.huymaster.textguardian.core.entity.AttachmentEntity
import org.ktorm.schema.long
import org.ktorm.schema.uuid
import org.ktorm.schema.varchar

object AttachmentTable : BaseTable<AttachmentEntity>("attachment") {
    val attachmentId = uuid("attachment_id").primaryKey()
        .bindTo { it.attachmentId }
    val mimeType = varchar("file_mime")
        .bindTo { it.mimeType }
    val fileSize = long("file_size")
        .bindTo { it.fileSize }
}