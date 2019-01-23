package no.nav.dagpenger.regel.api.arena.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.math.BigDecimal

class BigDecimalJsonAdapter {

    @ToJson
    fun toJson(bigDecimal: BigDecimal): String {
        return bigDecimal.toString()
    }

    @FromJson
    fun fromJson(json: String): BigDecimal {
        return BigDecimal(json)
    }
}
