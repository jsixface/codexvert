import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
       browser()
    }
    
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.resources)
            implementation(libs.kotlinx.datetime)
        }
    }
}

