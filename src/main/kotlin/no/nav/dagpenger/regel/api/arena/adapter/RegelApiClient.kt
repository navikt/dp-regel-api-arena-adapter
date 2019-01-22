package no.nav.dagpenger.regel.api.arena.adapter

import java.net.URI

interface RegelApiClient {
    fun startMinsteinntktBeregning(request: MinsteinntektBeregningsRequest): URI
    fun getMinsteinntekt(ressursUrl: URI): MinsteinntektBeregningsResponse
    fun startGrunnlagBeregning(request: DagpengegrunnlagBeregningsRequest): URI
    fun getGrunnlag(ressursUrl: URI): DagpengegrunnlagBeregningsResponse
    fun pollTask(taskUrl: URI): TaskPollResponse
}

class RegelApiException(val statusCode: Int, override val message: String, override val cause: Throwable) : RuntimeException(message, cause)

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
    val location: URI?
)