import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("application")
    kotlin("jvm") version "1.3.10"
    id("com.diffplug.gradle.spotless") version "3.13.0"
    id("com.github.johnrengelman.shadow") version "4.0.3"
}

apply {
    plugin("com.diffplug.gradle.spotless")
}

repositories {
    jcenter()
    maven("https://dl.bintray.com/kittinunf/maven")
}

application {
    applicationName = "dp-regel-api"
    mainClassName = "no.nav.dagpenger.regel.Api"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

val ktorVersion = "1.1.1"
val fuelVersion = "1.15.0"
val kotlinLoggingVersion = "1.4.9"
val log4j2Version = "2.11.1"
val jupiterVersion = "5.3.2"

dependencies {
    implementation(kotlin("stdlib"))

    implementation("io.ktor:ktor-server:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-gson:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-locations:$ktorVersion")

    implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")

    implementation("com.github.kittinunf.fuel:fuel:$fuelVersion")
    implementation("com.github.kittinunf.fuel:fuel-gson:$fuelVersion")

    implementation("org.apache.logging.log4j:log4j-api:$log4j2Version")
    implementation("org.apache.logging.log4j:log4j-core:$log4j2Version")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4j2Version")
    implementation("com.vlkan.log4j2:log4j2-logstash-layout-fatjar:0.15")

    implementation("de.nielsfalk.ktor:ktor-swagger:0.4.0")

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$jupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
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
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}