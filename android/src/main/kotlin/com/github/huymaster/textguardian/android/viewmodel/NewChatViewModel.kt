package com.github.huymaster.textguardian.android.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.github.huymaster.textguardian.android.data.repository.ConversationRepository
import com.github.huymaster.textguardian.android.data.repository.UserRepository
import com.github.huymaster.textguardian.android.data.type.RepositoryResult
import com.github.huymaster.textguardian.core.api.type.UserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import java.util.*

data class NewChatState(
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val error: String? = null,
    val selectedUsers: List<UserInfo> = emptyList(),
    val searchUsers: List<UserInfo> = emptyList(),
    val isContactsLoading: Boolean = false,
    val contacts: List<UserInfo> = emptyList()
)

class NewChatViewModel : BaseViewModel() {
    private val conversationRepository: ConversationRepository by inject()
    private val userRepository: UserRepository by inject()
    private val _state = MutableStateFlow(NewChatState())
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val phonePattern = Regex("^0\\d{9}$")
    private var me: UserInfo? = null
    private var lastSearchQuery: String = ""
    val state: StateFlow<NewChatState> = _state.asStateFlow()

    suspend fun searchUsers(query: String) {
        userRepository.getMe().let {
            if (it is RepositoryResult.Success)
                me = it.data
        }
        _state.update { it.copy(isSearching = true, error = null) }
        lastSearchQuery = query
        if (query.isBlank()) {
            _state.update { it.copy(isSearching = false, searchUsers = emptyList()) }
            return
        }
        when (val result = userRepository.findUsers(query)) {
            is RepositoryResult.Success<List<UserInfo>> -> {
                _state.update { it.copy(isSearching = false) }
                updateUserList(result.data ?: emptyList(), me)
                updateContactList(result.data ?: emptyList())
            }

            else -> _state.update { it.copy(isSearching = false, error = result.message) }
        }
    }

    private fun updateUserList(list: List<UserInfo>, exclude: UserInfo? = null) {
        val mList = list.toMutableList()
        mList.removeIf { it.userId == exclude?.userId }
        mList.removeIf { state.value.selectedUsers.any { user -> user.userId == it.userId } }
        _state.update { it.copy(searchUsers = mList) }
    }

    suspend fun reloadContacts(context: Context) {
        if (!checkContactPermission(context)) return
        scope.launch {
            _state.update { it.copy(isContactsLoading = true) }
            val contacts = loadContacts(context, phonePattern)
            val contactInfos = mutableListOf<UserInfo>()
            contacts.forEach { contact ->
                when (val result = userRepository.findUsers(contact.first)) {
                    is RepositoryResult.Success<List<UserInfo>> -> {
                        val user = result.data?.firstOrNull() ?: return@forEach
                        contactInfos.add(user.copy(displayName = "${user.displayName ?: ""} (${contact.second})"))
                    }

                    else -> {}
                }
            }
            updateContactList(contactInfos)
            _state.update { it.copy(isContactsLoading = false) }
        }
    }

    private fun loadContacts(context: Context, regex: Regex): List<Pair<String, String>> {
        val contactList = mutableListOf<Pair<String, String>>()
        val contentUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        )

        val cursor = context.contentResolver.query(contentUri, projection, null, null, null)
        cursor?.use {
            while (cursor.moveToNext()) {
                val phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    .replace(Regex("\\s+"), "")
                    .replace("+84", "0")
                val displayName =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                if (regex.matches(phoneNumber)) contactList.add(phoneNumber to displayName)
            }
        }
        println("Contact list: $contactList")
        return contactList
    }

    private fun updateContactList(list: List<UserInfo>) {
        _state.update { it.copy(contacts = list) }
        val mList = list.toMutableList()
        mList.removeIf { state.value.selectedUsers.any { user -> user.userId == it.userId } }
        _state.update { it.copy(contacts = mList) }
    }

    private fun checkContactPermission(context: Context): Boolean = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_CONTACTS
    ) == PackageManager.PERMISSION_GRANTED

    fun selectUser(user: UserInfo) {
        if (state.value.selectedUsers.any { it.userId == user.userId })
            return
        _state.update { it.copy(selectedUsers = it.selectedUsers + user) }
        updateUserList(state.value.searchUsers - user)
        updateContactList(state.value.contacts - user)
    }

    fun deselectUser(user: UserInfo) {
        if (state.value.selectedUsers.none { it.userId == user.userId })
            return
        _state.update { it.copy(selectedUsers = it.selectedUsers - user) }
        scope.launch { searchUsers(lastSearchQuery) }
    }

    suspend fun createConversation(name: String, onSuccess: () -> Unit) {
        if (name.isBlank()) {
            _state.update { it.copy(isLoading = false, error = "Conversation name is required") }
            return
        }
        if (state.value.selectedUsers.isEmpty()) {
            _state.update { it.copy(isLoading = false, error = "No users selected") }
            return
        }
        _state.update { it.copy(isLoading = true, error = null) }
        when (val result = conversationRepository.createConversation(name)) {
            is RepositoryResult.Success<UUID> -> {
                val conversation = result.data
                if (conversation == null)
                    _state.update { it.copy(isLoading = false, error = "Failed to create conversation") }
                else
                    addUsersToConversation(conversation.toString(), state.value.selectedUsers, onSuccess)
            }

            else -> _state.update { it.copy(isLoading = false, error = result.message) }
        }
    }

    private suspend fun addUsersToConversation(conversationId: String, users: List<UserInfo>, onSuccess: () -> Unit) {
        val results = users.map { user ->
            conversationRepository.addParticipant(conversationId, user.userId.toString())
        }
        if (results.all { it is RepositoryResult.Error }) {
            _state.update {
                it.copy(
                    isLoading = false,
                    error = "Failed to add users to conversation. ${results.first().message}"
                )
            }
        } else {
            _state.update { it.copy(isLoading = false) }
            onSuccess()
        }
    }
}