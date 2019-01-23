package no.nav.dagpenger.regel.api.arena.adapter

import de.huxhorn.sulky.ulid.ULID
import java.net.URI
import java.time.LocalDateTime

class RegelApiDummy : RegelApiClient {

    var ulid = ULID()
    lateinit var currentMinsteinntektBeregningsRequest: MinsteinntektBeregningsRequest
    lateinit var currentGrunnlagRequest: DagpengegrunnlagBeregningsRequest

    override fun startMinsteinntktBeregning(request: MinsteinntektBeregningsRequest): URI {
        return when {
            Scenario1_1Request == request -> {
                currentMinsteinntektBeregningsRequest = request
                URI.create("URN:minsteinntekt:scenario1_1")
            }
            Scenario1_2Request == request -> {
                currentMinsteinntektBeregningsRequest = request
                URI.create("URN:minsteinntekt:scenario1_2")
            }
            Scenario1_3Request == request -> {
                currentMinsteinntektBeregningsRequest = request
                URI.create("URN:minsteinntekt:scenario1_3")
            }
            Scenario2_1_AND_3_Request == request -> {
                currentMinsteinntektBeregningsRequest = request
                URI.create("URN:minsteinntekt:scenario2_1-3")
            }
            Scenario2_2Request == request -> {
                currentMinsteinntektBeregningsRequest = request
                URI.create("URN:minsteinntekt:scenario2_2")
            }
            Scenario3_1Request == request -> {
                currentMinsteinntektBeregningsRequest = request
                URI.create("URN:minsteinntekt:scenario3_1")
            }
            else -> {
                currentMinsteinntektBeregningsRequest = request
                URI.create("URN:minsteinntekt:nomatch")
            }
        }
    }

    override fun getMinsteinntekt(ressursUrl: URI): MinsteinntektBeregningsResponse {
        return when (ressursUrl) {
            URI.create("URN:minsteinntekt:scenario1_1") -> MinsteinntektBeregningsResponse(
                ulid.nextULID(),
                Utfall(
                    false,
                    0
                ),
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                mapRequestToParametere(currentMinsteinntektBeregningsRequest, "01D1XGEK0FNB179YPXB12TPDPT"),
                InntektsPeriode("2018-01", "2018-12"),
                InntektsPeriode("2017-01", "2017-12"),
                InntektsPeriode("2016-01", "2016-12"),
                Inntekt(50000, 0, 0, inneholderNaeringsinntekter = false)
            )
            URI.create("URN:minsteinntekt:scenario1_2") -> MinsteinntektBeregningsResponse(
                ulid.nextULID(),
                Utfall(
                    true,
                    52
                ),
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                mapRequestToParametere(currentMinsteinntektBeregningsRequest, "01D1XGEK15D0B6GDPC74C0ZASV"),
                InntektsPeriode("2018-02", "2019-01"),
                InntektsPeriode("2017-02", "2018-01"),
                InntektsPeriode("2016-02", "2017-01"),
                Inntekt(200000, 0, 0, inneholderNaeringsinntekter = false)
            )
            URI.create("URN:minsteinntekt:scenario1_3") -> MinsteinntektBeregningsResponse(
                ulid.nextULID(),
                Utfall(
                    true,
                    104
                ),
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                mapRequestToParametere(currentMinsteinntektBeregningsRequest, "01D1XGEK1T355GP4FWQ51XP153"),
                InntektsPeriode("2018-02", "2019-01"),
                InntektsPeriode("2017-02", "2018-01"),
                InntektsPeriode("2016-02", "2017-01"),
                Inntekt(500000, 0, 0, inneholderNaeringsinntekter = true)
            )
            URI.create("URN:minsteinntekt:scenario2_1-3") -> MinsteinntektBeregningsResponse(
                ulid.nextULID(),
                Utfall(
                    true,
                    52
                ),
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                mapRequestToParametere(currentMinsteinntektBeregningsRequest, "01D1XGEK2CA0X5BM7PZFYKS5WX"),
                InntektsPeriode("2018-01", "2018-12"),
                InntektsPeriode("2017-01", "2017-12"),
                InntektsPeriode("2016-01", "2016-12"),
                Inntekt(164701, 0, 0, inneholderNaeringsinntekter = false)
            )
            URI.create("URN:minsteinntekt:scenario2_2") -> MinsteinntektBeregningsResponse(
                ulid.nextULID(),
                Utfall(
                    false,
                    0
                ),
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                mapRequestToParametere(currentMinsteinntektBeregningsRequest, "01D1XGEK2S8WWRZ0QE0Y7W414K"),
                InntektsPeriode("2018-02", "2019-01"),
                InntektsPeriode("2017-02", "2018-01"),
                InntektsPeriode("2016-02", "2017-01"),
                Inntekt(100000, 40000, 0, inneholderNaeringsinntekter = false)
            )
            URI.create("URN:minsteinntekt:scenario3_1") -> MinsteinntektBeregningsResponse(
                ulid.nextULID(),
                Utfall(
                    false,
                    0
                ),
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                mapRequestToParametere(currentMinsteinntektBeregningsRequest, "01D1XGEK398JX50S7P3V9ENH76"),
                InntektsPeriode("2018-02", "2019-01"),
                InntektsPeriode("2017-02", "2018-01"),
                InntektsPeriode("2016-02", "2017-01"),
                Inntekt(100000, 40000, 0, inneholderNaeringsinntekter = false)
            )
            else -> MinsteinntektBeregningsResponse(
                ulid.nextULID(),
                Utfall(
                    false,
                    0
                ),
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                mapRequestToParametere(currentMinsteinntektBeregningsRequest, ulid.nextULID()),
                InntektsPeriode("2018-02", "2019-01"),
                InntektsPeriode("2017-02", "2018-01"),
                InntektsPeriode("2016-02", "2017-01"),
                Inntekt(100000, 40000, 0, inneholderNaeringsinntekter = false)
            )
        }
    }

