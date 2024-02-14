package no.nav.dagpenger.regel.api.internal

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.ResponseResultOf
import no.nav.dagpenger.regel.api.arena.adapter.responseObject

internal class FuelHttpClient(val baseUrl: String, private val apiKey: String? = null) {
    private val instance = FuelManager().apply {
        apiKey?.let {
            this.baseHeaders = mapOf("X-API-KEY" to it)
        }
    }

    inline fun request(method: Method, path: String, configure: (Request) -> Unit) =
        instance.request(method, this.baseUrl + path).apply(configure)

    inline fun <reified T : Any> get(path: String, configure: (Request) -> Unit = {}): ResponseResultOf<T> =
        run(Method.GET, path, configure)

    inline fun <reified T : Any> post(path: String, configure: (Request) -> Unit): ResponseResultOf<T> =
        run(Method.POST, path, configure)

    inline fun <reified T : Any> run(method: Method, path: String, configure: (Request) -> Unit): ResponseResultOf<T> =
        request(method, path, configure).run { this.responseObject() }
}
