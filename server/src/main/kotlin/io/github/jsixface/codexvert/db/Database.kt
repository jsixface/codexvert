package io.github.jsixface.codexvert.db

import io.ktor.server.application.Application
import io.ktor.server.application.log
import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.jetbrains.exposed.sql.Database
import java.sql.DriverManager

private const val MIGRATION_FILE = "db/changelog/changelog-master.yaml"

fun Application.getDbConfig(): DbConfig {
    val dbType = environment.config.property("database.type").getString().lowercase()
    val url = environment.config.property("database.url").getString()
    val user = environment.config.property("database.user").getString()
    val password = environment.config.property("database.password").getString()

    log.info("Connecting to: $url as $user")
    return DbConfig(dbType, url, user, password)
}


fun Application.getDb() = getDbConfig().run { Database.connect(url, user = username, password = password) }


fun Application.migrateDatabases() {
    val dbConfig = getDbConfig()
    val driverMapping = mutableMapOf(
        "jdbc:postgresql" to "org.postgresql.Driver",
        "jdbc:mysql" to "com.mysql.cj.jdbc.Driver",
        "jdbc:mariadb" to "org.mariadb.jdbc.Driver",
        "jdbc:sqlite" to "org.sqlite.JDBC",
    )
    val driver = driverMapping.entries.firstOrNull { (prefix, _) -> dbConfig.url.startsWith(prefix) }?.value
        ?: error("Database driver not found for ${dbConfig.url}")
    Class.forName(driver)
    DriverManager.getConnection(dbConfig.url, dbConfig.username, dbConfig.password).use { connection ->
        // Run the migrations using Liquibase
        System.setProperty("liquibase.analytics.enabled", "false");
        Liquibase(MIGRATION_FILE, ClassLoaderResourceAccessor(), JdbcConnection(connection)).update("")
    }
}