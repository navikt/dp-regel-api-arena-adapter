package no.nav.dagpenger.regel.api.arena.adapter

import io.kotlintest.shouldBe
import kotlin.test.assertEquals
import no.nav.dagpenger.regel.api.arena.adapter.v1.FeilBeregningsregelException
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagBeregningsregel
import no.nav.dagpenger.regel.api.internal.findBeregningsregel
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class FindBeregningsregelTest {

    @Test
    fun `Skal returnere beregningsregel ORDINAER_OVER_6G_SISTE_2019 når den er satt til ArbeidsinntektSiste12 og harAvkortet`() {
        val regel = findBeregningsregel("ArbeidsinntektSiste12", true)

        assertEquals(GrunnlagBeregningsregel.ORDINAER_OVER_6G_SISTE_2019, regel)
    }

    @Test
    fun `Skal returnere beregningsregel ORDINAER_OVER_6G_SISTE_2019 når den er satt til FangstOgFiskSiste12 og harAvkortet`() {
        val regel = findBeregningsregel("FangstOgFiskSiste12", true)

        assertEquals(GrunnlagBeregningsregel.ORDINAER_OVER_6G_SISTE_2019, regel)
    }

    @Test
    fun `Skal returnere beregningsregel ORDINAER_OVER_6G_3SISTE_2019 når den er satt til ArbeidsinntektSiste36 og harAvkortet`() {
        val regel = findBeregningsregel("ArbeidsinntektSiste36", true)

        assertEquals(GrunnlagBeregningsregel.ORDINAER_OVER_6G_3SISTE_2019, regel)
    }

    @Test
    fun `Skal returnere beregningsregel ORDINAER_OVER_6G_3SISTE_2019 når den er satt til FangstOgFiskSiste36 og harAvkortet`() {
        val regel = findBeregningsregel("FangstOgFiskSiste36", true)

        assertEquals(GrunnlagBeregningsregel.ORDINAER_OVER_6G_3SISTE_2019, regel)
    }

    @Test
    fun `Skal returnere beregningsregel VERNEPLIKT når den er satt til Verneplikt`() {
        val regel = findBeregningsregel("Verneplikt", false)

        assertEquals(GrunnlagBeregningsregel.VERNEPLIKT, regel)
    }

    @Test
    fun `Skal gi feil dersom det er en ukjent regel benyttet`() {
        assertThrows<FeilBeregningsregelException> { findBeregningsregel("Ukjent", false) }
    }

    @Test
    fun `Skal returnere beregningsregel ORDINAER_ETTAAR når den er satt til ArbeidsinntektSiste12 og ikke harAvkortet`() {
        val regel = findBeregningsregel("ArbeidsinntektSiste12", false)

        assertEquals(GrunnlagBeregningsregel.ORDINAER_ETTAAR, regel)
    }

    @Test
    fun `Skal returnere beregningsregel ORDINAER_ETTAAR når den er satt til FangstOgFiskSiste12 og ikke harAvkortet`() {
        val regel = findBeregningsregel("FangstOgFiskSiste12", false)

        assertEquals(GrunnlagBeregningsregel.ORDINAER_ETTAAR, regel)
    }

    @Test
    fun `Skal returnere beregningsregel ORDINAER_TREAAR når den er satt til ArbeidsinntektSiste36 og ikke harAvkortet`() {
        val regel = findBeregningsregel("ArbeidsinntektSiste36", false)

        assertEquals(GrunnlagBeregningsregel.ORDINAER_TREAAR, regel)
    }

    @Test
    fun `Skal returnere beregningsregel ORDINAER_TREAAR når den er satt til FangstOgFiskSiste36 og ikke harAvkortet`() {
        val regel = findBeregningsregel("FangstOgFiskSiste36", false)

        assertEquals(GrunnlagBeregningsregel.ORDINAER_TREAAR, regel)
    }

    @Test
    fun `skal finne beregningsregel for grunnlag ved lærling forskrift`() {
        findBeregningsregel("LærlingArbeidsinntektSiste1", false) shouldBe GrunnlagBeregningsregel.LAERLING
        findBeregningsregel("LærlingArbeidsinntektSiste3", false) shouldBe GrunnlagBeregningsregel.LAERLING
        findBeregningsregel("LærlingFangstOgFiskSiste1", false) shouldBe GrunnlagBeregningsregel.LAERLING
        findBeregningsregel("LærlingFangstOgFiskSiste3", false) shouldBe GrunnlagBeregningsregel.LAERLING
    }
}
