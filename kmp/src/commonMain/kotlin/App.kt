import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun App() {
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog)
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Title") },
            text = { Text("Text") },
            confirmButton = {
                Button(
                    onClick = { showDialog = false }
                ) { Text("OK ") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) { Text("Cancel ") }
            }
        )
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            modifier = Modifier,
            onClick = { showDialog = true }
        ) { Text("Click me!") }
    }
}