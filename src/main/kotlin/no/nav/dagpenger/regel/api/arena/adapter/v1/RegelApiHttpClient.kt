package no.nav.dagpenger.regel.api.arena.adapter.v1

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.github.kittinunf.fuel.moshi.responseObject
import com.github.kittinunf.result.Result
import no.nav.dagpenger.regel.api.arena.adapter.RegelApiArenaAdapterException
import no.nav.dagpenger.regel.api.arena.adapter.moshiInstance
import no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt.MinsteinntektSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.minsteinntekt.MinsteinntektInnParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.periode.PeriodeSubsumsjon

class RegelApiHttpClient(private val regelApiUrl: String) {

    fun startMinsteinntektSubsumsjon(payload: MinsteinntektInnParametere): String {
        val url = "$regelApiUrl/minsteinntekt"

        val jsonAdapter = moshiInstance.adapter(MinsteinntektInnParametere::class.java)
        val json = jsonAdapter.toJson(payload)

        val (_, response, result) =
            with(url.httpPost()
                .header(mapOf("Content-Type" to "application/json"))
                .body(json)) {
                responseObject<TaskResponse>()
            }
        return when (result) {
            is Result.Failure -> throw RegelApiHttpClientException(
                response.statusCode, response.responseMessage, result.getException())
            is Result.Success ->
                response.headers["Location"]?.first() ?: throw RegelApiArenaAdapterException("No location")
        }
    }

    fun getMinsteinntekt(ressursUrl: String): MinsteinntektSubsumsjon {
        val url = "$regelApiUrl$ressursUrl"
        val jsonAdapter = moshiInstance.adapter(MinsteinntektSubsumsjon::class.java)

        val (_, response, result) =
            with(url.httpGet()) { responseObject(moshiDeserializerOf(jsonAdapter)) }
        return when (result) {
            is Result.Failure -> throw RegelApiHttpClientException(
                response.statusCode, response.responseMessage, result.getException())
            is Result.Success -> result.get()
        }
    }

    fun getPeriode(ressursUrl: String): PeriodeSubsumsjon {
        val url = "$regelApiUrl$ressursUrl"
        val jsonAdapter = moshiInstance.adapter(PeriodeSubsumsjon::class.java)

        val (_, response, result) =
            with(url.httpGet()) { responseObject(moshiDeserializerOf(jsonAdapter)) }
        return when (result) {
            is Result.Failure -> throw RegelApiHttpClientException(
                response.statusCode, response.responseMessage, result.getException())
            is Result.Success -> result.get()
        }
    }

    fun startPeriodeSubsumsjon(payload: MinsteinntektInnParametere): String {
        val url = "$regelApiUrl/periode"

        val jsonAdapter = moshiInstance.adapter(MinsteinntektInnParametere::class.java)
        val json = jsonAdapter.toJson(payload)

        val (_, response, result) =
            with(url.httpPost()
                .header(mapOf("Content-Type" to "application/json"))
                .body(json)) {
                responseObject<TaskResponse>()
            }
        return when (result) {
            is Result.Failure -> throw RegelApiHttpClientException(
                response.statusCode, response.responseMessage, result.getException())
            is Result.Success ->
                response.headers["Location"]?.first() ?: throw RegelApiArenaAdapterException("No location")
        }
    }

    fun pollTask (taskUrl: String): TaskPollResponse {
        val url = "$regelApiUrl$taskUrl"

        val (_, response, result) =
            with(url.httpGet().allowRedirects(false)) { responseObject<TaskResponse>() }

        return try {
            TaskPollResponse(result.get(), null)
        } catch (exception: Exception) {
            if (response.statusCode == 303) {
                return TaskPollResponse(null, response.headers["Location"]?.first())
            } else {
                throw RegelApiHttpClientException(response.statusCode, response.responseMessage, exception)
            }
        }
    }

    class RegelApiHttpClientException(val statusCode: Int, override val message: String, override val cause: Throwable) : RuntimeException(message, cause)
}

enum class Regel {
    MINSTEINNTEKT, GRUNNLAG, PERIODE, SATS
}

enum class TaskStatus {
    PENDING, DONE
}

data class TaskResponse(
    val regel: Regel,
    val status: TaskStatus,
    val expires: String
)

data class TaskPollResponse(
    val task: TaskResponse?,
    val location: String?
)
