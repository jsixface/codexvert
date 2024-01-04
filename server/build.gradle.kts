plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
}

group = "io.github.jsixface"
version = "1.0.0"
application {
    mainClass.set("io.ktor.server.netty.EngineMain")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["development"] ?: "false"}")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.bundles.ktor.server)
    implementation(libs.bundles.koin.server)

    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.bundles.koin.test)
}


jib {
    to {
        image = "registry.gitlab.com/jsixface/codexvert"
        tags = setOf(project.version.toString())
    }

    extraDirectories {
        paths {
            path {
                setFrom(file("$projectDir/static"))
                into = "/static"
            }
        }
    }
}

tasks {
    val webArtifactsDir = "$rootDir/composeApp/build/dist/wasmJs/productionExecutable"
    val staticDir = "$projectDir/static"

    named("processResources") {
        dependsOn(":composeApp:wasmJsBrowserDistribution")
        doLast {
            copy {
                from(webArtifactsDir)
                into(staticDir)
            }
        }
    }
}