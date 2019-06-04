package no.nav.dagpenger.regel.api.arena.adapter.v1

class FeilBeregningsregelException(message: String) : RuntimeException(message)
class MissingSubsumsjonDataException(message: String) : RuntimeException(message)
class InvalidInnteksperiodeException(override val message: String) : RuntimeException(message)
