package no.nav.dagpenger.regel.api.arena.adapter

import com.auth0.jwk.JwkProvider
import io.ktor.server.application.Application
import io.mockk.mockk
import io.prometheus.metrics.model.registry.PrometheusRegistry
import no.nav.dagpenger.regel.api.internal.InntektApiInntjeningsperiodeHttpClient
import no.nav.dagpenger.regel.api.internal.RegelApi

internal fun Application.mockedRegelApiAdapter(
    jwtIssuer: String = "test issuer",
    jwkProvider: JwkProvider = mockk(),
    inntektApiBeregningsdatoHttpClient: InntektApiInntjeningsperiodeHttpClient = mockk(),
    regelApi: RegelApi = mockk(),
) = regelApiAdapter(
    jwtIssuer = jwtIssuer,
    jwkProvider = jwkProvider,
    inntektApiBeregningsdatoHttpClient = inntektApiBeregningsdatoHttpClient,
    regelApi = regelApi,
    collectorRegistry = PrometheusRegistry(),
)
