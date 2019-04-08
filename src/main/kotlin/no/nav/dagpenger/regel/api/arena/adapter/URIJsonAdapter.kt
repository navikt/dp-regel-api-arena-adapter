package no.nav.dagpenger.regel.api.arena.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.net.URI

class URIJsonAdapter {
    @ToJson
    fun toJson(uri: URI): String {
        return uri.toString()
    }

    @FromJson
    fun fromJson(json: String): URI {
        return URI.create(json)
    }
}
