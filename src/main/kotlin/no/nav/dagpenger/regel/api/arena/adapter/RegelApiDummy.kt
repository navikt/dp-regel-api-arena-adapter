package no.nav.dagpenger.regel.api.arena.adapter

import de.huxhorn.sulky.ulid.ULID
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class RegelApiDummy : RegelApiClient {

    private val ulid = ULID()
    lateinit var currentMinsteinntektBeregningsRequest: MinsteinntektBeregningsRequest
    lateinit var currentGrunnlagRequest: DagpengegrunnlagBeregningsRequest

    override fun startMinsteinntektBeregning(request: MinsteinntektBeregningsRequest): URI {
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
            Scenario5_1Request == request -> {
                currentMinsteinntektBeregningsRequest = request
                URI.create("URN:minsteinntekt:scenario5_1")
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
                MinsteinntektUtfall(
                    false,
                    0
                ),
                LocalDateTime.now(),
                LocalDateTime.now(),
                mapRequestToParametere(currentMinsteinntektBeregningsRequest, "01D1XGEK0FNB179YPXB12TPDPT"),
                InntektsPeriode(YearMonth.parse("2018-01"), YearMonth.parse("2018-12")),
                InntektsPeriode(YearMonth.parse("2017-01"), YearMonth.parse("2017-12")),
                InntektsPeriode(YearMonth.parse("2016-01"), YearMonth.parse("2016-12")),
                Inntekt(50000, 0, 0, inneholderNaeringsinntekter = false)
            )
            URI.create("URN:minsteinntekt:scenario1_2") -> MinsteinntektBeregningsResponse(
                ulid.nextULID(),
                MinsteinntektUtfall(
                    true,
                    52
                ),
                LocalDateTime.now(),
                LocalDateTime.now(),
                mapRequestToParametere(currentMinsteinntektBeregningsRequest, "01D1XGEK15D0B6GDPC74C0ZASV"),
                InntektsPeriode(YearMonth.parse("2018-02"), YearMonth.parse("2019-01")),
                InntektsPeriode(YearMonth.parse("2017-02"), YearMonth.parse("2018-01")),
                InntektsPeriode(YearMonth.parse("2016-02"), YearMonth.parse("2017-01")),
                Inntekt(200000, 0, 0, inneholderNaeringsinntekter = false)
            )
            URI.create("URN:minsteinntekt:scenario1_3") -> MinsteinntektBeregningsResponse(
                ulid.nextULID(),
                MinsteinntektUtfall(
                    true,
                    104
                ),
                LocalDateTime.now(),
                LocalDateTime.now(),
                mapRequestToParametere(currentMinsteinntektBeregningsRequest, "01D1XGEK15D0B6GDPC74C0ZASV"),
                InntektsPeriode(YearMonth.parse("2018-02"), YearMonth.parse("2019-01")),
                InntektsPeriode(YearMonth.parse("2017-02"), YearMonth.parse("2018-01")),
                InntektsPeriode(YearMonth.parse("2016-02"), YearMonth.parse("2017-01")),
                Inntekt(500000, 0, 0, inneholderNaeringsinntekter = true)
            )
            URI.create("URN:minsteinntekt:scenario2_1-3") -> MinsteinntektBeregningsResponse(
                ulid.nextULID(),
                MinsteinntektUtfall(
                    true,
                    52
                ),
                LocalDateTime.now(),
                LocalDateTime.now(),
                mapRequestToParametere(currentMinsteinntektBeregningsRequest, "01D1XGEK2CA0X5BM7PZFYKS5WX"),
                InntektsPeriode(YearMonth.parse("2018-01"), YearMonth.parse("2018-12")),
                InntektsPeriode(YearMonth.parse("2017-01"), YearMonth.parse("2017-12")),
                InntektsPeriode(YearMonth.parse("2016-01"), YearMonth.parse("2016-12")),
                Inntekt(164701, 0, 0, inneholderNaeringsinntekter = false)
            )
            URI.create("URN:minsteinntekt:scenario2_2") -> MinsteinntektBeregningsResponse(
                ulid.nextULID(),
                MinsteinntektUtfall(
                    false,
                    0
                ),
                LocalDateTime.now(),
                LocalDateTime.now(),
                mapRequestToParametere(currentMinsteinntektBeregningsRequest, "01D1XGEK2S8WWRZ0QE0Y7W414K"),
                InntektsPeriode(YearMonth.parse("2018-02"), YearMonth.parse("2019-01")),
                InntektsPeriode(YearMonth.parse("2017-02"), YearMonth.parse("2018-01")),
                InntektsPeriode(YearMonth.parse("2016-02"), YearMonth.parse("2017-01")),
                Inntekt(100000, 40000, 0, inneholderNaeringsinntekter = false)
            )
            URI.create("URN:minsteinntekt:scenario3_1") -> MinsteinntektBeregningsResponse(
                ulid.nextULID(),
                MinsteinntektUtfall(
                    false,
                    0
                ),
                LocalDateTime.now(),
                LocalDateTime.now(),
                mapRequestToParametere(currentMinsteinntektBeregningsRequest, "01D1XGEK398JX50S7P3V9ENH76"),
                InntektsPeriode(YearMonth.parse("2018-02"), YearMonth.parse("2019-01")),
                InntektsPeriode(YearMonth.parse("2017-02"), YearMonth.parse("2018-01")),
                InntektsPeriode(YearMonth.parse("2016-02"), YearMonth.parse("2017-01")),
                Inntekt(100000, 40000, 0, inneholderNaeringsinntekter = false)
            )
            URI.create("URN:minsteinntekt:scenario5_1") -> MinsteinntektBeregningsResponse(
                ulid.nextULID(),
                MinsteinntektUtfall(
                    true,
                    104
                ),
                LocalDateTime.now(),
                LocalDateTime.now(),
                mapRequestToParametere(currentMinsteinntektBeregningsRequest, "01D1XGEK151116GDPC74C0Z111"),
                InntektsPeriode(YearMonth.parse("2018-01"), YearMonth.parse("2018-12")),
                InntektsPeriode(YearMonth.parse("2017-01"), YearMonth.parse("2017-12")),
                InntektsPeriode(YearMonth.parse("2016-01"), YearMonth.parse("2016-12")),
                Inntekt(500000, 0, 0, inneholderNaeringsinntekter = false)
            )
            URI.create("URN:minsteinntekt:scenario5_2") -> MinsteinntektBeregningsResponse(
                ulid.nextULID(),
                MinsteinntektUtfall(
                    true,
                    104
                ),
                LocalDateTime.now(),
                LocalDateTime.now(),
                mapRequestToParametere(currentMinsteinntektBeregningsRequest, "01D1XGEK151116GDPC74C0Z111"),
                InntektsPeriode(YearMonth.parse("2018-01"), YearMonth.parse("2018-12")),
                InntektsPeriode(YearMonth.parse("2017-01"), YearMonth.parse("2017-12")),
                InntektsPeriode(YearMonth.parse("2016-01"), YearMonth.parse("2016-12")),
                Inntekt(500000, 0, 0, inneholderNaeringsinntekter = true)
            )
            else -> MinsteinntektBeregningsResponse(
                ulid.nextULID(),
                MinsteinntektUtfall(
                    false,
                    0
                ),
                LocalDateTime.now(),
                LocalDateTime.now(),
                mapRequestToParametere(currentMinsteinntektBeregningsRequest, ulid.nextULID()),
                InntektsPeriode(YearMonth.parse("2018-02"), YearMonth.parse("2019-01")),
                InntektsPeriode(YearMonth.parse("2017-02"), YearMonth.parse("2018-01")),
                InntektsPeriode(YearMonth.parse("2016-02"), YearMonth.parse("2017-01")),
                Inntekt(100000, 40000, 0, inneholderNaeringsinntekter = false)
            )
        }
    }

    override fun startGrunnlagBeregning(request: DagpengegrunnlagBeregningsRequest): URI {
        return when {
            Scenario3_4Request == request -> {
                currentGrunnlagRequest = request
                URI.create("URN:grunnlag:scenario1")
            }
            Scenario4_1Request == request -> {
                currentGrunnlagRequest = request
                URI.create("URN:grunnlag:scenario2")
            }
            Scenario4_2Request == request -> {
                currentGrunnlagRequest = request
                URI.create("URN:grunnlag:scenario3")
            }
            Scenario5_2Request == request -> {
                throw BadRequestException()
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
                LocalDateTime.now(),
                LocalDateTime.now(),
                mapRequestToParametere(currentGrunnlagRequest, "01D1XGQB1BSQ4NGXCBMGQ5M2KF"),
                InntektsPeriode(YearMonth.parse("2018-02"), YearMonth.parse("2019-01")),
                InntektsPeriode(YearMonth.parse("2017-02"), YearMonth.parse("2018-01")),
                InntektsPeriode(YearMonth.parse("2016-02"), YearMonth.parse("2017-01")),
                Inntekt(100000, 40000, 0, inneholderNaeringsinntekter = false)
            )
            URI.create("URN:grunnlag:scenario2") -> DagpengegrunnlagBeregningsResponse(
                ulid.nextULID(),
                UtfallGrunnlag(
                    500000, 500000, 500000, 5000, "N/A", false
                ),
                LocalDateTime.now(),
                LocalDateTime.now(),
                mapRequestToParametere(currentGrunnlagRequest, "01D1XGQB3NNMHC54ADBGNF7HQG"),
                InntektsPeriode(YearMonth.parse("2018-02"), YearMonth.parse("2019-01")),
                InntektsPeriode(YearMonth.parse("2017-02"), YearMonth.parse("2018-01")),
                InntektsPeriode(YearMonth.parse("2016-02"), YearMonth.parse("2017-01")),
                Inntekt(100000, 40000, 0, inneholderNaeringsinntekter = false)
            )
            URI.create("URN:grunnlag:scenario3") -> DagpengegrunnlagBeregningsResponse(
                ulid.nextULID(),
                UtfallGrunnlag(
                    500000, 500000, 500000, 5000, "N/A", false
                ),
                LocalDateTime.now(),
                LocalDateTime.now(),
                mapRequestToParametere(currentGrunnlagRequest, "01D1XGQB49ZP6KCMX3FAP2XTNG"),
                InntektsPeriode(YearMonth.parse("2018-02"), YearMonth.parse("2019-01")),
                InntektsPeriode(YearMonth.parse("2017-02"), YearMonth.parse("2018-01")),
                InntektsPeriode(YearMonth.parse("2016-02"), YearMonth.parse("2017-01")),
                Inntekt(200000, 40000, 0, inneholderNaeringsinntekter = false)
            )
            else -> DagpengegrunnlagBeregningsResponse(
                ulid.nextULID(),
                UtfallGrunnlag(
                    500000, 500000, 500000, 5000, "N/A", false
                ),
                LocalDateTime.now(),
                LocalDateTime.now(),
                mapRequestToParametere(currentGrunnlagRequest, ulid.nextULID()),
                InntektsPeriode(YearMonth.parse("2018-02"), YearMonth.parse("2019-01")),
                InntektsPeriode(YearMonth.parse("2017-02"), YearMonth.parse("2018-01")),
                InntektsPeriode(YearMonth.parse("2016-02"), YearMonth.parse("2017-01")),
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
    beregningsdato = LocalDate.of(2019, 1, 10),
    harAvtjentVerneplikt = false,
    oppfyllerKravTilFangstOgFisk = false
)

val Scenario1_2Request = Scenario1_1Request.copy(beregningsdato = LocalDate.of(2019, 2, 6))
val Scenario1_3Request = Scenario1_2Request.copy(
    oppfyllerKravTilFangstOgFisk = true
)

val Scenario2_1_AND_3_Request = MinsteinntektBeregningsRequest(
    aktorId = "1000003221752",
    vedtakId = 31018347,
    beregningsdato = LocalDate.of(2019, 1, 11),
    harAvtjentVerneplikt = false,
    oppfyllerKravTilFangstOgFisk = false
)

val Scenario2_2Request = MinsteinntektBeregningsRequest(
    aktorId = "1000003221752",
    vedtakId = 31018347,
    beregningsdato = LocalDate.of(2019, 2, 7),
    harAvtjentVerneplikt = false,
    oppfyllerKravTilFangstOgFisk = false
)

val Scenario3_1Request = MinsteinntektBeregningsRequest(
    aktorId = "1000074474453",
    vedtakId = 31018398,
    beregningsdato = LocalDate.of(2019, 2, 8),
    harAvtjentVerneplikt = false,
    oppfyllerKravTilFangstOgFisk = false
)

val Scenario3_4Request = DagpengegrunnlagBeregningsRequest(
    aktorId = "1000074474453",
    vedtakId = 31018398,
    beregningsdato = LocalDate.of(2019, 2, 8),
    harAvtjentVerneplikt = false,
    oppfyllerKravTilFangstOgFisk = false
)

val Scenario4_1Request = DagpengegrunnlagBeregningsRequest(
    aktorId = "1000066295933",
    vedtakId = 31018397,
    beregningsdato = LocalDate.of(2019, 1, 31),
    harAvtjentVerneplikt = false,
    oppfyllerKravTilFangstOgFisk = false
)

val Scenario4_2Request = DagpengegrunnlagBeregningsRequest(
    aktorId = "1000066295933",
    vedtakId = 31018397,
    beregningsdato = LocalDate.of(2019, 2, 8),
    harAvtjentVerneplikt = false,
    oppfyllerKravTilFangstOgFisk = false
)

val Scenario5_1Request = MinsteinntektBeregningsRequest(
    aktorId = "123",
    vedtakId = 456,
    beregningsdato = LocalDate.of(2019, 2, 2),
    harAvtjentVerneplikt = false,
    oppfyllerKravTilFangstOgFisk = false
)

val Scenario5_2Request = DagpengegrunnlagBeregningsRequest(
    aktorId = "123",
    vedtakId = 456,
    beregningsdato = LocalDate.of(2019, 2, 10),
    harAvtjentVerneplikt = false,
    oppfyllerKravTilFangstOgFisk = false,
    beregningsId = "01DFXL4H9127LKMTR99"
)