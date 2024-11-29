package no.nav.dagpenger.regel.api.internal

import io.prometheus.client.Summary

const val CLIENT_LATENCY_SECONDS_METRIC_NAME = "regel_client_seconds"
val clientLatencyStats: Summary =
    Summary
        .build()
        .name(CLIENT_LATENCY_SECONDS_METRIC_NAME)
        .quantile(0.5, 0.05) // Add 50th percentile (= median) with 5% tolerated error
        .quantile(0.99, 0.001) // Add 99th percentile with 0.1% tolerated error
        .help("Latency arena-adapter regel client, in seconds")
        .labelNames("operation")
        .register()
