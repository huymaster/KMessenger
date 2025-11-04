package com.github.huymaster.textguardian.android.viewmodel

import com.github.huymaster.textguardian.android.app.JWTTokenManager
import com.github.huymaster.textguardian.android.data.repository.ConversationRepository
import com.github.huymaster.textguardian.android.data.type.RepositoryResult
import com.github.huymaster.textguardian.core.api.type.ConversationInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import org.koin.core.component.inject

data class ChatListState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val chats: List<ConversationInfo> = emptyList()
)

class ChatListViewModel : BaseViewModel() {
    private val repository: ConversationRepository by inject()
    private val tokenManager: JWTTokenManager by inject()
    private val _state = MutableStateFlow(ChatListState())
    val state: StateFlow<ChatListState> = _state.asStateFlow()

    init {
        runBlocking { reload() }
    }

    suspend fun reload() {
        _state.update { it.copy(isLoading = true) }
        when (val result = repository.getConversations()) {
            is RepositoryResult.Success<List<ConversationInfo>> ->
                _state.update { it.copy(isLoading = false, chats = result.data ?: emptyList()) }

            else ->
                _state.update { it.copy(isLoading = false, error = result.message) }
        }
    }
}