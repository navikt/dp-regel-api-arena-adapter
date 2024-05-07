plugins {
    id("common")
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
    maven("https://jitpack.io")
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

val moshiVersion = "1.15.1"
val fuelVersion = "2.2.1"
val log4j2Versjon = "2.23.1"
val prometheusVersion = "0.16.0"
val kafkaVersion = "7.6.1-ce"
val ktorVersion = "2.3.10"
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

    implementation(libs.jackson.core)
    implementation(libs.jackson.kotlin)
    implementation(libs.jackson.datatype.jsr310)

    implementation("commons-codec:commons-codec:1.17.0")
    implementation(libs.micrometer.registry.prometheus)

    implementation("com.squareup.moshi:moshi:$moshiVersion")
    implementation("com.squareup.moshi:moshi-adapters:$moshiVersion")
    implementation("com.squareup.moshi:moshi-kotlin:$moshiVersion")

    implementation("com.github.kittinunf.fuel:fuel:$fuelVersion")
    implementation("com.github.kittinunf.fuel:fuel-moshi:$fuelVersion")

    implementation(libs.konfig)

    implementation("org.apache.logging.log4j:log4j-api:$log4j2Versjon")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:$log4j2Versjon")
    implementation("org.apache.logging.log4j:log4j-layout-template-json:$log4j2Versjon")
    implementation("org.apache.logging.log4j:log4j-core:$log4j2Versjon")
    implementation("org.apache.logging.log4j:log4j-layout-template-json:$log4j2Versjon")

    implementation("org.slf4j:slf4j-api:2.0.13")

    implementation(libs.kotlin.logging)

    implementation("io.prometheus:simpleclient_common:$prometheusVersion")
    implementation("io.prometheus:simpleclient_hotspot:$prometheusVersion")
    implementation("io.prometheus:simpleclient_log4j2:$prometheusVersion")
    implementation(libs.dp.biblioteker.oauth2.klient)

    implementation("de.huxhorn.sulky:de.huxhorn.sulky.ulid:8.3.0")

    // unleash
    implementation("io.getunleash:unleash-client-java:9.2.1")

    testImplementation(kotlin("test"))
    testImplementation(libs.ktor.server.test.host)
    testImplementation("org.junit.jupiter:junit-jupiter-api:${libs.versions.junit.get()}")

    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.assertions.json)
    testImplementation("io.kotest:kotest-runner-junit5-jvm:${libs.versions.kotest.get()}")

    testImplementation(libs.testcontainer.postgresql)
    testImplementation("org.testcontainers:kafka:${libs.versions.testcontainer.get()}")

    testImplementation("org.apache.kafka:kafka-clients:$kafkaVersion")
    testImplementation("org.apache.kafka:kafka-streams-test-utils:$kafkaVersion")
    testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
    testImplementation("org.skyscreamer:jsonassert:1.5.1")

    testImplementation(libs.mockk)

    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:${libs.versions.junit.get()}")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    mergeServiceFiles()
    transform(com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer::class.java)
}
