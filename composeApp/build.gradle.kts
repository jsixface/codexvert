import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "composeApp"
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        val output = binaries.executable()
        output.forEach {
            println(
                """
                ---
                ${it.name} -> name
                ${it.mainFileName.get()} -> mailfilename
                ${it.target.outputModuleName} -> outputModule name
                ${it.mainFile.get().asFile.name} -> mailFile name
                ${it.distribution.outputDirectory.get()} -> distributionOutputDirectory
                """.trimIndent()
            )
        }
    }
    
    jvm("desktop")
    
    sourceSets {
        val desktopMain by getting
        val wasmJsMain by getting
        
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.material3AdaptiveNavigationSuite)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.bundles.material3.adaptive)
            implementation(projects.shared)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.napier)
            implementation(libs.bundles.koin.client)

            // Ktor
            implementation(ktorLibs.client.core)
            implementation(ktorLibs.client.logging)
            implementation(ktorLibs.client.contentNegotiation)
            implementation(ktorLibs.client.resources)
            implementation(ktorLibs.serialization.kotlinx.cbor)

            implementation(kotlin("reflect"))
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(ktorLibs.client.cio)
            implementation(libs.logback)
        }
        wasmJsMain.dependencies {
            implementation(ktorLibs.client.js)
        }
    }
}

compose.desktop {
    application {
        mainClass = "DesktopMain"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "io.github.jsixface"
            packageVersion = "1.0.0"
        }
    }
}