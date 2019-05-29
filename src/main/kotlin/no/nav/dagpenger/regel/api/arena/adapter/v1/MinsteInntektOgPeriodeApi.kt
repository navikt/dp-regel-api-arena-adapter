package no.nav.dagpenger.regel.api.arena.adapter.v1

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.Inntekt
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.InntektsPeriode
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektOgPeriodeParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektOgPeriodeRegelfaktum
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektOgPeriodeResultat
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektOgPeriodeSubsumsjon
import no.nav.dagpenger.regel.api.internal.minsteinntekt.SynchronousMinsteinntekt
import no.nav.dagpenger.regel.api.internal.models.MinsteinntektFaktum
import no.nav.dagpenger.regel.api.internal.models.MinsteinntektSubsumsjon
import no.nav.dagpenger.regel.api.internal.models.PeriodeFaktum
import no.nav.dagpenger.regel.api.internal.models.PeriodeSubsumsjon
import no.nav.dagpenger.regel.api.internal.periode.SynchronousPeriode

fun Route.MinsteinntektOgPeriodeApi(
    synchronousMinsteinntekt: SynchronousMinsteinntekt,
    synchronousPeriode: SynchronousPeriode
) {

    route("/minsteinntekt") {
        post {
            val parametere = call.receive<MinsteinntektOgPeriodeParametere>()

            validateParameters(parametere)

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

fun validateParameters(parameters: MinsteinntektOgPeriodeParametere) {
    parameters.bruktInntektsPeriode?.let {
        if (it.foersteMaaned.isAfter(it.sisteMaaned)) throw InvalidInnteksperiodeException(
            "Feil bruktInntektsPeriode: foersteMaaned=${it.foersteMaaned} er etter sisteMaaned=${it.sisteMaaned}"
        )
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
            if (minsteinntektFaktum.bruktInntektsPeriode != null) InntektsPeriode(
                minsteinntektFaktum.bruktInntektsPeriode.førsteMåned,
                minsteinntektFaktum.bruktInntektsPeriode.sisteMåned
            ) else null
        ),
        MinsteinntektOgPeriodeResultat(
            minsteinntektSubsumsjon.resultat.oppfyllerKravTilMinsteArbeidsinntekt,
            periodeSubsumsjon.resultat.antallUker
        ),
        minsteinntektSubsumsjon.inntekt.map {
            Inntekt(
                it.inntekt,
                it.periode,
                InntektsPeriode(
                    it.inntektsPeriode.førsteMåned,
                    it.inntektsPeriode.sisteMåned
                ),
                it.inneholderFangstOgFisk,
                it.andel
            )
        }.toSet(),
        minsteinntektFaktum.inntektManueltRedigert,
        minsteinntektFaktum.inntektAvvik
    )
}

fun compareFields(minsteinntektFaktum: MinsteinntektFaktum, periodeFaktum: PeriodeFaktum): Boolean {

    if (minsteinntektFaktum.aktorId.equals(periodeFaktum.aktorId) &&
        minsteinntektFaktum.beregningsdato.equals(periodeFaktum.beregningsdato) &&
        minsteinntektFaktum.vedtakId.equals(periodeFaktum.vedtakId) &&
        minsteinntektFaktum.harAvtjentVerneplikt == periodeFaktum.harAvtjentVerneplikt &&
        minsteinntektFaktum.oppfyllerKravTilFangstOgFisk == periodeFaktum.oppfyllerKravTilFangstOgFisk &&
        minsteinntektFaktum.bruktInntektsPeriode?.equals(periodeFaktum.bruktInntektsPeriode) ?: (periodeFaktum.bruktInntektsPeriode === null)
    ) {
        return true
    }
    return false
}
