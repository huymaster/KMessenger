package com.github.huymaster.textguardian.android.ui.activity

import android.os.Bundle
import android.os.PersistableBundle
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.huymaster.textguardian.android.ui.model.AuthenticationViewModel
import com.github.huymaster.textguardian.android.ui.screen.LoginScreen
import com.github.huymaster.textguardian.android.ui.screen.RegisterScreen
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AuthenticationActivity : BaseActivity() {
    private val viewModel: AuthenticationViewModel by viewModel()

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun Content(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        val scope = rememberCoroutineScope()
        val titles = listOf("Login", "Register")
        val pageState = rememberPagerState(pageCount = { titles.size })

        LaunchedEffect(pageState.currentPage) { viewModel.toggleMode() }
        Scaffold { contentPadding ->
            Surface(
                Modifier
                    .padding(contentPadding)
                    .imePadding()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    TabHeader(
                        selected = pageState.currentPage,
                        state = pageState,
                        enabled = viewModel.allowSwitchMode,
                        titles = titles,
                        onTabClick = { scope.launch { pageState.animateScrollToPage(it) } }
                    )
                    HorizontalPager(
                        state = pageState,
                        userScrollEnabled = viewModel.allowSwitchMode
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                modifier = Modifier.padding(16.dp),
                                text = titles[it],
                                fontSize = MaterialTheme.typography.headlineLarge.fontSize
                            )
                            TabContent(selected = it, model = viewModel)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TabHeader(
        state: PagerState,
        enabled: Boolean,
        selected: Int,
        titles: List<String>,
        onTabClick: (Int) -> Unit,
    ) {
        val color = if (enabled) Color.Unspecified else Color.Gray
        PrimaryTabRow(
            selectedTabIndex = selected
        ) {
            titles.forEachIndexed { index, title ->
                Tab(
                    enabled = enabled,
                    modifier = Modifier.fillMaxSize(),
                    selected = selected == index,
                    onClick = { onTabClick(index) },
                ) { Text(text = title, modifier = Modifier.padding(16.dp), color = color) }
            }
        }
    }

    @Composable
    private fun ColumnScope.TabContent(
        selected: Int,
        model: AuthenticationViewModel
    ) {
        AnimatedContent(
            modifier = Modifier.weight(1f),
            targetState = selected,
            transitionSpec = {
                if (targetState > initialState)
                    slideInHorizontally { it / 2 } + fadeIn() togetherWith
                            slideOutHorizontally { -(it / 2) } + fadeOut()
                else
                    slideInHorizontally { -(it / 2) } + fadeIn() togetherWith
                            slideOutHorizontally { (it / 2) } + fadeOut()
            }
        ) {
            when (it) {
                0 -> LoginScreen(model)
                1 -> RegisterScreen(model)
            }
        }
    }
}