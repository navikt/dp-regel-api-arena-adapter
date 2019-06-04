package no.nav.dagpenger.regel.api.internal.models

enum class BehovStatus {
    PENDING
}

data class BehovStatusResponse(val status: BehovStatus)