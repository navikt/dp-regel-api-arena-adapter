package no.nav.dagpenger.regel.api.arena.adapter

import com.squareup.moshi.JsonAdapter

fun <T> String?.parseJsonFrom(adapter: JsonAdapter<T>): T {
    return this?.let { adapter.fromJson(it) } ?: throw AssertionError("No content from server")
}
