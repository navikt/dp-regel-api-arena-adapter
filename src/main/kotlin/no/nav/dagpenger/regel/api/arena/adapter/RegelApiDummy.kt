package no.nav.dagpenger.regel.api.arena.adapter

import java.math.BigDecimal
import java.net.URI
import java.time.LocalDateTime

class RegelApiDummy: RegelApiClient {

    lateinit var scenario1Request: MinsteinntektBeregningsRequest
    lateinit var scenario2Request: MinsteinntektBeregningsRequest

    override fun startMinsteinntktBeregning(request: MinsteinntektBeregningsRequest): URI {
        return when {
            matchesScenario1_1(request) -> {
                scenario1Request = request
                URI.create("URN:scenario1_1")
            }
            matchesScenario1_2(request) -> {
                scenario1Request = request
                URI.create("URN:scenario1_2")
            }
            matchesScenario1_3(request) -> {
                scenario1Request = request
                URI.create("URN:scenario1_3")
            }
            matchesScenario2_1(request) -> {
                scenario2Request = request
                URI.create("URN:scenario2_1")
            }
            matchesScenario2_2(request) -> {
                scenario2Request = request
                URI.create("URN:scenario2_2")
            }
            else -> URI.create("URN:nomatch")
        }
    }

    override fun getMinsteinntekt(ressursUrl: URI): MinsteinntektBeregningsResponse {
        return when (ressursUrl) {
            URI.create("URN:scenario1_1") -> MinsteinntektBeregningsResponse(
                "M1",
                Utfall(
                    false,
                    0
                ),
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                mapRequestToParametere(scenario1Request, "A"),
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
                mapRequestToParametere(scenario1Request, "B"),
                InntektsPeriode("2018-02", "2019-01"),
                InntektsPeriode("2017-02", "2018-01"),
                InntektsPeriode("2016-02", "2017-01"),
                Inntekt(BigDecimal(200000), BigDecimal(0), BigDecimal(0), inneholderNaeringsinntekter = false)
            )
            URI.create("URN:scenario1_3") -> MinsteinntektBeregningsResponse(
                "M3",
                Utfall(
                    true,
                    104
                ),
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                mapRequestToParametere(scenario1Request, "B"),
                InntektsPeriode("2018-02", "2019-01"),
                InntektsPeriode("2017-02", "2018-01"),
                InntektsPeriode("2016-02", "2017-01"),
                Inntekt(BigDecimal(500000), BigDecimal(0), BigDecimal(0), inneholderNaeringsinntekter = true)
            )
            URI.create("URN:scenario2_1") -> MinsteinntektBeregningsResponse(
                "M4",
                Utfall(
                    true,
                    52
                ),
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                mapRequestToParametere(scenario2Request, "C"),
                InntektsPeriode("2018-01", "2018-12"),
                InntektsPeriode("2017-01", "2017-12"),
                InntektsPeriode("2016-01", "2016-12"),
                Inntekt(BigDecimal(164701), BigDecimal(0), BigDecimal(0), inneholderNaeringsinntekter = false)
            )
            URI.create("URN:scenario2_2") -> MinsteinntektBeregningsResponse(
                "M5",
                Utfall(
                    false,
                    0
                ),
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                mapRequestToParametere(scenario2Request, "D"),
                InntektsPeriode("2018-02", "2019-01"),
                InntektsPeriode("2017-02", "2018-01"),
                InntektsPeriode("2016-02", "2017-01"),
                Inntekt(BigDecimal(100000), BigDecimal(40000), BigDecimal(0), inneholderNaeringsinntekter = false)
            )
            else -> throw RuntimeException("scenario not matched")
        }
    }

    override fun startGrunnlagBeregning(request: DagpengegrunnlagBeregningsRequest): URI {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getGrunnlag(ressursUrl: URI): DagpengegrunnlagBeregningsResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun pollTask(taskUrl: URI): TaskPollResponse {
        return TaskPollResponse(TaskResponse(Regel.MINSTEINNTEKT, TaskStatus.DONE, ""), taskUrl)
    }
}

fun matchesScenario1_1(request: MinsteinntektBeregningsRequest) = request.aktorId == "1000033752789" && request.vedtakId == 31018297 && request.beregningsdato == "2019-01-10"

fun matchesScenario1_2(request: MinsteinntektBeregningsRequest) = request.aktorId == "1000033752789" && request.vedtakId == 31018297 && request.beregningsdato == "2019-02-06" && !request.oppfyllerKravTilFangstOgFisk

fun matchesScenario1_3(request: MinsteinntektBeregningsRequest) = request.aktorId == "1000033752789" && request.vedtakId == 31018297 && request.beregningsdato == "2019-02-06" && request.oppfyllerKravTilFangstOgFisk

fun matchesScenario2_1(request: MinsteinntektBeregningsRequest) = request.aktorId == "1000003221752" && request.vedtakId == 31018347 && request.beregningsdato == "2019-01-11"
fun matchesScenario2_2(request: MinsteinntektBeregningsRequest) = request.aktorId == "1000003221752" && request.vedtakId == 31018347 && request.beregningsdato == "2019-02-07"

fun mapRequestToParametere(request: MinsteinntektBeregningsRequest, inntektsId: String): Parametere =
    Parametere(
        request.aktorId,
        request.vedtakId,
        request.beregningsdato,
        inntektsId,
        request.bruktinntektsPeriode?.let {  InntektsPeriode(request.bruktinntektsPeriode.foersteMaaned, request.bruktinntektsPeriode.sisteMaaned)},
        request.harAvtjentVerneplikt,
        request.oppfyllerKravTilFangstOgFisk
    )
