package no.nav.dagpenger.regel.api.arena.adapter

import com.auth0.jwk.JwkProvider
import io.ktor.server.application.Application
import io.mockk.mockk
import io.prometheus.metrics.model.registry.PrometheusRegistry
import no.nav.dagpenger.regel.api.internal.InntektApiInntjeningsperiodeHttpClient
import no.nav.dagpenger.regel.api.internal.RegelApiNyVurderingHttpClient
import no.nav.dagpenger.regel.api.internal.SynchronousSubsumsjonClient

internal fun Application.mockedRegelApiAdapter(
    jwtIssuer: String = "test issuer",
    jwkProvider: JwkProvider = mockk(),
    inntektApiBeregningsdatoHttpClient: InntektApiInntjeningsperiodeHttpClient = mockk(),
    nyVurderingHttpClient: RegelApiNyVurderingHttpClient = mockk(),
    synchronousSubsumsjonClient: SynchronousSubsumsjonClient = mockk(),
) = regelApiAdapter(
    jwtIssuer = jwtIssuer,
    jwkProvider = jwkProvider,
    inntektApiBeregningsdatoHttpClient = inntektApiBeregningsdatoHttpClient,
    kreverRebergningClient = nyVurderingHttpClient,
    synchronousSubsumsjonClient = synchronousSubsumsjonClient,
    collectorRegistry = PrometheusRegistry(),
)
