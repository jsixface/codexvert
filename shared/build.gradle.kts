@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    wasmJs {
       browser()
    }
    
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            implementation(ktorLibs.resources)
            implementation(libs.kotlinx.datetime)
        }
    }
}

