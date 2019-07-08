import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    application
    kotlin("jvm") version Kotlin.version
    id(Spotless.spotless) version Spotless.version
    id(Shadow.shadow) version Shadow.version
}

buildscript {
    repositories {
        jcenter()
    }
}

apply {
    plugin(Spotless.spotless)
}

repositories {
    jcenter()
    maven("http://packages.confluent.io/maven/")
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

val ktorVersion = "1.2.0"
val fuelVersion = "2.1.0"
val kotlinLoggingVersion = "1.6.22"
val log4j2Version = "2.11.1"
val jupiterVersion = "5.3.2"
val moshiVersion = "1.8.0"
val ktorMoshiVersion = "1.0.1"
val cucumberVersion = "4.0.0"
val mockkVersion = "1.9.3"
val konfigVersion = "1.6.10.0"
val prometheusVersion = "0.6.0"
val dpBibliotekerVersion = "2019.06.19-09.38.5466af242e44"

dependencies {
    implementation(kotlin("stdlib"))

    implementation(Ktor.server)
    implementation(Ktor.serverNetty)
    implementation(Ktor.auth)
    implementation(Ktor.authJwt) {
        exclude(group = "junit")
    }
    implementation(Ktor.micrometerMetrics)
    implementation(Dagpenger.Biblioteker.ktorUtils)
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
    implementation(Log4j2.Logstash.logstashLayout)
    implementation(Kotlin.Logging.kotlinLogging)

    implementation(Prometheus.common)
    implementation(Prometheus.hotspot)
    implementation(Prometheus.log4j2)

    implementation(Ulid.ulid)

    testImplementation(kotlin("test"))
    testImplementation(Cucumber.java8)
    testImplementation(Cucumber.junit)
    testImplementation(Ktor.ktorTest)
    testImplementation(Junit5.api)
    testImplementation(Junit5.kotlinRunner)
    testImplementation(TestContainers.postgresql)
    testImplementation(TestContainers.kafka)
    testImplementation(Kafka.streamTestUtils)
    testImplementation(Wiremock.standalone)
    testImplementation(JsonAssert.jsonassert)

    testImplementation(Mockk.mockk)

    testRuntimeOnly(Junit5.engine)
    testRuntimeOnly(Junit5.vintageEngine)
}

spotless {
    kotlin {
        ktlint(Klint.version)
    }
    kotlinGradle {
        target("*.gradle.kts", "additionalScripts/*.gradle.kts")
        ktlint(Klint.version)
    }
}

sourceSets {

    create("uat") {
        withConvention(KotlinSourceSet::class) {
            java.srcDir(file("src/uatTests/kotlin"))
            resources.srcDir(file("src/uatTests/resources"))
            compileClasspath += sourceSets.main.get().output + configurations["testRuntimeClasspath"] + configurations["runtimeClasspath"]
            runtimeClasspath += output + compileClasspath
        }
    }
}

configurations["uatCompile"].extendsFrom(configurations["testCompile"])

tasks.register<Test>("uatLocal") {
    description = "Runs the user acceptance tests."
    group = "verification"
    testClassesDirs = sourceSets["uat"].output.classesDirs
    classpath = sourceSets["uat"].runtimeClasspath
    mustRunAfter(tasks["test"])
    useJUnitPlatform()
    testLogging {
        showExceptions = true
        showStackTraces = true
        exceptionFormat = TestExceptionFormat.FULL
        events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    }
}

tasks.register<Test>("uatDev") {
    description = "Runs the user acceptance tests."
    group = "verification"
    testClassesDirs = sourceSets["uat"].output.classesDirs
    classpath = sourceSets["uat"].runtimeClasspath
    mustRunAfter(tasks["test"])
    useJUnitPlatform()
    environment(mapOf("CUCUMBER_ENV" to "dev"))
    testLogging {
        showExceptions = true
        showStackTraces = true
        exceptionFormat = TestExceptionFormat.FULL
        events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showExceptions = true
        showStackTraces = true
        exceptionFormat = TestExceptionFormat.FULL
        events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "5.1.1"
}
