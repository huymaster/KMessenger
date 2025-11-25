@file:OptIn(
    ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class
)

package com.github.huymaster.textguardian.android.ui.screen

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.github.huymaster.textguardian.android.app.CipherManager
import com.github.huymaster.textguardian.android.ui.utils.SharedTransitionBundle
import com.github.huymaster.textguardian.android.ui.utils.with
import com.github.huymaster.textguardian.android.viewmodel.ChatState
import com.github.huymaster.textguardian.android.viewmodel.ChatViewModel
import com.github.huymaster.textguardian.core.api.type.ConversationInfo
import com.github.huymaster.textguardian.core.api.type.Message
import com.github.huymaster.textguardian.core.api.type.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import java.util.*

@Composable
fun ChatScreen(
    transitionBundle: SharedTransitionBundle,
    navController: NavController?,
    conversationId: String?
) {
    val model = viewModel<ChatViewModel>()
    val scope = rememberCoroutineScope { Dispatchers.IO }
    val state by model.state.collectAsStateWithLifecycle()
    LaunchedEffect(conversationId) {
        if (conversationId != null) {
            model.setConversation(conversationId)
            model.connectToChatSocket(conversationId)
        }
    }
    LaunchedEffect(Unit) { model.getMessages(null) }
    DisposableEffect(Unit) { onDispose { model.disconnectSocket() } }
    ChatScreenContent(
        transitionBundle = transitionBundle,
        navController = navController,
        conversationId = runCatching { UUID.fromString(conversationId) }.getOrNull(),
        state = state,
        messageLoadRequest = { scope.launch { model.getMessages(it) } },
        onMessageSend = { message, replyTo ->
            scope.launch { model.sendMessage(message, replyTo) }
        },
        userQuery = { model.getUserInfo(it) },
        clearNewMessages = { scope.launch { model.clearNewMessages() } }
    )
}

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@Preview
@Composable
private fun Preview() {
    SharedTransitionLayout {
        AnimatedContent(Unit) {
            ChatScreenContent(
                transitionBundle = this@SharedTransitionLayout with this,
                navController = null,
                conversationId = null,
                state = ChatState(participantNumber = 5)
            )
        }
    }
}

