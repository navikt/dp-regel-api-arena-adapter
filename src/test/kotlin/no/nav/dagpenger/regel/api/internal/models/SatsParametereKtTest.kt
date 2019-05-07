package no.nav.dagpenger.regel.api.internal.models

import io.kotlintest.shouldBe
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.InntektsPeriode
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth

internal class SatsParametereKtTest {

    @Test
    fun toSatsParametere() {
        val localDate = LocalDate.now()
        val inntektsPeriode = InntektsPeriode(
            foersteMaaned = YearMonth.now(),
            sisteMaaned = YearMonth.now()
        )
        val grunnlagOgSatsParametere = GrunnlagOgSatsParametere(
            aktorId = "1234",
            vedtakId = 1234,
            beregningsdato = localDate,
            harAvtjentVerneplikt = true,
            oppfyllerKravTilFangstOgFisk = false,
            antallBarn = 1,
            bruktInntektsPeriode = inntektsPeriode,
            grunnlag = 100
        )

        val satsParametere = grunnlagOgSatsParametere.toSatsParametere()
        with(satsParametere) {
            aktorId shouldBe "1234"
            vedtakId shouldBe 1234
            beregningsdato shouldBe localDate
            harAvtjentVerneplikt shouldBe true
            antallBarn shouldBe 1
            manueltGrunnlag shouldBe 100
        }
    }
}