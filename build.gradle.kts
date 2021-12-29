import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    application
    kotlin("jvm") version Kotlin.version
    id(Spotless.spotless) version Spotless.version
    id(Shadow.shadow) version Shadow.version
}

buildscript {
    repositories {
        mavenCentral()
    }
}

apply {
    plugin(Spotless.spotless)
}

repositories {
    mavenCentral()
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
    maven("https://jitpack.io")
}

application {
    applicationName = "dp-regel-api-arena-adapter"
    mainClassName = "no.nav.dagpenger.regel.api.arena.adapter.RegelApiArenaAdapterKt"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> { kotlinOptions.jvmTarget = "1.8" }

dependencies {
    implementation(kotlin("stdlib"))

    implementation(Ktor.server)
    implementation(Ktor.serverNetty)
    implementation(Ktor.auth)
    implementation(Ktor.authJwt) {
        exclude(group = "junit")
    }
    implementation(Ktor.micrometerMetrics)
    implementation(Dagpenger.Biblioteker.Ktor.Server.apiKeyAuth)
    implementation(Micrometer.prometheusRegistry)

    implementation(Moshi.moshi)
    implementation(Moshi.moshiAdapters)
    implementation(Moshi.moshiKotlin)
    implementation(Moshi.moshiKtor)

    implementation(Fuel.fuel)
    implementation(Fuel.fuelMoshi)
    implementation(Konfig.konfig)

    implementation(Log4j2.api)
    implementation(Log4j2.core)
    implementation(Log4j2.slf4j)
    implementation(Log4j2.library("layout-template-json"))
    implementation(Kotlin.Logging.kotlinLogging)

    implementation(Prometheus.common)
    implementation(Prometheus.hotspot)
    implementation(Prometheus.log4j2)

    implementation(Ulid.ulid)

    // unleash
    implementation("no.finn.unleash:unleash-client-java:3.2.9")

    testImplementation(kotlin("test"))
    testImplementation(Ktor.ktorTest) {
        // https://youtrack.jetbrains.com/issue/KT-46090
        exclude("org.jetbrains.kotlin", "kotlin-test-junit")
    }
    testImplementation(Junit5.api)
    testImplementation(KoTest.assertions)
    testImplementation(KoTest.runner)
    testImplementation(TestContainers.postgresql)
    testImplementation(TestContainers.kafka)
    testImplementation(Kafka.streamTestUtils)
    testImplementation(Wiremock.standalone)
    testImplementation(JsonAssert.jsonassert)

    testImplementation(Mockk.mockk)

    testRuntimeOnly(Junit5.engine)
}

spotless {
    kotlin {
        ktlint(Ktlint.version)
    }
    kotlinGradle {
        target("*.gradle.kts", "buildSrc/**/*.kt*")
        ktlint(Ktlint.version)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showExceptions = true
        showStackTraces = true
        exceptionFormat = TestExceptionFormat.FULL
        events = setOf(TestLogEvent.STANDARD_OUT, TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        showStandardStreams = false
    }
}

tasks.named("shadowJar") {
    dependsOn("test")
}

tasks.named("compileKotlin") {
    dependsOn("spotlessCheck")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    transform(com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer::class.java)
}
