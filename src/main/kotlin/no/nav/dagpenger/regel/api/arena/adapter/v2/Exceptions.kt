package no.nav.dagpenger.regel.api.arena.adapter.v2

class FeilBeregningsregelException(message: String) : RuntimeException(message)
class MissingSubsumsjonDataException(message: String) : RuntimeException(message)
class InvalidInnteksperiodeException(override val message: String) : RuntimeException(message)
