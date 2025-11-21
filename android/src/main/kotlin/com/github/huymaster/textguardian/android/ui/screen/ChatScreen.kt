@file:OptIn(
    ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class
)

package com.github.huymaster.textguardian.android.ui.screen

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.drawText
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
import com.github.huymaster.textguardian.core.api.type.WebSocketMsg
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.closeQuietly
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
        if (conversationId != null)
            model.setConversation(conversationId)
    }
    DisposableEffect(Unit) {
        if (conversationId != null) {
            val client = HttpClient(OkHttp) { install(WebSockets) }
            try {
                scope.launch {
                    client.webSocket(
                        urlString = "wss://api-textguardian.ddns.net/api/v1/message/${conversationId}",
                    ) {
                        val handshakeFrame = incoming.receive() as? Frame.Text
                        if (handshakeFrame?.readText() != WebSocketMsg.HANDSHAKE) {
                            close()
                            return@webSocket
                        }
                        model.processHandshake(this)
                        for (frame in incoming)
                            if (frame is Frame.Text)
                                if (frame.readText() == WebSocketMsg.NEW_MESSAGE)
                                    model.getLastestMessages()
                    }
                }
            } catch (e: Exception) {
                Log.w("Chat", "Can't connect to ws server. ${e.message}")
            }
            onDispose { client.closeQuietly() }
        } else onDispose { }
    }
    ChatScreenContent(
        transitionBundle = transitionBundle,
        navController = navController,
        conversationId = runCatching { UUID.fromString(conversationId) }.getOrNull(),
        state = state,
        messageLoadRequest = { scope.launch { model.getMessages(it) } },
        onMessageSend = { message, replyTo ->
            scope.launch { model.sendMessage(message, replyTo) }
        }
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
    onMessageSend: (String, String?) -> Unit = { _, _ -> }
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        if (state.error != null)
            snackbarHostState.showSnackbar(state.error)
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
                        state.conversationInfo != null -> ChatContent(state, messageLoadRequest, onMessageSend)
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
    onMessageSend: (String, String?) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MessageList(
            modifier = Modifier.weight(1f),
            state = state,
            messageLoadRequest = messageLoadRequest
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
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val density = LocalDensity.current
    val ime = WindowInsets.ime
    val imeAnimationTarget = WindowInsets.imeAnimationTarget
    val isImeVisible = WindowInsets.isImeVisible

    val isImeFullyVisible by remember {
        derivedStateOf {
            ime.getBottom(density) == imeAnimationTarget.getBottom(density) &&
                    imeAnimationTarget.getBottom(density) > 0
        }
    }

    LaunchedEffect(isImeVisible, isImeFullyVisible) {
        delay(100)
        if (!isImeVisible)
            focusManager.clearFocus()
        else if (isImeFullyVisible)
            focusRequester.requestFocus()
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            value = text,
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            shape = MaterialTheme.shapes.extraLarge,
            label = { Text("Message") }
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
    messageLoadRequest: (String?) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val cipherManager = koinInject<CipherManager>()

    val displayMessages = remember(state.messages) {
        state.messages
            .asSequence()
            .sortedByDescending { it.sendAt }
            .distinctBy { it.id }
            .toList()
    }

    LaunchedEffect(Unit) {
        if (state.messages.isEmpty()) messageLoadRequest(null)
    }

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
            key = { message -> message.id.toString() }
        ) { message ->
            MessageItem(
                message = message,
                cipherManager = cipherManager
            )
        }
    }
}

@Composable
private fun MessageItem(
    message: Message,
    cipherManager: CipherManager
) {
    val decryptedContent by produceState<DecryptionState>(
        initialValue = DecryptionState.Loading,
        key1 = message.id
    ) {
        value = withContext(Dispatchers.Default) {
            runCatching {
                cipherManager.decrypt(message)
            }.fold(
                onSuccess = { DecryptionState.Success(it) },
                onFailure = { DecryptionState.Error(it.message ?: "Decryption error") }
            )
        }
    }

    Box(modifier = Modifier.padding(horizontal = 8.dp)) {
        when (val state = decryptedContent) {
            is DecryptionState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            }

            is DecryptionState.Success -> {
                Text(text = state.content)
            }

            is DecryptionState.Error -> {
                Text(text = "Error: ${state.message}")
            }
        }
    }
}