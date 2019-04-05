package no.nav.dagpenger.regel.api.arena.adapter

import no.nav.dagpenger.regel.api.arena.adapter.v1.FeilBeregningsregelException
import no.nav.dagpenger.regel.api.arena.adapter.v1.findBeregningsregel
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsResultat
import no.nav.dagpenger.regel.api.internal.models.GrunnlagResultat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class FindBeregningsregelTest {

    @Test
    fun `Skal returnere beregningsregel ORDINAER_OVER_6G_SISTE_2019 når den er satt til ArbeidsinntektSiste12 og harAvkortet`() {
        val grunnlagResultat = GrunnlagResultat(123, 123, "ArbeidsinntektSiste12", true)

        assertEquals(GrunnlagOgSatsResultat.Beregningsregel.ORDINAER_OVER_6G_SISTE_2019, findBeregningsregel(grunnlagResultat))
    }

    @Test
    fun `Skal returnere beregningsregel ORDINAER_OVER_6G_SISTE_2019 når den er satt til FangstOgFiskSiste12 og harAvkortet`() {
        val grunnlagResultat = GrunnlagResultat(123, 123, "FangstOgFiskSiste12", true)

        assertEquals(GrunnlagOgSatsResultat.Beregningsregel.ORDINAER_OVER_6G_SISTE_2019, findBeregningsregel(grunnlagResultat))
    }

    @Test
    fun `Skal returnere beregningsregel ORDINAER_OVER_6G_3SISTE_2019 når den er satt til ArbeidsinntektSiste36 og harAvkortet`() {
        val grunnlagResultat = GrunnlagResultat(123, 123, "ArbeidsinntektSiste36", true)

        assertEquals(GrunnlagOgSatsResultat.Beregningsregel.ORDINAER_OVER_6G_3SISTE_2019, findBeregningsregel(grunnlagResultat))
    }

    @Test
    fun `Skal returnere beregningsregel ORDINAER_OVER_6G_3SISTE_2019 når den er satt til FangstOgFiskSiste36 og harAvkortet`() {
        val grunnlagResultat = GrunnlagResultat(123, 123, "FangstOgFiskSiste36", true)

        assertEquals(GrunnlagOgSatsResultat.Beregningsregel.ORDINAER_OVER_6G_3SISTE_2019, findBeregningsregel(grunnlagResultat))
    }

    @Test
    fun `Skal returnere beregningsregel VERNEPLIKT når den er satt til Verneplikt`() {
        val grunnlagResultat = GrunnlagResultat(123, 123, "Verneplikt", false)

        assertEquals(GrunnlagOgSatsResultat.Beregningsregel.VERNEPLIKT, findBeregningsregel(grunnlagResultat))
    }

    @Test
    fun `Skal returnere beregningsregel MANUELL_UNDER_6G når den er satt til Manuell under 6G`() {
        val grunnlagResultat = GrunnlagResultat(123, 123, "Manuell under 6G", false)

        assertEquals(GrunnlagOgSatsResultat.Beregningsregel.MANUELL_UNDER_6G, findBeregningsregel(grunnlagResultat))
    }

    @Test
    fun `Skal returnere beregningsregel MANUELL_OVER_6G når den er satt til Manuell over 6G`() {
        val grunnlagResultat = GrunnlagResultat(123, 123, "Manuell over 6G", true)

        assertEquals(GrunnlagOgSatsResultat.Beregningsregel.MANUELL_OVER_6G, findBeregningsregel(grunnlagResultat))
    }

    @Test
    fun `Skal gi feil dersom det er en ukjent regel benyttet`() {
        val grunnlagResultat = GrunnlagResultat(123, 123, "Ukjent", false)

        assertThrows<FeilBeregningsregelException> { findBeregningsregel(grunnlagResultat) }
    }

    @Test
    fun `Skal returnere beregningsregel ORDINAER_ETTAAR når den er satt til ArbeidsinntektSiste12 og ikke harAvkortet`() {
        val grunnlagResultat = GrunnlagResultat(123, 123, "ArbeidsinntektSiste12", false)

        assertEquals(GrunnlagOgSatsResultat.Beregningsregel.ORDINAER_ETTAAR, findBeregningsregel(grunnlagResultat))
    }

    @Test
    fun `Skal returnere beregningsregel ORDINAER_ETTAAR når den er satt til FangstOgFiskSiste12 og ikke harAvkortet`() {
        val grunnlagResultat = GrunnlagResultat(123, 123, "FangstOgFiskSiste12", false)

        assertEquals(GrunnlagOgSatsResultat.Beregningsregel.ORDINAER_ETTAAR, findBeregningsregel(grunnlagResultat))
    }

    @Test
    fun `Skal returnere beregningsregel ORDINAER_TREAAR når den er satt til ArbeidsinntektSiste36 og ikke harAvkortet`() {
        val grunnlagResultat = GrunnlagResultat(123, 123, "ArbeidsinntektSiste36", false)

        assertEquals(GrunnlagOgSatsResultat.Beregningsregel.ORDINAER_TREAAR, findBeregningsregel(grunnlagResultat))
    }

    @Test
    fun `Skal returnere beregningsregel ORDINAER_TREAAR når den er satt til FangstOgFiskSiste36 og ikke harAvkortet`() {
        val grunnlagResultat = GrunnlagResultat(123, 123, "FangstOgFiskSiste36", false)

        assertEquals(GrunnlagOgSatsResultat.Beregningsregel.ORDINAER_TREAAR, findBeregningsregel(grunnlagResultat))
    }
}