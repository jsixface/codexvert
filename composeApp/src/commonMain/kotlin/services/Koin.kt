package services

import getClientConfig
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.resources.Resources
import io.ktor.serialization.kotlinx.cbor.cbor
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import org.koin.dsl.module
import viewmodels.BackupScreenViewModel
import viewmodels.JobsScreenModel
import viewmodels.SettingsScreenModel
import viewmodels.VideoListViewModel

object Koin {
    val services = module {
        single { createHttpClient(enableNetworkLogs = true) }

        factory { SettingsScreenModel(client = get()) }
        factory { JobsScreenModel(client = get()) }
        factory { BackupScreenViewModel(client = get()) }
        factory { VideoListViewModel(client = get()) }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun createHttpClient(enableNetworkLogs: Boolean) = HttpClient {
        install(ContentNegotiation) {
            cbor(Cbor {
                ignoreUnknownKeys = true
            })
        }
        install(Resources)
        defaultRequest {
            url(getClientConfig().backendHost)
        }
        if (enableNetworkLogs) {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.NONE
            }
        }
    }
}