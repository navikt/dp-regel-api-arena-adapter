package no.nav.dagpenger.regel.api.arena.adapter.v1.tasks

enum class Regel {
    MINSTEINNTEKT, GRUNNLAG, PERIODE, SATS
}

enum class TaskStatus {
    PENDING, DONE
}

data class TaskResponse(
    val regel: Regel,
    val status: TaskStatus,
    val expires: String
)

data class TaskPollResponse(
    val task: TaskResponse?,
    val location: String?
)