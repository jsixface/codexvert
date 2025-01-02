plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ktor)
    application
}

group = "io.github.jsixface"
version = "1.0.0"
application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.logstash.logback.encoder)
    implementation(libs.bundles.ktor.server)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.json)
    implementation(libs.bundles.koin.server)

//    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.bundles.koin.test)
}

val staticDir = "$projectDir/static"

val copyDevToStatic by tasks.registering {
    dependsOn(":composeApp:wasmJsBrowserDevelopmentExecutableDistribution")
    doLast {
        val webArtifactsDir = "$rootDir/composeApp/build/dist/wasmJs/developmentExecutable"
        delete(staticDir)
        copy {
            from(webArtifactsDir)
            into(staticDir)
        }
    }
}

val copyProdToStatic by tasks.registering {
    dependsOn(":composeApp:wasmJsBrowserDistribution")
    doLast {
        val webArtifactsDir = "$rootDir/composeApp/build/dist/wasmJs/productionExecutable"
        delete(staticDir)
        copy {
            from(webArtifactsDir)
            into(staticDir)
        }
    }
}

tasks.named("run") { dependsOn(copyDevToStatic) }
tasks.named("runFatJar") { dependsOn(copyDevToStatic) }
tasks.named("startScripts") { dependsOn(copyProdToStatic) }
tasks.named("shadowJar") { dependsOn(copyProdToStatic) }
