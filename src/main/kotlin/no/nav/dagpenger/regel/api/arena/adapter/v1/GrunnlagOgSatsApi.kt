package no.nav.dagpenger.regel.api.arena.adapter.v1

import de.huxhorn.sulky.ulid.ULID
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsReberegningParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.IllegalInntektIdException
import no.nav.dagpenger.regel.api.internal.BehovRequest
import no.nav.dagpenger.regel.api.internal.SynchronousSubsumsjonClient
import no.nav.dagpenger.regel.api.internal.extractGrunnlagOgSats

private val sikkerlogg = KotlinLogging.logger("tjenestekall.grunnlagOgSatsApi")

internal fun Route.GrunnlagOgSatsApi(
    synchronousSubsumsjonClient: SynchronousSubsumsjonClient
) {

    route("/dagpengegrunnlag") {
        post {
            val parametere = call.receive<GrunnlagOgSatsParametere>()

            parametere.validate()

            val behovRequest = behovFromParametere(parametere)

            val grunnlagOgSatsSubsumsjon =
                synchronousSubsumsjonClient.getSubsumsjonSynchronously(behovRequest, ::extractGrunnlagOgSats)

            call.respond(HttpStatusCode.OK, grunnlagOgSatsSubsumsjon)
        }
    }

    route("/dagpengegrunnlag-reberegning") {
        post {
            val parametere = call.receive<GrunnlagOgSatsReberegningParametere>()
            try {
                ULID.parseULID(parametere.inntektsId)
            } catch (e: IllegalArgumentException) {
                throw IllegalInntektIdException(e)
            }

            val behovRequest = behovFromParametere(parametere)

            val grunnlagOgSatsSubsumsjon =
                synchronousSubsumsjonClient.getSubsumsjonSynchronously(behovRequest, ::extractGrunnlagOgSats)

            call.respond(HttpStatusCode.OK, grunnlagOgSatsSubsumsjon)
        }
    }
}

fun GrunnlagOgSatsParametere.validate() {
    if (this.oppfyllerKravTilLaerling && this.harAvtjentVerneplikt) throw UgyldigParameterkombinasjonException("harAvtjentVerneplikt og oppfyllerKravTilLaerling kan ikke vaere true samtidig")
}

fun behovFromParametere(parametere: GrunnlagOgSatsParametere): BehovRequest {
    return BehovRequest(
        aktorId = parametere.aktorId,
        vedtakId = parametere.vedtakId,
        beregningsdato = parametere.beregningsdato,
        harAvtjentVerneplikt = parametere.harAvtjentVerneplikt,
        oppfyllerKravTilFangstOgFisk = parametere.oppfyllerKravTilFangstOgFisk,
        manueltGrunnlag = parametere.grunnlag,
        antallBarn = parametere.antallBarn,
        lærling = parametere.oppfyllerKravTilLaerling
    ).also {
        withLoggingContext("requestId" to it.requestId) {
            sikkerlogg.info { "Lager behov for $parametere" }
        }
    }
}

fun behovFromParametere(parametere: GrunnlagOgSatsReberegningParametere): BehovRequest {
    return BehovRequest(
        aktorId = parametere.aktorId,
        vedtakId = parametere.vedtakId,
        beregningsdato = parametere.beregningsdato,
        harAvtjentVerneplikt = parametere.harAvtjentVerneplikt,
        oppfyllerKravTilFangstOgFisk = parametere.oppfyllerKravTilFangstOgFisk,
        manueltGrunnlag = parametere.grunnlag,
        antallBarn = parametere.antallBarn,
        inntektsId = parametere.inntektsId,
        lærling = parametere.oppfyllerKravTilLaerling
    ).also {
        withLoggingContext("requestId" to it.requestId) {
            sikkerlogg.info { "Lager behov for $parametere" }
        }
    }
}
