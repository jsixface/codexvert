package ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.jsixface.common.AutoConversion
import ui.utils.ComboBox
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Composable
fun AutoConvertSettings(
    setting: AutoConversion, modifier: Modifier = Modifier, onChanged: (AutoConversion) -> Unit = {}
) {
    Column(modifier = modifier) {
        var newFrom by remember { mutableStateOf("") }
        var newTo by remember { mutableStateOf("") }
        Text(
            "Auto Conversion",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        setting.conversion.forEach { (from, to) ->
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp, 4.dp).fillMaxWidth()
            ) {
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.Center) { Text(from) }
                Row(
                    modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.Center
                ) { Icon(Icons.AutoMirrored.Rounded.ArrowForward, "To") }
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.Center) { Text(to) }
                FilledTonalButton(modifier = Modifier.padding(start = 10.dp), onClick = {
                    onChanged(setting.copy(conversion = setting.conversion - from))
                }) {
                    Icon(Icons.Filled.Delete, "Delete")
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp, 4.dp).fillMaxWidth()) {
            Row(modifier = Modifier.weight(1f)) {
                TextField(
                    value = newFrom, onValueChange = { newFrom = it },
                    label = { Text("From") })
            }
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.Center) {
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, "To")
            }
            Row(modifier = Modifier.weight(1f)) {
                TextField(value = newTo, onValueChange = { newTo = it }, label = { Text("To") })
            }
            OutlinedButton(modifier = Modifier.padding(start = 10.dp), onClick = {
                onChanged(setting.copy(conversion = setting.conversion + mapOf(newFrom to newTo)))
                newFrom = ""
                newTo = ""
            }) {
                Icon(Icons.Filled.Add, "Add")
            }
        }
        ComboBox(
            title = "Media Scan Duration",
            options = listOf(1.minutes, 5.minutes, 15.minutes, 30.minutes, 1.hours),
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            selected = setting.watchDuration
        ) { onChanged(setting.copy(watchDuration = it)) }
    }

}