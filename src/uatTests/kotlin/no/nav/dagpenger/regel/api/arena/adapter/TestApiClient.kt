package no.nav.dagpenger.regel.api.arena.adapter

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.moshi.responseObject
import com.github.kittinunf.result.Result
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

private val config = CucumberConfiguration()

class TestApiClient(config: CucumberConfiguration = CucumberConfiguration()) {

    init {
        if (config.disableSSL) {
            FuelManager.instance.apply {
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate>? = null
                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) =
                        Unit

                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) =
                        Unit
                })

                socketFactory = SSLContext.getInstance("SSL").apply {
                    init(null, trustAllCerts, java.security.SecureRandom())
                }.socketFactory

                hostnameVerifier = HostnameVerifier { _, _ -> true }
            }
        }
    }

    private val token = getToken(config)

    private fun getToken(config: CucumberConfiguration): String {
        val parameters = listOf(
            "grant_type" to "client_credentials",
            "scope" to "openid"
        )

        val (_, response, result) = with(config.stsIssuerUrl.httpGet(parameters)) {
            authentication().basic(config.username, config.password)
            responseObject<Map<*, *>>()
        }
        when (result) {
            is Result.Failure -> throw AssertionError("Failed to get token, tried ${config.stsIssuerUrl}, response ${response.responseMessage}", result.getException())
            is Result.Success -> return result.get()["access_token"] as String
        }
    }

    fun minsteinntektOgPeriode(body: String): String {
        return apiRequest(body, "/v2/minsteinntekt")
    }

    fun grunnlagOgSats(body: String): String {
        return apiRequest(body, "/v2/dagpengegrunnlag")
    }

    private fun apiRequest(body: String, path: String): String {
        val (_, _, result) = with(
            "${config.dpApiArenaAdapterUrl}$path".httpPost()
                .header("Authorization", "Bearer $token")
                .header("Content-Type" to "application/json")
                .body(body)
        ) {
            responseString()
        }

        return when (result) {
            is Result.Failure -> throw AssertionError(
                "Failed post to adapter. Response body ${result.error.response.body().asString("application/json")}. Error message: ${result.error.message}"
            )
            is Result.Success -> result.get()
        }
    }
}

val testApiClient = TestApiClient()
