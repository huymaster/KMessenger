@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)

package com.github.huymaster.textguardian.android.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.github.huymaster.textguardian.android.ui.utils.SharedTransitionBundle
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val model = viewModel<NewChatViewModel>()
    val state by model.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        model.reloadContacts(context)
    }

    NewChatScreenContent(
        transitionBundle,
        navController,
        state,
        { scope.launch { model.searchUsers(it) } },
        { scope.launch { model.selectUser(it) } },
        { scope.launch { model.deselectUser(it) } },
        { scope.launch { model.reloadContacts(context) } },
        { scope.launch { model.createConversation(it) { navController?.popBackStack() } } }
    )
}

@Composable
private fun NewChatScreenContent(
    transitionBundle: SharedTransitionBundle,
    navController: NavController?,
    state: NewChatState,
    onUserSearch: (String) -> Unit,
    userSelect: (UserInfo) -> Unit,
    userDeselect: (UserInfo) -> Unit,
    contactReload: () -> Unit,
    createConversation: (String) -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Permission denied.", Toast.LENGTH_SHORT).show()
        }
    }


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
                    enabled = !state.isLoading
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

                var currentTab by remember { mutableIntStateOf(0) }
                PrimaryTabRow(selectedTabIndex = currentTab) {
                    Tab(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Contacts,
                                    contentDescription = "Contacts"
                                )
                                Text("Contacts")
                            }
                        },
                        selected = currentTab == 0,
                        onClick = {
                            currentTab = 0
                            contactReload()
                        }
                    )
                    Tab(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search"
                                )
                                Text("Find")
                            }
                        },
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 }
                    )
                }

                if (currentTab == 0)
                    Contacts(
                        context = context,
                        permissionLauncher = permissionLauncher,
                        modifier = Modifier.weight(1f),
                        state = state,
                        hasPermission = hasPermission,
                        userSelect = userSelect,
                        enabled = !state.isLoading,
                        reloadRequest = contactReload
                    )
                if (currentTab == 1)
                    UserSearch(
                        modifier = Modifier,
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
private fun ColumnScope.UserSearch(
    modifier: Modifier = Modifier,
    state: NewChatState,
    onUserSearch: (String) -> Unit,
    userSelect: (UserInfo) -> Unit,
    enabled: Boolean
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var text by rememberSaveable { mutableStateOf("") }
    val animationWeight by animateFloatAsState(if (expanded) 1f else 0.2f)
    LaunchedEffect(text) {
        delay(500)
        onUserSearch(text)
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .weight(animationWeight, fill = expanded)
    ) {
        SharedTransitionLayout {
            AnimatedContent(expanded) { expand ->
                if (!expand)
                    Box(
                        modifier = modifier
                            .sharedBounds(
                                rememberSharedContentState("box"),
                                this,
                                resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                            )
                    ) {
                        Button(
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            onClick = { expanded = true }
                        ) {
                            Icon(
                                modifier = Modifier
                                    .size(24.dp)
                                    .sharedElement(rememberSharedContentState("icon"), this@AnimatedContent),
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                            Spacer(Modifier.size(8.dp))
                            Text("Find users")
                        }
                    }
                else
                    Column(
                        modifier = modifier
                            .sharedBounds(
                                rememberSharedContentState("box"),
                                this,
                                resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                            )
                            .fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .fillMaxWidth(),
                            value = text,
                            readOnly = !enabled,
                            onValueChange = { text = it.trim() },
                            maxLines = 1,
                            singleLine = true,
                            label = { Text("Username or phone number", maxLines = 1) },
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
                                            .size(24.dp),
                                        imageVector = Icons.Default.PersonAdd,
                                        contentDescription = null
                                    )
                                }
                            }
                        )
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
        }
    }
}

@Composable
private fun Contacts(
    context: Context,
    permissionLauncher: ActivityResultLauncher<String>,
    modifier: Modifier = Modifier,
    state: NewChatState,
    hasPermission: Boolean,
    userSelect: (UserInfo) -> Unit,
    reloadRequest: () -> Unit,
    enabled: Boolean
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!hasPermission) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Read contact permission is required to search contacts")
                Button(onClick = {
                    when {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            Toast.makeText(context, "Permission already granted!", Toast.LENGTH_SHORT).show()
                        }

                        else -> {
                            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                        }
                    }
                }) {
                    Text("Grant permission")
                }
            }
        }
        if (state.isContactsLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        if (state.contacts.isNotEmpty())
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                items(state.contacts) { user -> SearchUserItem(user, userSelect, enabled) }
            }
        else
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("No contacts found")
                Button(onClick = reloadRequest) {
                    Text("Reload")
                }
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
            readOnly = !enabled,
            singleLine = true,
            label = { Text("Chat name") },
            trailingIcon = {
                IconButton(
                    modifier = Modifier,
                    colors = IconButtonDefaults.filledIconButtonColors(),
                    enabled = enabled,
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
            .clickable(enabled = enabled) { onClick(user) }
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