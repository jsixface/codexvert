package ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun ClientDialog(initialUrl: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        BackendDialogContent(initialUrl, onSave, onDismiss)
    }
}

@Composable
fun BackendDialogContent(
    url: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {

    var text by remember { mutableStateOf(url) }
    val padder = Modifier.padding(16.dp)
    Card(
        modifier = padder.width(500.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = padder.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Backend URL") }
                )
            }
            Row {
                SuggestionChip(
                    onClick = { text = "http://helium.home:8123" },
                    label = { Text("Helium") },
                    modifier = Modifier.padding(8.dp)
                )
                SuggestionChip(
                    onClick = { text = "http://localhost:8080" },
                    label = { Text("Localhost") },
                    modifier = Modifier.padding(8.dp)
                )
            }
            Row {
                Button(onClick = { onSave(text); onDismiss() }, modifier = padder) {
                    Text("Save")
                }
                Button(onClick = { onDismiss() }, modifier = padder) {
                    Text("Cancel")
                }
            }
        }
    }
}

