package com.github.huymaster.textguardian.android.ui.activity

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.github.huymaster.textguardian.android.ui.theme.KMessengerTheme
import kotlinx.coroutines.cancel
import org.koin.core.component.KoinComponent

@ExperimentalMaterial3ExpressiveApi
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class,
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalSharedTransitionApi::class
)
abstract class BaseActivity : AppCompatActivity(), KoinComponent {
    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent(savedInstanceState)
    }

    final override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContent(savedInstanceState, persistentState)
    }

    final override fun onDestroy() {
        lifecycleScope.cancel()
        onDestroyEx()
        super.onDestroy()
    }

    private fun setContent(
        savedInstanceState: Bundle? = null,
        persistentState: PersistableBundle? = null
    ) {
        onCreateEx(savedInstanceState, persistentState)
        enableEdgeToEdge()
        setContent {
            KMessengerTheme {
                Content(savedInstanceState, persistentState)
            }
        }
    }

    open fun onCreateEx(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {}

    open fun onDestroyEx() {}

    @Composable
    protected open fun Content(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        Scaffold { contentPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "No content",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}