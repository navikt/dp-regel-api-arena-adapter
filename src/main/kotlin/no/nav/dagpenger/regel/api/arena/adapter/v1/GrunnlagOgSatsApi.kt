package no.nav.dagpenger.regel.api.arena.adapter.v1

import de.huxhorn.sulky.ulid.ULID
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsReberegningParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.IllegalInntektIdException
import no.nav.dagpenger.regel.api.internal.BehovRequest
import no.nav.dagpenger.regel.api.internal.RegelApi
import no.nav.dagpenger.regel.api.internal.RegelKontekst
import no.nav.dagpenger.regel.api.internal.extractGrunnlagOgSats

private val sikkerlogg = KotlinLogging.logger("tjenestekall.grunnlagOgSatsApi")

internal fun Route.grunnlagOgSatsApi(regelApi: RegelApi) {
    route("/dagpengegrunnlag") {
        post {
            withContext(Dispatchers.IO) {
                val parametere = call.receive<GrunnlagOgSatsParametere>()

                parametere.validate()

                val behovRequest = behovFromParametere(parametere)

                val grunnlagOgSatsSubsumsjon =
                    regelApi.getSubsumsjonSynchronously(behovRequest, ::extractGrunnlagOgSats)

                call.respond(HttpStatusCode.OK, grunnlagOgSatsSubsumsjon)
            }
        }
    }

    route("/dagpengegrunnlag-reberegning") {
        post {
            withContext(Dispatchers.IO) {
                val parametere = call.receive<GrunnlagOgSatsReberegningParametere>()
                try {
                    ULID.parseULID(parametere.inntektsId)
                } catch (e: IllegalArgumentException) {
                    throw IllegalInntektIdException(e)
                }

                val behovRequest = behovFromParametere(parametere)

                val grunnlagOgSatsSubsumsjon =
                    regelApi.getSubsumsjonSynchronously(behovRequest, ::extractGrunnlagOgSats)

                call.respond(HttpStatusCode.OK, grunnlagOgSatsSubsumsjon)
            }
        }
    }
}

fun GrunnlagOgSatsParametere.validate() {
    if (this.oppfyllerKravTilLaerling && this.harAvtjentVerneplikt) {
        throw UgyldigParameterkombinasjonException(
            "harAvtjentVerneplikt og oppfyllerKravTilLaerling kan ikke vaere true samtidig",
        )
    }
    if (this.manueltGrunnlag != null && this.forrigeGrunnlag != null) {
        throw UgyldigParameterkombinasjonException(
            "manueltGrunnlag og forrigeGrunnlag kan ikke settes samtidig",
        )
    }
}

fun behovFromParametere(parametere: GrunnlagOgSatsParametere): BehovRequest =
    BehovRequest(
        aktorId = parametere.aktorId,
        vedtakId = parametere.vedtakId,
        regelkontekst = RegelKontekst(id = parametere.vedtakId.toString()),
        beregningsdato = parametere.beregningsdato,
        harAvtjentVerneplikt = parametere.harAvtjentVerneplikt,
        oppfyllerKravTilFangstOgFisk = parametere.oppfyllerKravTilFangstOgFisk,
        manueltGrunnlag = parametere.manueltGrunnlag ?: parametere.grunnlag,
        forrigeGrunnlag = parametere.forrigeGrunnlag,
        antallBarn = parametere.antallBarn,
        lærling = parametere.oppfyllerKravTilLaerling,
        regelverksdato = parametere.regelverksdato,
    ).also {
        withLoggingContext("requestId" to it.requestId) {
            sikkerlogg.info { "Lager behov for $parametere" }
        }
    }

fun behovFromParametere(parametere: GrunnlagOgSatsReberegningParametere): BehovRequest =
    BehovRequest(
        aktorId = parametere.aktorId,
        vedtakId = parametere.vedtakId,
        regelkontekst = RegelKontekst(id = parametere.vedtakId.toString()),
        beregningsdato = parametere.beregningsdato,
        harAvtjentVerneplikt = parametere.harAvtjentVerneplikt,
        oppfyllerKravTilFangstOgFisk = parametere.oppfyllerKravTilFangstOgFisk,
        manueltGrunnlag = parametere.manueltGrunnlag ?: parametere.grunnlag,
        antallBarn = parametere.antallBarn,
        inntektsId = parametere.inntektsId,
        lærling = parametere.oppfyllerKravTilLaerling,
        regelverksdato = parametere.regelverksdato,
    ).also {
        withLoggingContext("requestId" to it.requestId) {
            sikkerlogg.info { "Lager behov for $parametere" }
        }
    }
