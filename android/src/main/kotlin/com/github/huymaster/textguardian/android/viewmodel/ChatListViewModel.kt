package com.github.huymaster.textguardian.android.viewmodel

import com.github.huymaster.textguardian.android.app.JWTTokenManager
import com.github.huymaster.textguardian.android.data.repository.AuthenticationRepository
import com.github.huymaster.textguardian.android.data.repository.ConversationRepository
import com.github.huymaster.textguardian.android.data.type.RepositoryResult
import com.github.huymaster.textguardian.core.api.type.ConversationInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.inject

data class ChatListState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val chats: List<ConversationInfo> = emptyList()
)

class ChatListViewModel : BaseViewModel() {
    companion object {
        private var savedChats: List<ConversationInfo> = emptyList()
    }

    private val authRepository: AuthenticationRepository by inject()
    private val repository: ConversationRepository by inject()
    private val tokenManager: JWTTokenManager by inject()
    private val _state = MutableStateFlow(ChatListState(chats = savedChats))
    val state: StateFlow<ChatListState> = _state.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO + Job()).launch { reload() }
    }

    suspend fun reload() {
        _state.update { it.copy(isLoading = true) }
        when (val result = repository.getConversations()) {
            is RepositoryResult.Success<List<ConversationInfo>> -> {
                savedChats = result.data ?: emptyList()
                _state.update { it.copy(isLoading = false, chats = savedChats) }
            }

            else ->
                _state.update { it.copy(isLoading = false, error = result.message) }
        }
    }

    suspend fun deleteConversation(conversationId: String) {
        _state.update { it.copy(isLoading = true) }
        when (val result = repository.deleteConversation(conversationId)) {
            is RepositoryResult.Success -> {
                _state.update { it.copy(isLoading = false) }
                reload()
            }

            else -> _state.update { it.copy(isLoading = false, error = result.message) }
        }
    }

    suspend fun logout() {
        authRepository.logout()
        tokenManager.removeRefreshToken()
    }
}