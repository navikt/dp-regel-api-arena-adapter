package no.nav.dagpenger.regel.api.arena.adapter

import com.auth0.jwk.JwkProvider
import io.ktor.application.Application
import io.mockk.mockk
import no.nav.dagpenger.regel.api.internal.InntektApiInntjeningsperiodeHttpClient
import no.nav.dagpenger.regel.api.internal.SynchronousSubsumsjonClient

fun Application.mockedRegelApiAdapter(
    jwtIssuer: String = "test issuer",
    jwkProvider: JwkProvider = mockk(),
    inntektApiBeregningsdatoHttpClient: InntektApiInntjeningsperiodeHttpClient = mockk(),
    synchronousSubsumsjonClient: SynchronousSubsumsjonClient = mockk()
) {
    return regelApiAdapter(
        jwtIssuer = jwtIssuer,
        jwkProvider = jwkProvider,
        inntektApiBeregningsdatoHttpClient = inntektApiBeregningsdatoHttpClient,
        synchronousSubsumsjonClient = synchronousSubsumsjonClient
    )
}
