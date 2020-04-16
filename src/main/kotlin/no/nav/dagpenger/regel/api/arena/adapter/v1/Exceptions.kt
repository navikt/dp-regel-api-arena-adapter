package no.nav.dagpenger.regel.api.arena.adapter.v1

import no.nav.dagpenger.regel.api.arena.adapter.Problem

class FeilBeregningsregelException(message: String) : RuntimeException(message)
class MissingSubsumsjonDataException(message: String) : RuntimeException(message)
class SubsumsjonProblem(val problem: Problem) : RuntimeException("Subsumsjon har problem: ${problem.title}")
class InvalidInnteksperiodeException(override val message: String) : RuntimeException(message)
class NegativtGrunnlagException(message: String) : RuntimeException(message)
