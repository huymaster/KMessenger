package com.github.huymaster.textguardian.server.utils

import com.fasterxml.jackson.databind.ObjectMapper
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.io.File

class AttachmentCompressor : KoinComponent {
    private val mapper = get<ObjectMapper>()
    private val root = File(System.getProperty("user.home"), ".kmessenger")
    private val mapFile = File(root, "attachments")
    private val attachmentsMap = mutableMapOf<String, String>()
}