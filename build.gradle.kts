plugins {
    id("common")
    application
    alias(libs.plugins.shadow.jar)
}

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
}

application {
    applicationName = "dp-regel-api-arena-adapter"
    mainClass.set("no.nav.dagpenger.regel.api.arena.adapter.RegelApiArenaAdapterKt")
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Multi-Release"] = "true" // https://github.com/johnrengelman/shadow/issues/449
    }
}

val prometheusVersion = "0.16.0"
val kafkaVersion = "8.1.1-ce"
val ktorVersion = "3.3.3"
dependencies {
    implementation(kotlin("stdlib"))

    implementation("io.ktor:ktor-server-netty:$ktorVersion")

    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation("io.ktor:ktor-server-default-headers:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.jackson)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.metrics.micrometer)

    implementation(libs.bundles.jackson)
    implementation(libs.bundles.ktor.client)

    implementation("commons-codec:commons-codec:1.20.0")
    implementation("io.micrometer:micrometer-registry-prometheus:1.16.1")

    implementation(libs.konfig)

    implementation("ch.qos.logback:logback-classic:1.5.24")
    implementation("net.logstash.logback:logstash-logback-encoder:9.0")
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation(libs.kotlin.logging)

    implementation("io.prometheus:simpleclient_common:$prometheusVersion")
    implementation("io.prometheus:simpleclient_hotspot:$prometheusVersion")
    implementation("io.prometheus:simpleclient_log4j2:$prometheusVersion")
    implementation("no.nav.dagpenger:oauth2-klient:2025.12.19-08.15.2e150cd55270")

    implementation("de.huxhorn.sulky:de.huxhorn.sulky.ulid:8.3.0")

    testImplementation(kotlin("test"))
    testImplementation(libs.ktor.server.test.host)
    testImplementation("org.junit.jupiter:junit-jupiter-api:${libs.versions.junit.get()}")

    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.assertions.json)
    testImplementation("io.kotest:kotest-runner-junit5-jvm:${libs.versions.kotest.get()}")
    testImplementation("io.kotest:kotest-property:${libs.versions.kotest.get()}")

    testImplementation(libs.testcontainer.postgresql)
    testImplementation("org.testcontainers:testcontainers-kafka:${libs.versions.testcontainer.get()}")

    testImplementation("org.apache.kafka:kafka-clients:$kafkaVersion")
    testImplementation("org.apache.kafka:kafka-streams-test-utils:$kafkaVersion")
    testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
    testImplementation("org.skyscreamer:jsonassert:1.5.3")

    testImplementation(libs.mockk)

    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:${libs.versions.junit.get()}")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    mergeServiceFiles()
}
