package com.github.huymaster.textguardian.android.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

enum class SwipeOrientation {
    LEFT,
    RIGHT,
    ALL
}

@Composable
fun SwipeableContainer(
    modifier: Modifier = Modifier,
    onSwiped: (SwipeOrientation) -> Unit = {},
    swipeOrientation: SwipeOrientation = SwipeOrientation.ALL,
    swipePercentage: Float = 0.5f,
    flingVelocityThreshold: Float = 600f,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
            .pointerInput(Unit) {
                fling(
                    scope,
                    offsetX,
                    screenWidth,
                    swipePercentage,
                    swipeOrientation,
                    flingVelocityThreshold,
                    onSwiped
                )
            }
    ) {
        content()
    }
}

private suspend fun PointerInputScope.fling(
    scope: CoroutineScope,
    offsetX: Animatable<Float, AnimationVector1D>,
    screenWidth: Float,
    swipePercentage: Float,
    swipeOrientation: SwipeOrientation,
    flingVelocityThreshold: Float,
    onFling: (SwipeOrientation) -> Unit
) {
    val velocityTracker = VelocityTracker()
    detectDragGestures(
        onDragStart = { velocityTracker.resetTracking() },
        onDrag = { change, dragAmount ->
            val newOffsetX = offsetX.value + dragAmount.x
            if (swipeOrientation == SwipeOrientation.RIGHT && newOffsetX < 0) return@detectDragGestures
            if (swipeOrientation == SwipeOrientation.LEFT && newOffsetX > 0) return@detectDragGestures
            velocityTracker.addPosition(change.uptimeMillis, change.position)
            change.consume()
            scope.launch { offsetX.snapTo(newOffsetX) }
        },
        onDragEnd = {
            scope.launch {
                val direction = if (offsetX.value > 0) SwipeOrientation.RIGHT else SwipeOrientation.LEFT
                val velocity = velocityTracker.calculateVelocity()
                velocityTracker.resetTracking()
                val sign = if (offsetX.value > 0) 1 else -1
                val opSign = if (sign == 1) -1 else 1

                val isSwiped = abs(offsetX.value) > screenWidth * swipePercentage
                val isFling = velocity.x * opSign > flingVelocityThreshold && velocity.x * offsetX.value > 0
                println("isSwiped: $isSwiped, isFling: $isFling")
                if (isSwiped || isFling) {
                    offsetX.animateTo(screenWidth * sign, tween(150))
                    onFling(direction)
                } else {
                    offsetX.animateTo(0f, tween(150))
                }
            }
        }
    )
}