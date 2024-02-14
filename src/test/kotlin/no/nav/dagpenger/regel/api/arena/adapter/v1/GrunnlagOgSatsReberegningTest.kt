package no.nav.dagpenger.regel.api.arena.adapter.v1

import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsReberegningParametere
import no.nav.dagpenger.regel.api.internal.BehovRequest
import no.nav.dagpenger.regel.api.internal.RegelKontekst
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class GrunnlagOgSatsReberegningTest {
    @Test
    fun `Mapper parametere til BehovRequest`() {
        val parametere =
            GrunnlagOgSatsReberegningParametere(
                aktorId = "12345",
                vedtakId = 123,
                beregningsdato = LocalDate.of(2019, 5, 13),
                harAvtjentVerneplikt = true,
                oppfyllerKravTilFangstOgFisk = false,
                manueltGrunnlag = 3000,
                antallBarn = 3,
                oppfyllerKravTilLaerling = false,
                inntektsId = "123456123",
            )
        val parametreMedRegelverksdato = parametere.copy(regelverksdato = LocalDate.of(2020, 6, 14))

        val standardBehovRequest =
            BehovRequest(
                aktorId = "12345",
                vedtakId = 123,
                regelkontekst = RegelKontekst(id = "123", type = "vedtak"),
                beregningsdato = LocalDate.of(2019, 5, 13),
                harAvtjentVerneplikt = true,
                oppfyllerKravTilFangstOgFisk = false,
                manueltGrunnlag = 3000,
                antallBarn = 3,
                lærling = false,
                regelverksdato = LocalDate.of(2019, 5, 13),
                inntektsId = "123456123",
            )
        val behovRequestMedRegelverksdato = standardBehovRequest.copy(regelverksdato = LocalDate.of(2020, 6, 14))

        assertEquals(standardBehovRequest, behovFromParametere(parametere))
        assertEquals(behovRequestMedRegelverksdato, behovFromParametere(parametreMedRegelverksdato))
    }

    @Test
    fun `Deprecated grunnlag mappes til manueltgrunnlag`() {
        val parametere =
            GrunnlagOgSatsReberegningParametere(
                aktorId = "12345",
                vedtakId = 123,
                beregningsdato = LocalDate.of(2019, 5, 13),
                grunnlag = 4000,
                inntektsId = "123456123",
            )

        val standardBehovRequest =
            BehovRequest(
                aktorId = "12345",
                vedtakId = 123,
                regelkontekst = RegelKontekst(id = "123", type = "vedtak"),
                beregningsdato = LocalDate.of(2019, 5, 13),
                regelverksdato = LocalDate.of(2019, 5, 13),
                manueltGrunnlag = 4000,
                harAvtjentVerneplikt = false,
                oppfyllerKravTilFangstOgFisk = false,
                lærling = false,
                antallBarn = 0,
                inntektsId = "123456123",
            )
        assertEquals(standardBehovRequest, behovFromParametere(parametere))
    }
}
