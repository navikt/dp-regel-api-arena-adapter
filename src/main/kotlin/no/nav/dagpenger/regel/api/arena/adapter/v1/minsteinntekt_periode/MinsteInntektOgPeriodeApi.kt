package no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt_periode

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt_periode.minsteinntekt.SynchronousMinsteinntekt
import no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt_periode.periode.SynchronousPeriode
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.arena.Inntekt
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.arena.InntektsPeriode
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.arena.MinsteinntektOgPeriodeParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.arena.MinsteinntektOgPeriodeRegelfaktum
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.arena.MinsteinntektOgPeriodeResultat
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.arena.MinsteinntektOgPeriodeSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.digidag.MinsteinntektFaktum
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.digidag.MinsteinntektSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.digidag.PeriodeFaktum
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.digidag.PeriodeSubsumsjon

fun Route.MinsteinntektOgPeriodeApi(
    synchronousMinsteinntekt: SynchronousMinsteinntekt,
    synchronousPeriode: SynchronousPeriode
) {

    route("/minsteinntekt") {
        post {
            val parametere = call.receive<MinsteinntektOgPeriodeParametere>()

            val minsteinntektSubsumsjon = synchronousMinsteinntekt.getMinsteinntektSynchronously(parametere)

            val periodeSubsumsjon = synchronousPeriode.getPeriodeSynchronously(parametere)

            val minsteinntektOgPeriodeSubsumsjon =
                mergeMinsteinntektOgPeriodeSubsumsjon(
                    minsteinntektSubsumsjon,
                    periodeSubsumsjon
                )

            call.respond(HttpStatusCode.OK, minsteinntektOgPeriodeSubsumsjon)
        }
    }
}

fun mergeMinsteinntektOgPeriodeSubsumsjon(
    minsteinntektSubsumsjon: MinsteinntektSubsumsjon,
    periodeSubsumsjon: PeriodeSubsumsjon
): MinsteinntektOgPeriodeSubsumsjon {

    val minsteinntektFaktum = minsteinntektSubsumsjon.faktum
    val periodeFaktum = periodeSubsumsjon.faktum

    if (!compareFields(
            minsteinntektFaktum,
            periodeFaktum
        )
    ) throw UnMatchingFaktumException("Minsteinntekt and periode faktum dont match")

    return MinsteinntektOgPeriodeSubsumsjon(
        minsteinntektSubsumsjon.subsumsjonsId,
        periodeSubsumsjon.subsumsjonsId,
        minsteinntektSubsumsjon.opprettet,
        minsteinntektSubsumsjon.utfort,
        MinsteinntektOgPeriodeRegelfaktum(
            minsteinntektFaktum.aktorId,
            minsteinntektFaktum.vedtakId,
            minsteinntektFaktum.beregningsdato,
            minsteinntektFaktum.inntektsId,
            minsteinntektFaktum.harAvtjentVerneplikt,
            minsteinntektFaktum.oppfyllerKravTilFangstOgFisk,
            if (minsteinntektFaktum.bruktInntektsPeriode != null) InntektsPeriode(minsteinntektFaktum.bruktInntektsPeriode.førsteMåned, minsteinntektFaktum.bruktInntektsPeriode.sisteMåned) else null
        ),
        MinsteinntektOgPeriodeResultat(
            minsteinntektSubsumsjon.resultat.oppfyllerKravTilMinsteArbeidsinntekt,
            periodeSubsumsjon.resultat.antallUker
        ),
        minsteinntektSubsumsjon.inntekt.map { Inntekt(it.inntekt, it.periode, InntektsPeriode(it.inntektsPeriode.førsteMåned, it.inntektsPeriode.sisteMåned), it.inneholderFangstOgFisk, it.andel) }.toSet()
    )
}

fun compareFields(minsteinntektFaktum: MinsteinntektFaktum, periodeFaktum: PeriodeFaktum): Boolean {

    if (minsteinntektFaktum.aktorId.equals(periodeFaktum.aktorId) &&
        minsteinntektFaktum.beregningsdato.equals(periodeFaktum.beregningsdato) &&
        minsteinntektFaktum.vedtakId.equals(periodeFaktum.vedtakId) &&
        minsteinntektFaktum.harAvtjentVerneplikt == periodeFaktum.harAvtjentVerneplikt &&
        minsteinntektFaktum.oppfyllerKravTilFangstOgFisk == periodeFaktum.oppfyllerKravTilFangstOgFisk &&
        minsteinntektFaktum.bruktInntektsPeriode?.equals(periodeFaktum.bruktInntektsPeriode) ?: (periodeFaktum.bruktInntektsPeriode === null)) {
        return true
    }
    return false
}

class UnMatchingFaktumException(override val message: String) : RuntimeException(message)
