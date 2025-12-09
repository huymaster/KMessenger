import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt
import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.random.Random

@Composable
fun App() {
    var showDialog by remember { mutableStateOf(false) }
    var percent by remember { mutableStateOf(0) }
    val aPercent by animateIntAsState(percent)
    LaunchedEffect(showDialog) {
        if (showDialog)
            while (percent < 1000) {
                percent = min(percent + Random.nextInt(10), 1000)
                delay(Random.nextLong(100) + 50)
            }
    }
    if (showDialog && percent >= 1000)
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Title") },
            text = { Text("Text") },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        percent = 0
                    }
                ) { Text("OK ") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        percent = 0
                    }
                ) { Text("Cancel ") }
            }
        )
    Column(
        modifier = Modifier.fillMaxSize().padding(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            modifier = Modifier,
            enabled = !showDialog,
            onClick = { showDialog = true }
        ) { Text("Click me!") }
        AnimatedVisibility(showDialog) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LinearProgressIndicator(progress = { aPercent / 1000f })
                Text("${(aPercent / 1000f * 100).fastRoundToInt()}%")
            }
        }
    }
}