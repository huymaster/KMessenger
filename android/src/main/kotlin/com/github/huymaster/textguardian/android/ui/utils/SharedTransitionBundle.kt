@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.github.huymaster.textguardian.android.ui.utils

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope

data class SharedTransitionBundle(
    val sharedTransitionScope: SharedTransitionScope,
    val animatedContentScope: AnimatedContentScope
)

infix fun SharedTransitionScope.with(animatedContentScope: AnimatedContentScope): SharedTransitionBundle {
    return SharedTransitionBundle(this, animatedContentScope)
}

infix fun AnimatedContentScope.with(sharedTransitionScope: SharedTransitionScope): SharedTransitionBundle {
    return SharedTransitionBundle(sharedTransitionScope, this)
}