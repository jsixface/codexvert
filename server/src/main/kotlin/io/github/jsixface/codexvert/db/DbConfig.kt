package io.github.jsixface.codexvert.db

data class DbConfig(
    val type: String,
    val url: String,
    val username: String,
    val password: String,
)
