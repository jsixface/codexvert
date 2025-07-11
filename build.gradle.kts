plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader

    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(ktorLibs.plugins.ktor) apply false
}