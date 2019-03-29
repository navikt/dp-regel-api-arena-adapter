package no.nav.dagpenger.regel.api.arena.adapter

import no.nav.dagpenger.regel.api.arena.adapter.v1.findBeregningsregel
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsResultat
import no.nav.dagpenger.regel.api.internal.models.GrunnlagResultat
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FindBeregningsregelTest {

    @Test
    fun `Skal returnere beregningsregel ORDINAER_OVER_6G_SISTE_2019 når den er satt til ArbeidsinntektSiste12`() {
        val grunnlagResultat = GrunnlagResultat(123, 123, "ArbeidsinntektSiste12")

        assertEquals(GrunnlagOgSatsResultat.Beregningsregel.ORDINAER_OVER_6G_SISTE_2019, findBeregningsregel(grunnlagResultat))
    }

    @Test
    fun `Skal returnere beregningsregel ORDINAER_OVER_6G_SISTE_2019 når den er satt til FangstOgFiskSiste12`() {
        val grunnlagResultat = GrunnlagResultat(123, 123, "FangstOgFiskSiste12")

        assertEquals(GrunnlagOgSatsResultat.Beregningsregel.ORDINAER_OVER_6G_SISTE_2019, findBeregningsregel(grunnlagResultat))
    }

    @Test
    fun `Skal returnere beregningsregel ORDINAER_OVER_6G_3SISTE_2019 når den er satt til ArbeidsinntektSiste36`() {
        val grunnlagResultat = GrunnlagResultat(123, 123, "ArbeidsinntektSiste36")

        assertEquals(GrunnlagOgSatsResultat.Beregningsregel.ORDINAER_OVER_6G_3SISTE_2019, findBeregningsregel(grunnlagResultat))
    }

    @Test
    fun `Skal returnere beregningsregel ORDINAER_OVER_6G_3SISTE_2019 når den er satt til FangstOgFiskSiste36`() {
        val grunnlagResultat = GrunnlagResultat(123, 123, "FangstOgFiskSiste36")

        assertEquals(GrunnlagOgSatsResultat.Beregningsregel.ORDINAER_OVER_6G_3SISTE_2019, findBeregningsregel(grunnlagResultat))
    }

    @Test
    fun `Skal returnere beregningsregel VERNEPLIKT når den er satt til Verneplikt`() {
        val grunnlagResultat = GrunnlagResultat(123, 123, "Verneplikt")

        assertEquals(GrunnlagOgSatsResultat.Beregningsregel.VERNEPLIKT, findBeregningsregel(grunnlagResultat))
    }

    @Test
    fun `Skal returnere beregningsregel MANUELL_UNDER_6G når den er satt til Manuell under 6G`() {
        val grunnlagResultat = GrunnlagResultat(123, 123, "Manuell under 6G")

        assertEquals(GrunnlagOgSatsResultat.Beregningsregel.MANUELL_UNDER_6G, findBeregningsregel(grunnlagResultat))
    }

    @Test
    fun `Skal returnere beregningsregel MANUELL_OVER_6G når den er satt til Manuell over 6G`() {
        val grunnlagResultat = GrunnlagResultat(123, 123, "Manuell over 6G")

        assertEquals(GrunnlagOgSatsResultat.Beregningsregel.MANUELL_OVER_6G, findBeregningsregel(grunnlagResultat))
    }
}