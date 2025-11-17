@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)

package com.github.huymaster.textguardian.android.ui.screen

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.github.huymaster.textguardian.android.ui.component.QuickThemeButton
import com.github.huymaster.textguardian.android.ui.utils.SharedTransitionBundle
import com.github.huymaster.textguardian.android.viewmodel.ChatListState
import com.github.huymaster.textguardian.android.viewmodel.ChatListViewModel
import com.github.huymaster.textguardian.core.api.type.ConversationInfo
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun ChatListScreen(
    transitionBundle: SharedTransitionBundle,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val model = viewModel<ChatListViewModel>()
    val state by model.state.collectAsState()
    LaunchedEffect(Unit) { model.reload() }
    ChatListScreenContent(
        transitionBundle,
        navController,
        state,
        { scope.launch { model.reload() } },
        { scope.launch { model.logout() } },
        { scope.launch { model.deleteConversation(it) } }
    )
}

@Composable
private fun ChatListScreenContent(
    transitionBundle: SharedTransitionBundle,
    navController: NavController? = null,
    state: ChatListState,
    reloadRequest: () -> Unit,
    logout: () -> Unit,
    deleteConversation: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var logoutDialog by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scrollBehavior = if (state.chats.isEmpty())
        TopAppBarDefaults.pinnedScrollBehavior()
    else
        TopAppBarDefaults.enterAlwaysScrollBehavior()
    val snackbarHostState by remember { mutableStateOf(SnackbarHostState()) }

    LaunchedEffect(state.error) {
        if (state.error != null)
            snackbarHostState.showSnackbar(state.error)
        else
            snackbarHostState.currentSnackbarData?.dismiss()
    }

    if (logoutDialog)
        AlertDialog(
            onDismissRequest = { logoutDialog = false },
            icon = { Icon(Icons.AutoMirrored.Default.Logout, null) },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                Button(
                    onClick = {
                        logout()
                        logoutDialog = false
                    }
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { logoutDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    ChatListDrawer(
        drawerState = drawerState,
        navController = navController,
        logout = { logoutDialog = true }
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                ChatListTopAppBar(scrollBehavior, reloadRequest) {
                    scope.launch { if (drawerState.isOpen) drawerState.close() else drawerState.open() }
                }
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { navController?.navigate("NEW_CHAT") }) {
                    Icon(
                        Icons.Default.Add,
                        null
                    )
                }
            }
        ) { paddingValues ->
            Surface(
                modifier = Modifier.padding(paddingValues)
            ) {
                PullToRefreshBox(
                    modifier = Modifier.fillMaxSize(),
                    isRefreshing = state.isLoading, onRefresh = reloadRequest
                ) {
                    ChatListConversations(
                        transitionBundle,
                        state.chats.sortedByDescending { it.lastUpdated },
                        { navController?.navigate("CHAT/${it}") },
                        !state.isLoading,
                        deleteConversation
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatListDrawer(
    modifier: Modifier = Modifier,
    drawerState: DrawerState,
    navController: NavController? = null,
    logout: () -> Unit,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text("KMessenger", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                    HorizontalDivider()
                    NavigationDrawerItem(
                        label = { Text("Profile") },
                        icon = { Icon(Icons.Default.Person, null) },
                        selected = false,
                        onClick = { navController?.navigate("PROFILE") }
                    )
                    NavigationDrawerItem(
                        label = { Text("Settings") },
                        icon = { Icon(Icons.Default.Settings, null) },
                        selected = false,
                        onClick = { navController?.navigate("SETTINGS") }
                    )
                    HorizontalDivider()
                    NavigationDrawerItem(
                        label = { Text("Logout") },
                        icon = { Icon(Icons.AutoMirrored.Default.Logout, null) },
                        selected = false,
                        onClick = logout
                    )
                }
            }
        },
        content = content
    )
}

@Composable
private fun ChatListTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    reloadRequest: () -> Unit,
    onMenuClick: () -> Unit = {}
) {
    TopAppBar(
        title = { Text("KMessenger", maxLines = 1, overflow = TextOverflow.Ellipsis) }, navigationIcon = {
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, null) }
        },
        colors = TopAppBarDefaults.topAppBarColors(),
        actions = {
            IconButton(onClick = reloadRequest) { Icon(Icons.Default.Refresh, null) }
            QuickThemeButton()
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun ChatListConversations(
    transitionBundle: SharedTransitionBundle,
    list: List<ConversationInfo>,
    onChatSelect: (String) -> Unit,
    enabled: Boolean,
    deleteConversation: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.medium
            ),
        contentAlignment = Alignment.Center
    ) {
        if (list.isEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                    text = "No conversation available"
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                itemsIndexed(items = list, key = { idx, it -> it.conversationId }) { index, item ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ConversationItem(
                            transitionBundle,
                            item,
                            { deleteConversation(item.conversationId) },
                            { onChatSelect(item.conversationId) }
                        )
                        if (index + 1 < list.size)
                            HorizontalDivider(modifier = Modifier.fillMaxWidth(0.95f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationItem(
    transitionBundle: SharedTransitionBundle,
    info: ConversationInfo,
    onDelete: () -> Unit,
    onChatSelect: () -> Unit
) {
    var isMenuOpen by remember { mutableStateOf(false) }
    with(transitionBundle.sharedTransitionScope) {
        Box(
            modifier = Modifier
                .sharedBounds(
                    rememberSharedContentState("chatbox_${info.conversationId}"),
                    transitionBundle.animatedContentScope
                )
                .clickable { onChatSelect() }
                .fillMaxWidth()
        ) {
            Row {
                val textMeasurer = rememberTextMeasurer()
                val bg = MaterialTheme.colorScheme.primaryContainer
                val fg = MaterialTheme.colorScheme.onPrimaryContainer
                val character = info.name.first().toString()
                val layout = textMeasurer.measure(character, style = MaterialTheme.typography.titleLarge)
                Canvas(
                    modifier = Modifier
                        .sharedElement(
                            rememberSharedContentState("icon_${info.conversationId}"),
                            transitionBundle.animatedContentScope
                        )
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
                Text(
                    modifier = Modifier
                        .sharedElement(
                            rememberSharedContentState("title_${info.conversationId}"),
                            transitionBundle.animatedContentScope
                        )
                        .weight(1f)
                        .padding(top = 12.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    text = info.name,
                    fontStyle = MaterialTheme.typography.titleLarge.fontStyle
                )
                Text(
                    modifier = Modifier
                        .padding(top = 12.dp, start = 12.dp, end = 12.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    text = instantToString(info.lastUpdated),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp
                )
                Box(
                    modifier = Modifier.align(Alignment.CenterVertically),
                ) {
                    IconButton(
                        onClick = { isMenuOpen = !isMenuOpen }
                    ) {
                        Icon(Icons.Default.MoreVert, null)
                    }
                    MoreMenu(
                        isMenuOpen,
                        onDelete
                    ) { isMenuOpen = false }
                }
            }
        }
    }
}

@Composable
private fun MoreMenu(
    isMenuOpen: Boolean,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        expanded = isMenuOpen,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text("Delete") },
            leadingIcon = { Icon(Icons.Default.Delete, null) },
            onClick = onDelete
        )
    }
}

private fun instantToString(past: Instant): String {
    val now = Instant.now()
    val duration = Duration.between(past, now)
    if (duration.isZero || duration.isNegative)
        return "Just now"
    val seconds = duration.seconds
    val minutes = seconds / 60
    val hours = minutes / 60

    val pastTime = past.atZone(ZoneId.systemDefault())
    val currentTime = now.atZone(ZoneId.systemDefault())

    return when {
        hours < 1 -> {
            when {
                minutes > 0 -> "$minutes mins ago"
                else -> "Just now"
            }
        }

        hours < 24 -> {
            "$hours hours ago"
        }

        else -> {
            val yearDifferent = pastTime.year != currentTime.year
            DateTimeFormatter
                .ofPattern(if (!yearDifferent) "dd/MM" else "dd/MM/yy", Locale.getDefault())
                .withZone(ZoneId.systemDefault())
                .format(past)
        }
    }
}