    override fun startGrunnlagBeregning(request: DagpengegrunnlagBeregningsRequest): URI {
        return when {
            GrunnlagScenario1 == request -> {
                currentGrunnlagRequest = request
                URI.create("URN:grunnlag:scenario1")
            }
            GrunnlagScenario2 == request -> {
                currentGrunnlagRequest = request
                URI.create("URN:grunnlag:scenario2")
            }
            GrunnlagScenario3 == request -> {
                currentGrunnlagRequest = request
                URI.create("URN:grunnlag:scenario3")
            }
            else -> {
                currentGrunnlagRequest = request
                URI.create("URN:grunnlag:nomatch")
            }
        }
    }

    override fun getGrunnlag(ressursUrl: URI): DagpengegrunnlagBeregningsResponse {
        return when (ressursUrl) {
            URI.create("URN:grunnlag:scenario1") -> DagpengegrunnlagBeregningsResponse(
                ulid.nextULID(),
                UtfallGrunnlag(
                    500000, 500000, 500000, 5000, "N/A", false
                ),
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                mapRequestToParametere(currentGrunnlagRequest, "01D1XGQB1BSQ4NGXCBMGQ5M2KF"),
                InntektsPeriode("2018-02", "2019-01"),
                InntektsPeriode("2017-02", "2018-01"),
                InntektsPeriode("2016-02", "2017-01"),
                Inntekt(100000, 40000, 0, inneholderNaeringsinntekter = false)
            )
            URI.create("URN:grunnlag:scenario2") -> DagpengegrunnlagBeregningsResponse(
                ulid.nextULID(),
                UtfallGrunnlag(
                    500000, 500000, 500000, 5000, "N/A", false
                ),
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                mapRequestToParametere(currentGrunnlagRequest, "01D1XGQB3NNMHC54ADBGNF7HQG"),
                InntektsPeriode("2018-02", "2019-01"),
                InntektsPeriode("2017-02", "2018-01"),
                InntektsPeriode("2016-02", "2017-01"),
                Inntekt(100000, 40000, 0, inneholderNaeringsinntekter = false)
            )
            URI.create("URN:grunnlag:scenario3") -> DagpengegrunnlagBeregningsResponse(
                ulid.nextULID(),
                UtfallGrunnlag(
                    500000, 500000, 500000, 5000, "N/A", false
                ),
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                mapRequestToParametere(currentGrunnlagRequest, "01D1XGQB49ZP6KCMX3FAP2XTNG"),
                InntektsPeriode("2018-02", "2019-01"),
                InntektsPeriode("2017-02", "2018-01"),
                InntektsPeriode("2016-02", "2017-01"),
                Inntekt(200000, 40000, 0, inneholderNaeringsinntekter = false)
            )
            else -> DagpengegrunnlagBeregningsResponse(
                ulid.nextULID(),
                UtfallGrunnlag(
                    500000, 500000, 500000, 5000, "N/A", false
                ),
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                mapRequestToParametere(currentGrunnlagRequest, ulid.nextULID()),
                InntektsPeriode("2018-02", "2019-01"),
                InntektsPeriode("2017-02", "2018-01"),
                InntektsPeriode("2016-02", "2017-01"),
                Inntekt(100000, 40000, 0, inneholderNaeringsinntekter = false)
            )
        }
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

fun mapRequestToParametere(request: DagpengegrunnlagBeregningsRequest, inntektsId: String): Parametere =
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
val GrunnlagScenario1 = DagpengegrunnlagBeregningsRequest(
    aktorId = "1000074474453",
    vedtakId = 31018398,
    beregningsdato = "2019-02-08",
    harAvtjentVerneplikt = false,
    oppfyllerKravTilFangstOgFisk = false
)

val GrunnlagScenario2 = DagpengegrunnlagBeregningsRequest(
    aktorId = "1000066295933",
    vedtakId = 31018397,
    beregningsdato = "2019-01-31",
    harAvtjentVerneplikt = false,
    oppfyllerKravTilFangstOgFisk = false
)

val GrunnlagScenario3 = DagpengegrunnlagBeregningsRequest(
    aktorId = "1000066295933",
    vedtakId = 31018397,
    beregningsdato = "2019-02-08",
    harAvtjentVerneplikt = false,
    oppfyllerKravTilFangstOgFisk = false
)