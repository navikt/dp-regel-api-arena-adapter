import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    application
    kotlin("jvm") version "1.3.21"
    id("com.diffplug.gradle.spotless") version "3.13.0"
    id("com.github.johnrengelman.shadow") version "4.0.3"
}

buildscript {
    repositories {
        mavenCentral()
    }
}

apply {
    plugin("com.diffplug.gradle.spotless")
}

repositories {
    maven("https://jitpack.io")
    jcenter()
    maven("https://dl.bintray.com/kittinunf/maven")
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

val ktorVersion = "1.1.1"
val fuelVersion = "2.0.1"
val kotlinLoggingVersion = "1.6.22"
val log4j2Version = "2.11.1"
val jupiterVersion = "5.3.2"
val moshiVersion = "1.8.0"
val ktorMoshiVersion = "1.0.1"
val cucumberVersion = "4.0.0"
val mockkVersion = "1.9.1"
val konfigVersion = "1.6.10.0"
val prometheusVersion = "0.6.0"

dependencies {
    implementation(kotlin("stdlib"))

    implementation("io.ktor:ktor-server:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-auth-jwt:$ktorVersion") {
        exclude(group = "junit")
    }

    implementation("com.squareup.moshi:moshi-adapters:$moshiVersion")
    implementation("com.squareup.moshi:moshi-kotlin:$moshiVersion")
    implementation("com.squareup.moshi:moshi:$moshiVersion")
    implementation("com.ryanharter.ktor:ktor-moshi:$ktorMoshiVersion")

    implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")

    implementation("com.github.kittinunf.fuel:fuel:$fuelVersion")
    implementation("com.github.kittinunf.fuel:fuel-moshi:$fuelVersion")
    implementation("com.github.navikt:dp-biblioteker:1.2019.05.15-09.50.8d6053f59a15")

    implementation("com.natpryce:konfig:$konfigVersion")

    implementation("org.apache.logging.log4j:log4j-api:$log4j2Version")
    implementation("org.apache.logging.log4j:log4j-core:$log4j2Version")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4j2Version")
    implementation("com.vlkan.log4j2:log4j2-logstash-layout-fatjar:0.15")

    implementation("io.prometheus:simpleclient_common:$prometheusVersion")
    implementation("io.prometheus:simpleclient_hotspot:$prometheusVersion")
    implementation("io.prometheus:simpleclient_log4j2:$prometheusVersion")

    implementation("de.huxhorn.sulky:de.huxhorn.sulky.ulid:8.2.0")

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("com.squareup.okhttp3:mockwebserver:3.13.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$jupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:$jupiterVersion")
    testImplementation("io.cucumber:cucumber-java8:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-junit:$cucumberVersion")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.0")
    testImplementation("io.mockk:mockk:$mockkVersion")
}

spotless {
    kotlin {
        ktlint()
    }
    kotlinGradle {
        target("*.gradle.kts", "additionalScripts/*.gradle.kts")
        ktlint()
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
