package ui.model

import androidx.compose.runtime.Composable

interface Screen {

    val name: String

    @Composable
    fun icon()

    @Composable
    fun content()
}
