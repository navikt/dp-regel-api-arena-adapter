package no.nav.dagpenger.regel.api.arena.adapter

import java.net.URI
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class URIJsonAdapterTest {

    private val uriJsonAdapter = URIJsonAdapter()
    @Test
    fun toJson() {
        Assertions.assertEquals("about:blank", uriJsonAdapter.toJson(URI("about:blank")))
    }

    @Test
    fun fromJson() {
        Assertions.assertEquals(URI("about:blank"), uriJsonAdapter.fromJson("about:blank"))
    }
}
