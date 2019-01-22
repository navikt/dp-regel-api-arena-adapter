package no.nav.dagpenger.regel.api.arena.adapter

import java.math.BigDecimal
import java.net.URI
import java.time.LocalDateTime
import java.util.UUID

class RegelApiDummy : RegelApiClient {

    lateinit var currentRequest: MinsteinntektBeregningsRequest

    override fun startMinsteinntktBeregning(request: MinsteinntektBeregningsRequest): URI {
        return when {
            Scenario1_1Request == request -> {
                currentRequest = request
                URI.create("URN:scenario1_1")
            }
            Scenario1_2Request == request -> {
                currentRequest = request
                URI.create("URN:scenario1_2")
            }
            Scenario1_3Request == request -> {
                currentRequest = request
                URI.create("URN:scenario1_3")
            }
            Scenario2_1_AND_3_Request == request -> {
                currentRequest = request
                URI.create("URN:scenario2_1-3")
            }
            Scenario2_2Request == request -> {
                currentRequest = request
                URI.create("URN:scenario2_2")
            }
            Scenario3_1Request == request -> {
                currentRequest = request
                URI.create("URN:scenario3_1")
            }
            else -> {
                currentRequest = request
                URI.create("URN:nomatch")
            }
        }
    }

    override fun getMinsteinntekt(ressursUrl: URI): MinsteinntektBeregningsResponse {
        return when (ressursUrl) {
            URI.create("URN:scenario1_1") -> MinsteinntektBeregningsResponse(
                UUID.randomUUID().toString(),
                Utfall(
                    false,
                    0
                ),
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                mapRequestToParametere(currentRequest, "A"),
                InntektsPeriode("2018-01", "2018-12"),
                InntektsPeriode("2017-01", "2017-12"),
                InntektsPeriode("2016-01", "2016-12"),
                Inntekt(BigDecimal(50000), BigDecimal(0), BigDecimal(0), inneholderNaeringsinntekter = false)
            )
            URI.create("URN:scenario1_2") -> MinsteinntektBeregningsResponse(
                "M2",
                Utfall(
                    true,
                    52
                ),
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                mapRequestToParametere(currentRequest, "B"),
                InntektsPeriode("2018-02", "2019-01"),
                InntektsPeriode("2017-02", "2018-01"),
                InntektsPeriode("2016-02", "2017-01"),
                Inntekt(BigDecimal(200000), BigDecimal(0), BigDecimal(0), inneholderNaeringsinntekter = false)
            )
            URI.create("URN:scenario1_3") -> MinsteinntektBeregningsResponse(
                UUID.randomUUID().toString(),
                Utfall(
                    true,
                    104
                ),
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                mapRequestToParametere(currentRequest, "B"),
                InntektsPeriode("2018-02", "2019-01"),
                InntektsPeriode("2017-02", "2018-01"),
                InntektsPeriode("2016-02", "2017-01"),
                Inntekt(BigDecimal(500000), BigDecimal(0), BigDecimal(0), inneholderNaeringsinntekter = true)
            )
            URI.create("URN:scenario2_1-3") -> MinsteinntektBeregningsResponse(
                UUID.randomUUID().toString(),
                Utfall(
                    true,
                    52
                ),
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                mapRequestToParametere(currentRequest, "C"),
                InntektsPeriode("2018-01", "2018-12"),
                InntektsPeriode("2017-01", "2017-12"),
                InntektsPeriode("2016-01", "2016-12"),
                Inntekt(BigDecimal(164701), BigDecimal(0), BigDecimal(0), inneholderNaeringsinntekter = false)
            )
            URI.create("URN:scenario2_2") -> MinsteinntektBeregningsResponse(
                UUID.randomUUID().toString(),
                Utfall(
                    false,
                    0
                ),
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                mapRequestToParametere(currentRequest, "D"),
                InntektsPeriode("2018-02", "2019-01"),
                InntektsPeriode("2017-02", "2018-01"),
                InntektsPeriode("2016-02", "2017-01"),
                Inntekt(BigDecimal(100000), BigDecimal(40000), BigDecimal(0), inneholderNaeringsinntekter = false)
            )
            URI.create("URN:scenario3_1") -> MinsteinntektBeregningsResponse(
                UUID.randomUUID().toString(),
                Utfall(
                    false,
                    0
                ),
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                mapRequestToParametere(currentRequest, "J"),
                InntektsPeriode("2018-02", "2019-01"),
                InntektsPeriode("2017-02", "2018-01"),
                InntektsPeriode("2016-02", "2017-01"),
                Inntekt(BigDecimal(100000), BigDecimal(40000), BigDecimal(0), inneholderNaeringsinntekter = false)
            )
            else -> MinsteinntektBeregningsResponse(
                UUID.randomUUID().toString(),
                Utfall(
                    false,
                    0
                ),
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                mapRequestToParametere(currentRequest, "D"),
                InntektsPeriode("2018-02", "2019-01"),
                InntektsPeriode("2017-02", "2018-01"),
                InntektsPeriode("2016-02", "2017-01"),
                Inntekt(BigDecimal(100000), BigDecimal(40000), BigDecimal(0), inneholderNaeringsinntekter = false)
            )
        }
    }

    override fun startGrunnlagBeregning(request: DagpengegrunnlagBeregningsRequest): URI {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun getGrunnlag(ressursUrl: URI): DagpengegrunnlagBeregningsResponse {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun pollTask(taskUrl: URI): TaskPollResponse {
        return TaskPollResponse(TaskResponse(Regel.MINSTEINNTEKT, TaskStatus.DONE, ""), taskUrl)
    }
}

fun mapRequestToParametere(request: MinsteinntektBeregningsRequest, inntektsId: String): Parametere =
    Parametere(
        request.aktorId,
        request.vedtakId,
        request.beregningsdato,
        inntektsId,
        request.bruktinntektsPeriode?.let {
            InntektsPeriode(
                request.bruktinntektsPeriode.foersteMaaned,
                request.bruktinntektsPeriode.sisteMaaned
            )
        },
        request.harAvtjentVerneplikt,
        request.oppfyllerKravTilFangstOgFisk
    )

val Scenario1_1Request = MinsteinntektBeregningsRequest(
    aktorId = "1000033752789",
    vedtakId = 31018297,
    beregningsdato = "2019-01-10",
    harAvtjentVerneplikt = false,
    oppfyllerKravTilFangstOgFisk = false
)

val Scenario1_2Request = Scenario1_1Request.copy(beregningsdato = "2019-02-06")
val Scenario1_3Request = Scenario1_2Request.copy(
    oppfyllerKravTilFangstOgFisk = true
)

val Scenario2_1_AND_3_Request = MinsteinntektBeregningsRequest(
    aktorId = "1000003221752",
    vedtakId = 31018347,
    beregningsdato = "2019-01-11",
    harAvtjentVerneplikt = false,
    oppfyllerKravTilFangstOgFisk = false
)

val Scenario2_2Request = MinsteinntektBeregningsRequest(
    aktorId = "1000003221752",
    vedtakId = 31018347,
    beregningsdato = "2019-02-07",
    harAvtjentVerneplikt = false,
    oppfyllerKravTilFangstOgFisk = false
)

val Scenario3_1Request = MinsteinntektBeregningsRequest(
    aktorId = "1000074474453",
    vedtakId = 31018398,
    beregningsdato = "2019-02-08",
    harAvtjentVerneplikt = false,
    oppfyllerKravTilFangstOgFisk = false
)
