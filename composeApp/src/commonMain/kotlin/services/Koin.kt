package services

import Backend
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.resources.*
import io.ktor.serialization.kotlinx.cbor.*
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
            url(Backend.host)
        }
        if (enableNetworkLogs) {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.NONE
            }
        }
    }
}