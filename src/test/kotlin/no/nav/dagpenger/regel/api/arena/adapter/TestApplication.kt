package no.nav.dagpenger.regel.api.arena.adapter

import com.auth0.jwk.JwkProvider
import io.ktor.application.Application
import io.mockk.mockk
import no.nav.dagpenger.regel.api.internal.grunnlag.SynchronousGrunnlag
import no.nav.dagpenger.regel.api.internal.inntjeningsperiode.InntektApiInntjeningsperiodeHttpClient
import no.nav.dagpenger.regel.api.internal.minsteinntekt.SynchronousMinsteinntekt
import no.nav.dagpenger.regel.api.internal.periode.SynchronousPeriode
import no.nav.dagpenger.regel.api.internal.sats.SynchronousSats
import no.nav.dagpenger.regel.api.internalV2.SynchronousSubsumsjonClient

fun Application.mockedRegelApiAdapter(
    jwtIssuer: String = "test issuer",
    jwkProvider: JwkProvider = mockk(),
    synchronousMinsteinntekt: SynchronousMinsteinntekt = mockk(),
    synchronousPeriode: SynchronousPeriode = mockk(),
    synchronousGrunnlag: SynchronousGrunnlag = mockk(),
    synchronousSats: SynchronousSats = mockk(),
    inntektApiBeregningsdatoHttpClient: InntektApiInntjeningsperiodeHttpClient = mockk(),
    synchronousSubsumsjonClient: SynchronousSubsumsjonClient = mockk()
) {
    return regelApiAdapter(
        jwtIssuer,
        jwkProvider,
        synchronousMinsteinntekt,
        synchronousPeriode,
        synchronousGrunnlag,
        synchronousSats,
        inntektApiBeregningsdatoHttpClient,
        synchronousSubsumsjonClient
    )
}
