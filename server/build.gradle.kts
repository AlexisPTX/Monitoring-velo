plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
}

group = "alexis.rioc.proje_iot"
version = "1.0.0"
application {
    mainClass.set("alexis.rioc.proje_iot.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.cors)
    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test.junit)

    // Dépendances pour PostgreSQL et Exposed
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.postgresql)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    // Dépendance hash mot de passe
    implementation(libs.jbcrypt)

    // Coroutine Dependencies
    implementation(libs.kotlinx.coroutines.core)
}