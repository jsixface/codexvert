package ui.utils

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ComboBox(
    title: String,
    options: List<T>,
    selected: T?,
    optionName: T.() -> String = { toString() },
    onSelect: (T?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.padding(8.dp, 0.dp, 0.dp, 0.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor(),
            value = selected?.optionName() ?: "",
            onValueChange = { },
            readOnly = true,
            label = { Text(title) },
            trailingIcon = { Icon(Icons.Rounded.ArrowDropDown, contentDescription = "Select") },
        )

        if (options.isNotEmpty()) {
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text("None") },
                    onClick = {
                        onSelect(null)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.optionName()) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }
}
