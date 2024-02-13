package no.nav.dagpenger.regel.api.internal

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.ResponseResultOf
import no.nav.dagpenger.regel.api.arena.adapter.responseObject

internal class FuelHttpClient(val baseUrl: String, private val tokentProvider: (() -> String)? = null) {

    val instance = FuelManager()

    inline fun request(method: Method, path: String, configure: (Request) -> Unit): Request {
        val request = instance.request(method, this.baseUrl + path)
        tokentProvider?.let {
            request.header("Authorization", "Bearer ${it()}")
        }
        return request.apply(configure)
    }

    inline fun <reified T : Any> get(path: String, configure: (Request) -> Unit = {}): ResponseResultOf<T> =
        run(Method.GET, path, configure)

    inline fun <reified T : Any> post(path: String, configure: (Request) -> Unit): ResponseResultOf<T> =
        run(Method.POST, path, configure)

    inline fun <reified T : Any> run(method: Method, path: String, configure: (Request) -> Unit): ResponseResultOf<T> =
        request(method, path, configure).run { this.responseObject() }
}
