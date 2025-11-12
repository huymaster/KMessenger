@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)

package com.github.huymaster.textguardian.android.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.github.huymaster.textguardian.android.ui.utils.SharedTransitionBundle
import com.github.huymaster.textguardian.android.ui.utils.with
import com.github.huymaster.textguardian.android.viewmodel.NewChatState
import com.github.huymaster.textguardian.android.viewmodel.NewChatViewModel
import com.github.huymaster.textguardian.core.api.type.UserInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NewChatScreen(
    transitionBundle: SharedTransitionBundle,
    navController: NavController?
) {
    val scope = rememberCoroutineScope()
    val model = viewModel<NewChatViewModel>()
    val state by model.state.collectAsState()


    NewChatScreenContent(
        transitionBundle,
        navController,
        state,
        { scope.launch { model.searchUsers(it) } },
        { scope.launch { model.selectUser(it) } },
        { scope.launch { model.deselectUser(it) } },
        { scope.launch { model.createConversation(it) { navController?.popBackStack() } } }
    )
}


@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun NewChatScreenPreview() {
    Scaffold {
        Surface(modifier = Modifier.padding(it)) {
            SharedTransitionLayout {
                AnimatedContent(Unit) {
                    NewChatScreenContent(
                        transitionBundle = this@SharedTransitionLayout with this,
                        navController = null,
                        state = NewChatState(),
                        onUserSearch = {},
                        userSelect = {},
                        userDeselect = {},
                        createConversation = {}
                    )
                }
            }
        }
    }
}

@Composable
private fun NewChatScreenContent(
    transitionBundle: SharedTransitionBundle,
    navController: NavController?,
    state: NewChatState,
    onUserSearch: (String) -> Unit,
    userSelect: (UserInfo) -> Unit,
    userDeselect: (UserInfo) -> Unit,
    createConversation: (String) -> Unit
) {
    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = { NewChatTopAppBar(navController) }
    ) { contentPaddings ->
        Surface(
            modifier = Modifier
                .padding(contentPaddings)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                if (state.isLoading)
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                ActionBar(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    onAction = createConversation,
                    enabled = state.selectedUsers.isNotEmpty() && !state.isLoading
                )
                SelectedUsers(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .padding(top = 2.dp),
                    state = state,
                    userDeselect = userDeselect,
                    enabled = !state.isLoading
                )
                if (state.error != null)
                    Text(text = state.error, color = MaterialTheme.colorScheme.error)
                UserSearch(
                    modifier = Modifier.weight(1f),
                    state = state,
                    onUserSearch = onUserSearch,
                    userSelect = userSelect,
                    enabled = !state.isLoading,
                )
            }
        }
    }
}

@Composable
private fun NewChatTopAppBar(navController: NavController?) {
    TopAppBar(
        title = { Text("New Chat") },
        actions = {},
        navigationIcon = {
            IconButton(onClick = { navController?.popBackStack() }) {
                Icon(Icons.Default.Close, null)
            }
        }
    )
}


@Composable
private fun UserSearch(
    modifier: Modifier = Modifier,
    state: NewChatState,
    onUserSearch: (String) -> Unit,
    userSelect: (UserInfo) -> Unit,
    enabled: Boolean
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var text by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(text) {
        delay(500)
        onUserSearch(text)
    }
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        SharedTransitionLayout {
            AnimatedContent(expanded) { expand ->
                if (expand)
                    OutlinedTextField(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .sharedBounds(
                                rememberSharedContentState("box"),
                                this,
                                resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                            )
                            .sharedElement(rememberSharedContentState("box_e"), this)
                            .fillMaxWidth(),
                        value = text,
                        onValueChange = { text = it.trim() },
                        maxLines = 1,
                        singleLine = true,
                        label = { Text("Search user(username or phone)", maxLines = 1) },
                        trailingIcon = {
                            if (state.isSearching)
                                CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                            else if (text.isNotBlank())
                                IconButton(onClick = { text = "" }) { Icon(Icons.Default.Clear, null) }
                            else
                                Icon(Icons.Default.Search, null)
                        },
                        leadingIcon = {
                            IconButton(
                                onClick = { expanded = false }
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .sharedElement(rememberSharedContentState("icon"), this),
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                else
                    Button(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .sharedBounds(
                                rememberSharedContentState("box"),
                                this,
                                resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                            )
                            .sharedElement(rememberSharedContentState("box_e"), this),
                        onClick = { expanded = true }
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(24.dp)
                                .sharedElement(rememberSharedContentState("icon"), this@AnimatedContent),
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null
                        )
                        Spacer(Modifier.size(8.dp))
                        Text("Add user")
                    }
            }
        }
        Spacer(Modifier.size(12.dp))
        if (state.searchUsers.isNotEmpty())
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(state.searchUsers) { user -> SearchUserItem(user, userSelect, enabled) }
            }
        else
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No users found")
            }
    }
}

@Composable
private fun SelectedUsers(
    modifier: Modifier = Modifier,
    state: NewChatState,
    userDeselect: (UserInfo) -> Unit,
    enabled: Boolean
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(bottom = 8.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
    ) {
        items(state.selectedUsers) { user -> SelectedUserItem(user, userDeselect, enabled) }
    }
}

@Composable
private fun ActionBar(
    modifier: Modifier = Modifier,
    onAction: (String) -> Unit,
    enabled: Boolean = true
) {
    var text by rememberSaveable { mutableStateOf("") }
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = text,
            onValueChange = { text = it.replace("\n", " ").replace(Regex("\\s+"), " ") },
            maxLines = 1,
            singleLine = true,
            label = { Text("Chat name") },
            trailingIcon = {
                IconButton(
                    modifier = Modifier,
                    colors = IconButtonDefaults.filledIconButtonColors(),
                    onClick = { onAction(text) }
                ) {
                    Icon(Icons.AutoMirrored.Default.Chat, null)
                }
            }
        )
    }
}

@Composable
private fun SearchUserItem(
    user: UserInfo,
    onClick: (UserInfo) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            modifier = Modifier.size(48.dp),
            imageVector = Icons.Default.Person,
            contentDescription = null
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = user.displayName ?: "<name not set>",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = user.username ?: user.phoneNumber,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(
            modifier = Modifier.padding(8.dp),
            enabled = enabled,
            onClick = { onClick(user) }
        ) {
            Icon(Icons.Default.Add, null)
        }
    }
}

@Composable
private fun SelectedUserItem(
    user: UserInfo,
    onClick: (UserInfo) -> Unit,
    enabled: Boolean
) {
    InputChip(
        modifier = Modifier.padding(4.dp),
        enabled = enabled,
        selected = true,
        onClick = { onClick(user) },
        leadingIcon = { Icon(Icons.Default.Person, null) },
        trailingIcon = { Icon(Icons.Default.Close, null) },
        label = {
            Text(
                text = user.displayName ?: user.username ?: user.phoneNumber,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    )
}