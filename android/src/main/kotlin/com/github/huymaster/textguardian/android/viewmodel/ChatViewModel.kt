package com.github.huymaster.textguardian.android.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.github.huymaster.textguardian.android.app.CipherManager
import com.github.huymaster.textguardian.android.app.JWTTokenManager
import com.github.huymaster.textguardian.android.data.repository.CipherRepository
import com.github.huymaster.textguardian.android.data.repository.ConversationRepository
import com.github.huymaster.textguardian.android.data.repository.MessageRepository
import com.github.huymaster.textguardian.android.data.type.RepositoryResult
import com.github.huymaster.textguardian.core.api.type.ConversationInfo
import com.github.huymaster.textguardian.core.api.type.Message
import com.github.huymaster.textguardian.core.api.type.UserPublicKey
import com.github.huymaster.textguardian.core.api.type.WebSocketMsg
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.get
import java.util.*

data class ChatState(
    val conversationInfo: ConversationInfo? = null,
    val messages: List<Message> = emptyList(),
    val masterLoading: Boolean = false,
    val messageLoading: Boolean = false,
    val messageSending: Boolean = false,
    val newMessages: Int = 0,
    val participantNumber: Int = -1,
    val masterError: String? = null,
    val error: String? = null
)

class ChatViewModel : BaseViewModel() {
    private val tokenManager: JWTTokenManager = get()
    private val messageRepository: MessageRepository = get()
    private val conversationRepository: ConversationRepository = get()
    private val cipherRepository: CipherRepository = get()
    private val cipherManager: CipherManager = get()
    private val publicKeys = Collections.synchronizedList(mutableListOf<UserPublicKey>())
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private val httpClient: HttpClient = HttpClient(OkHttp) { install(WebSockets) }

    private var socketJob: Job? = null

    fun connectToChatSocket(conversationId: String) {
        if (socketJob?.isActive == true) return

        socketJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                while (isActive) {
                    try {
                        httpClient.webSocket(
                            urlString = "wss://api-textguardian.ddns.net/api/v1/message/${conversationId}"
                        ) {
                            val handshakeFrame = incoming.receive() as? Frame.Text
                            if (handshakeFrame?.readText() != WebSocketMsg.HANDSHAKE) {
                                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid Handshake"))
                                return@webSocket
                            }

                            processHandshake(this)
                            for (frame in incoming) {
                                if (frame is Frame.Text) {
                                    val text = frame.readText()
                                    if (text == WebSocketMsg.NEW_MESSAGE) {
                                        getLastestMessages()
                                    }
                                }
                            }
                        }
                    } catch (e: ClosedReceiveChannelException) {
                        Log.w("ChatVM", "WS Closed: ${e.message}")
                    } catch (e: Exception) {
                        Log.e("ChatVM", "WS Error: ${e.message}")
                    }

                    delay(3000)
                }
            } finally {
                Log.d("ChatVM", "Socket Job Cancelled")
            }
        }
    }

    fun disconnectSocket() {
        socketJob?.cancel()
        socketJob = null
    }

    override fun onCleared() {
        super.onCleared()
        disconnectSocket()
    }

    suspend fun clearNewMessages() {
        _state.update { it.copy(newMessages = 0) }
    }

    suspend fun setConversation(conversationId: String) {
        _state.update { it.copy(masterLoading = true, masterError = null) }

        val infoResult = conversationRepository.getConversation(conversationId)
        if (infoResult !is RepositoryResult.Success) {
            _state.update { it.copy(masterLoading = false, masterError = infoResult.message) }
            return
        }

        _state.update { it.copy(conversationInfo = infoResult.data) }

        val partsResult = conversationRepository.getParticipants(conversationId)
        if (partsResult is RepositoryResult.Success) {
            val participants = partsResult.data ?: emptyList()
            _state.update { it.copy(participantNumber = participants.size) }

            val loadedKeys = coroutineScope {
                participants.map { participant ->
                    async {
                        val keyResult = cipherRepository.getPublicKey(participant.userId)
                        if (keyResult is RepositoryResult.Success) keyResult.data?.keyList else null
                    }
                }.awaitAll()
            }.filterNotNull().flatten()

            publicKeys.clear()
            publicKeys.addAll(loadedKeys)
        }

        _state.update { it.copy(masterLoading = false) }
    }

    suspend fun getMessages(startMessageId: String? = null) {
        val conversationId = _state.value.conversationInfo?.conversationId ?: return
        _state.update { it.copy(messageLoading = true, error = null) }

        val result = messageRepository.getMessages(conversationId, startMessageId)

        if (result is RepositoryResult.Success) {
            val newFetchedMessages = result.data ?: emptyList()

            _state.update { currentState ->
                val combined = (currentState.messages + newFetchedMessages)
                    .distinctBy { it.id }
                currentState.copy(messages = combined)
            }
        } else {
            _state.update { it.copy(error = result.message) }
        }
        _state.update { it.copy(messageLoading = false) }
    }

    suspend fun getLastestMessages() {
        val conversationId = _state.value.conversationInfo?.conversationId ?: return

        val since = _state.value.messages.lastOrNull()?.id?.toString()
        val result = messageRepository.getLastestMessages(conversationId, since)

        if (result is RepositoryResult.Success) {
            val incomingMessages = result.data ?: emptyList()
            if (incomingMessages.isNotEmpty()) {
                _state.update { currentState ->
                    currentState.copy(
                        newMessages = currentState.newMessages + incomingMessages.size,
                        messages = (currentState.messages + incomingMessages).distinctBy { it.id }
                    )
                }
            }
        }
    }

    suspend fun sendMessage(message: String, replyTo: String? = null) {
        if (message.isBlank()) return
        val conversationId = _state.value.conversationInfo?.conversationId ?: return
        _state.update { it.copy(messageSending = true, error = null) }
        val normalMessage = message.trimIndent().trim()
            .replace(Regex("\\s+"), " ")

        val msgResult = withContext(Dispatchers.IO) {
            try {
                val keysToUse = synchronized(publicKeys) { publicKeys.toList() }
                val (secret, list) = cipherManager.encapsulation(keysToUse.map { it.public })
                val cipher = cipherManager.encrypt(normalMessage, secret)

                val msg = Message(
                    id = UUID.randomUUID(),
                    content = cipher,
                    sessionKeys = list.toTypedArray(),
                    replyTo = runCatching { UUID.fromString(replyTo) }.getOrNull()
                )
                RepositoryResult.Success(msg)
            } catch (e: Exception) {
                RepositoryResult.Error(message = e.message ?: "Encryption failed")
            }
        }
        if (msgResult is RepositoryResult.Success) {
            val msg = msgResult.data!!
            val apiResult = messageRepository.sendMessage(conversationId, msg)
            if (apiResult !is RepositoryResult.Success) {
                _state.update { it.copy(error = apiResult.message) }
            }
        } else {
            _state.update { it.copy(error = msgResult.message) }
        }

        _state.update { it.copy(messageSending = false) }
    }

    suspend fun processHandshake(client: ClientWebSocketSession) {
        client.send(tokenManager.getAccessToken() ?: "")
    }
}