@Composable
private fun ChatScreenContent(
    transitionBundle: SharedTransitionBundle,
    navController: NavController?,
    conversationId: UUID?,
    state: ChatState,
    messageLoadRequest: (String?) -> Unit = { _ -> },
    onMessageSend: (String, String?) -> Unit = { _, _ -> },
    userQuery: suspend (UUID?) -> UserInfo? = { _ -> null },
    clearNewMessages: () -> Unit = { }
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        if (state.error != null)
            snackbarHostState.showSnackbar(state.error, withDismissAction = true)
    }
    Scaffold(
        modifier = Modifier.imePadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ChatTopBar(
                transitionBundle,
                navController,
                conversationId?.toString(),
                state.participantNumber,
                state.masterLoading,
                state.conversationInfo
            )
        }
    ) { contentPadding ->
        with(transitionBundle.sharedTransitionScope) {
            Surface(
                modifier = Modifier
                    .padding(contentPadding)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .sharedBounds(
                            rememberSharedContentState("chatbox_${conversationId}"),
                            transitionBundle.animatedContentScope,
                            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                            zIndexInOverlay = 1f,
                            renderInOverlayDuringTransition = false
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.masterLoading)
                        CircularProgressIndicator()
                    else if (state.masterError != null) {
                        Text(
                            text = state.masterError,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else when {
                        state.conversationInfo != null -> ChatContent(
                            state,
                            messageLoadRequest,
                            onMessageSend,
                            userQuery,
                            clearNewMessages
                        )

                        else -> InvalidChat()
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatTopBar(
    transitionBundle: SharedTransitionBundle,
    navController: NavController?,
    conversationId: String?,
    participantNumber: Int = -1,
    isLoading: Boolean = false,
    info: ConversationInfo? = null
) {
    with(transitionBundle.sharedTransitionScope) {
        TopAppBar(
            modifier = Modifier.sharedBounds(
                rememberSharedContentState("topbar"),
                transitionBundle.animatedContentScope,
                resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
            ),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary
            ),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ChatIcon(
                        conversationId = info?.conversationId,
                        name = info?.name,
                        modifier = Modifier.sharedElement(
                            rememberSharedContentState("icon_${conversationId}"),
                            transitionBundle.animatedContentScope
                        )
                    )
                    Column(
                        modifier = Modifier
                    ) {
                        Text(
                            modifier = Modifier
                                .sharedElement(
                                    rememberSharedContentState("title_${conversationId}"),
                                    transitionBundle.animatedContentScope
                                ),
                            text = if (isLoading) "Loading..." else info?.name ?: "<invalid conversation>",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        with(transitionBundle.animatedContentScope) {
                            Text(
                                modifier = Modifier
                                    .animateEnterExit(
                                        enter = fadeIn() + slideInVertically { it },
                                        exit = fadeOut() + slideOutVertically { it }
                                    ),
                                text = if (participantNumber > 0) "$participantNumber members" else "unknown",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = { navController?.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )
    }
}

@Composable
private fun ChatIcon(
    conversationId: String?,
    name: String?,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val bg = MaterialTheme.colorScheme.primaryContainer
    val fg = MaterialTheme.colorScheme.onPrimaryContainer
    val character = name?.first()?.toString() ?: "?"
    val layout = textMeasurer.measure(character, style = MaterialTheme.typography.titleLarge)
    Canvas(
        modifier = modifier
            .size(78.dp)
            .padding(12.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        drawCircle(
            bg,
            radius = minOf(canvasWidth, canvasHeight) / 2f,
            center = Offset(canvasWidth / 2f, canvasHeight / 2f)
        )
        val x = (canvasWidth - layout.size.width) / 2f
        val y = (canvasHeight - layout.size.height) / 2f
        drawText(
            textLayoutResult = layout,
            color = fg,
            topLeft = Offset(x, y)
        )
    }
}

@Composable
private fun InvalidChat() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "This conversation is invalid",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun ChatContent(
    state: ChatState,
    messageLoadRequest: (String?) -> Unit,
    onMessageSend: (String, String?) -> Unit,
    userQuery: suspend (UUID?) -> UserInfo?,
    clearNewMessages: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MessageList(
            modifier = Modifier.weight(1f),
            state = state,
            messageLoadRequest = messageLoadRequest,
            userQuery = userQuery,
            clearNewMessages = clearNewMessages
        )
        InputBar(
            modifier = Modifier.padding(horizontal = 8.dp),
            state = state,
            onSend = { onMessageSend(it, null) }
        )
    }
}


@Composable
private fun InputBar(
    modifier: Modifier = Modifier,
    state: ChatState,
    onSend: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            modifier = Modifier
                .weight(1f),
            value = text,
            singleLine = false,
            maxLines = 3,
            minLines = 1,
            onValueChange = { text = it },
            trailingIcon = {
                IconButton(
                    enabled = text.isNotBlank() && !state.messageSending && !state.masterLoading,
                    onClick = {
                        onSend(text)
                        text = ""
                    }
                ) {
                    if (state.messageSending)
                        CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                    else
                        Icon(Icons.AutoMirrored.Default.Send, contentDescription = "Send")
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = {
                onSend(text)
                text = ""
            }),
            shape = MaterialTheme.shapes.extraLarge,
            label = { Text("Message") },
            placeholder = { Text("Type your message here") }
        )
    }
}

data class DecryptedMessageCache(
    val id: String,
    var content: String? = null
)

sealed class DecryptionState {
    object Loading : DecryptionState()
    data class Success(val content: String) : DecryptionState()
    data class Error(val message: String) : DecryptionState()
}

@Composable
fun MessageList(
    modifier: Modifier = Modifier,
    state: ChatState,
    messageLoadRequest: (String?) -> Unit,
    userQuery: suspend (UUID?) -> UserInfo?,
    clearNewMessages: () -> Unit
) {
    val lazyListState = rememberLazyListState()
    val cipherManager = koinInject<CipherManager>()
    val scope = rememberCoroutineScope()
    var autoScroll by remember { mutableStateOf(true) }

    val displayMessages = remember(state.messages) {
        state.messages
            .asSequence()
            .distinctBy { it.id }
            .sortedByDescending { it.sendAt }
            .toList()
    }

    val newestMessageId = remember(displayMessages) { displayMessages.firstOrNull()?.id }

    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems == 0) return@derivedStateOf false

            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            val buffer = 5
            lastVisibleItemIndex >= (totalItems - buffer)
        }
    }

    LaunchedEffect(Unit) {
        if (state.messages.isEmpty()) messageLoadRequest(null)
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            val lastMessageId = displayMessages.lastOrNull()?.id
            if (lastMessageId != null) {
                messageLoadRequest(lastMessageId.toString())
            }
        }
    }

    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.firstVisibleItemIndex }
            .collect { index ->
                autoScroll = index == 0
                if (autoScroll) clearNewMessages()
            }
    }

    LaunchedEffect(autoScroll) {
        if (autoScroll) clearNewMessages()
    }

    LaunchedEffect(displayMessages.size, newestMessageId) {
        if (displayMessages.isNotEmpty()) {
            if (autoScroll && newestMessageId == displayMessages.first().id) {
                lazyListState.animateScrollToItem(0)
                clearNewMessages()
            }
        }
    }

    LaunchedEffect(displayMessages.size) {
        if (displayMessages.isNotEmpty() && autoScroll) {
            lazyListState.animateScrollToItem(0)
            clearNewMessages()
        }
    }
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = modifier
                .fillMaxSize()
                .imeNestedScroll(),
            contentPadding = PaddingValues(bottom = 8.dp, top = 8.dp),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom),
        ) {
            items(
                items = displayMessages,
                key = { message -> message.id.toString() },
                contentType = { "message" }
            ) { message ->
                MessageCard(
                    message = message,
                    state = state,
                    userQuery = userQuery,
                    cipherManager = cipherManager
                )
            }
            item {
                if (state.messageLoading)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
            }
        }
        if (!autoScroll || state.newMessages > 0) {
            BadgedBox(
                modifier = Modifier
                    .padding(16.dp)
                    .clickable(onClick = {
                        scope.launch {
                            lazyListState.animateScrollToItem(0)
                            autoScroll = true
                        }
                        clearNewMessages()
                    }),
                badge = {
                    if (state.newMessages > 0) {
                        Badge { Text(state.newMessages.toString()) }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Scroll to bottom",
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                        .size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun MessageCard(
    message: Message,
    state: ChatState,
    userQuery: suspend (UUID?) -> UserInfo?,
    cipherManager: CipherManager
) {
    var retryTrigger by remember { mutableIntStateOf(0) }
    val decryptionState by produceState<DecryptionState>(
        initialValue = DecryptionState.Loading,
        key1 = message.id,
        key2 = retryTrigger
    ) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                cipherManager.decrypt(message)
            }.fold(
                onSuccess = { DecryptionState.Success(it) },
                onFailure = { DecryptionState.Error(it.message ?: "Decryption error") }
            )
        }
    }

    val user by produceState<UserInfo?>(
        initialValue = null,
        key1 = message.senderId
    ) { value = withContext(Dispatchers.IO) { userQuery(message.senderId) } }
    val isSelf by produceState(
        initialValue = false,
        key1 = message.senderId,
        key2 = state.self
    ) { value = withContext(Dispatchers.IO) { message.senderId == state.self } }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 16.dp),
        contentAlignment = if (isSelf) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = if (isSelf) Alignment.End else Alignment.Start
            ) {
                Text(
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    text = user?.displayName ?: user?.username ?: user?.phoneNumber ?: "<unknown>"
                )
                MessageContent(decryptionState = decryptionState) { retryTrigger++ }
            }
        }
    }
}

@Composable
private fun MessageContent(
    decryptionState: DecryptionState,
    retry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (decryptionState) {
            is DecryptionState.Error -> {
                Text(
                    text = "Can't decrypt message",
                    color = MaterialTheme.colorScheme.error
                )
                TextButton(onClick = retry) {
                    Text("Retry")
                }
            }

            DecryptionState.Loading -> {
                Text("Loading...")
                LinearProgressIndicator()
            }

            is DecryptionState.Success -> {
                Text(decryptionState.content)
            }
        }
    }
}