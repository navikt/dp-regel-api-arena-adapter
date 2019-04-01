package no.nav.dagpenger.regel.api.arena.adapter.v1

class UnMatchingFaktumException(override val message: String) : RuntimeException(message)
class InvalidInnteksperiodeException(override val message: String) : RuntimeException(message)