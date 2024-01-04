package io.github.jsixface.codexvert.plugins

import io.ktor.serialization.kotlinx.cbor.cbor
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor

@OptIn(ExperimentalSerializationApi::class)
fun Application.configureHTTP() {

    install(ContentNegotiation) {
        cbor(Cbor {
            ignoreUnknownKeys = true
        })

    }
}
