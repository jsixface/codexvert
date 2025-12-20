package ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.jsixface.common.AutoConversion
import io.github.jsixface.common.Codec
import io.github.jsixface.common.TrackType
import ui.utils.ComboBox
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Composable
fun AutoConvertSettings(
    setting: AutoConversion, modifier: Modifier = Modifier, onChanged: (AutoConversion) -> Unit = {}
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        val durations = listOf(1.minutes, 5.minutes, 10.minutes, 15.minutes, 30.minutes, 1.hours)
        ComboBox(
            "Media Scan Duration",
            durations,
            setting.watchDuration,
            modifier = Modifier.fillMaxWidth(),
            optionName = {
                val mins = inWholeMinutes
                if (mins >= 60) "${mins / 60} hour${if (mins / 60 > 1) "s" else ""}"
                else "$mins mins"
            },
            onSelect = { onChanged(setting.copy(watchDuration = it)) }
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            setting.conversion.forEach { (from, to) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(from.name, modifier = Modifier.weight(1f))
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, "To")
                    Text(to.name, modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        onChanged(setting.copy(conversion = setting.conversion - from))
                    }) {
                        Icon(Icons.Rounded.Delete, "Delete")
                    }
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            var selectedFrom by remember { mutableStateOf<Codec?>(null) }
            var selectedTo by remember { mutableStateOf<Codec?>(null) }
            val addEnabled = selectedFrom != selectedTo && selectedTo != null && selectedFrom != null
            val audioCodecs = Codec.entries.filter { it.type == TrackType.Audio }
            val fromCodecs = audioCodecs.filterNot { setting.conversion.keys.contains(it) }

            ComboBox(
                "From",
                fromCodecs,
                selectedFrom,
                modifier = Modifier.weight(1f),
                optionName = { name },
                onSelect = { selectedFrom = it })
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, "To")
            ComboBox(
                "To",
                audioCodecs,
                selectedTo,
                modifier = Modifier.weight(1f),
                optionName = { name },
                onSelect = { selectedTo = it })
            FilledTonalButton(
                modifier = Modifier.padding(start = 4.dp),
                enabled = addEnabled,
                onClick = {
                    onChanged(setting.copy(conversion = setting.conversion + mapOf(selectedFrom!! to selectedTo!!)))
                    selectedTo = null
                    selectedFrom = null
                }) {
                Text("Add")
            }
        }
    }

}