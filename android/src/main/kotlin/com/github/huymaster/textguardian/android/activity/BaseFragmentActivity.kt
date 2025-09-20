package com.github.huymaster.textguardian.android.activity

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.github.huymaster.textguardian.android.ui.theme.KMessengerTheme

abstract class BaseFragmentActivity : FragmentActivity() {
    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent(savedInstanceState)
    }

    final override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContent(savedInstanceState, persistentState)
    }

    private fun setContent(
        savedInstanceState: Bundle?,
        persistentState: PersistableBundle? = null
    ) {
        enableEdgeToEdge()
        setContent {
            KMessengerTheme {
                Content(savedInstanceState, persistentState)
            }
        }
    }


    @Composable
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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