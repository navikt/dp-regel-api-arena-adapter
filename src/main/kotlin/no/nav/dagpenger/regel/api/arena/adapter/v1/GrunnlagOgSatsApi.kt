package no.nav.dagpenger.regel.api.arena.adapter.v1

import com.github.spoptchev.scientist.conduct
import com.github.spoptchev.scientist.scientist
import de.huxhorn.sulky.ulid.ULID
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.Grunnlag
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagBeregningsregel
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsReberegningParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsRegelFaktum
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsResultat
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.IllegalInntektIdException
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.Sats
import no.nav.dagpenger.regel.api.internal.BehovRequest
import no.nav.dagpenger.regel.api.internal.SynchronousSubsumsjonClient
import no.nav.dagpenger.regel.api.internal.extractGrunnlagOgSats
import java.time.LocalDate
import java.time.LocalDateTime

fun Route.GrunnlagOgSatsApi(
    synchronousSubsumsjonClient: SynchronousSubsumsjonClient
) {

    route("/dagpengegrunnlag") {
        post {
            val parametere = call.receive<GrunnlagOgSatsParametere>()

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

            val currentCodePath: () -> GrunnlagOgSatsSubsumsjon = ::statiskSvar
            val newCodePath: () -> GrunnlagOgSatsSubsumsjon = {
                runBlocking { dynamiskSvar(parametere, synchronousSubsumsjonClient) }
            }

            val result = scientist<GrunnlagOgSatsSubsumsjon, Unit> {
                publisher { result -> println(result) }
            } conduct {
                experiment { "reberegning" }
                control { currentCodePath() }
                candidate { newCodePath() }
            }

            call.respond(HttpStatusCode.OK, result)
        }
    }
}

private fun statiskSvar(): GrunnlagOgSatsSubsumsjon {
    return GrunnlagOgSatsSubsumsjon(
        grunnlagSubsumsjonsId = ULID().nextULID(),
        satsSubsumsjonsId = ULID().nextULID(),
        opprettet = LocalDateTime.now(),
        utfort = LocalDateTime.now(),
        parametere = GrunnlagOgSatsRegelFaktum(
            aktorId = "1234",
            vedtakId = 1234,
            beregningsdato = LocalDate.now(),
            inntektsId = ULID().nextULID(),
            antallBarn = 0,
            grunnlag = 0
        ),
        resultat = GrunnlagOgSatsResultat(
            grunnlag = Grunnlag(
                avkortet = 100,
                uavkortet = 100
            ),
            sats = Sats(
                dagsats = 1,
                ukesats = 1
            ),
            benyttet90ProsentRegel = false,
            beregningsRegel = GrunnlagBeregningsregel.ORDINAER_ETTAAR
        )
    )
}

private suspend fun dynamiskSvar(
    parametere: GrunnlagOgSatsReberegningParametere,
    synchronousSubsumsjonClient: SynchronousSubsumsjonClient
): GrunnlagOgSatsSubsumsjon {
    val behovRequest = behovFromParametere(parametere)

    return synchronousSubsumsjonClient.getSubsumsjonSynchronously(behovRequest, ::extractGrunnlagOgSats)
}

fun behovFromParametere(parametere: GrunnlagOgSatsParametere): BehovRequest {
    return BehovRequest(
        aktorId = parametere.aktorId,
        vedtakId = parametere.vedtakId,
        beregningsdato = parametere.beregningsdato,
        harAvtjentVerneplikt = parametere.harAvtjentVerneplikt,
        oppfyllerKravTilFangstOgFisk = parametere.oppfyllerKravTilFangstOgFisk,
        manueltGrunnlag = parametere.grunnlag,
        antallBarn = parametere.antallBarn
    )
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
        inntektsId = parametere.inntektsId
    )
}
