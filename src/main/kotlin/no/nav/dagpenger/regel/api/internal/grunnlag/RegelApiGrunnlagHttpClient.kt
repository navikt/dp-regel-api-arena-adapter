package no.nav.dagpenger.regel.api.internal.grunnlag

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.github.kittinunf.fuel.moshi.responseObject
import com.github.kittinunf.result.Result
import no.nav.dagpenger.regel.api.arena.adapter.moshiInstance
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsParametere
import no.nav.dagpenger.regel.api.internal.models.GrunnlagSubsumsjon
import no.nav.dagpenger.regel.api.internal.models.InntektsPeriode
import no.nav.dagpenger.regel.api.internal.models.TaskResponse

class RegelApiGrunnlagHttpClient(private val regelApiUrl: String) {

    private val jsonAdapter = moshiInstance.adapter(no.nav.dagpenger.regel.api.internal.models.GrunnlagParametere::class.java)

    fun startGrunnlagSubsumsjon(payload: GrunnlagOgSatsParametere): String {
        val url = "$regelApiUrl/grunnlag"

        val internalParameters = no.nav.dagpenger.regel.api.internal.models.GrunnlagParametere(
            aktorId = payload.aktorId,
            vedtakId = payload.vedtakId,
            beregningsdato = payload.beregningsdato,
            harAvtjentVerneplikt = payload.harAvtjentVerneplikt,
            oppfyllerKravTilFangstOgFisk = payload.oppfyllerKravTilFangstOgFisk,
            manueltGrunnlag = payload.grunnlag,
            bruktInntektsPeriode = payload.bruktInntektsPeriode?.let {
                InntektsPeriode(
                    førsteMåned = it.foersteMaaned,
                    sisteMåned = it.sisteMaaned
                )
            }
        )

        val json = jsonAdapter.toJson(internalParameters)

        val (_, response, result) =
            with(
                url.httpPost()
                    .header(mapOf("Content-Type" to "application/json"))
                    .body(json)
            ) {
                responseObject<TaskResponse>()
            }
        return when (result) {
            is Result.Failure -> throw RegelApiGrunnlagHttpClientException(
                "Failed to run minsteinntekt. Response message ${response.responseMessage}. Error message: ${result.error.message}")
            is Result.Success ->
                response.headers["Location"].first()
        }
    }

    fun getGrunnlag(ressursUrl: String): GrunnlagSubsumsjon {
        val url = "$regelApiUrl$ressursUrl"
        val jsonAdapter = moshiInstance.adapter(GrunnlagSubsumsjon::class.java)

        val (_, response, result) =
            with(url.httpGet()) { responseObject(moshiDeserializerOf(jsonAdapter)) }
        return when (result) {
            is Result.Failure -> throw RegelApiGrunnlagHttpClientException(
                "Failed to run minsteinntekt. Response message ${response.responseMessage}. Error message: ${result.error.message}")
            is Result.Success -> result.get()
        }
    }
}

class RegelApiGrunnlagHttpClientException(
    override val message: String
) : RuntimeException(message)