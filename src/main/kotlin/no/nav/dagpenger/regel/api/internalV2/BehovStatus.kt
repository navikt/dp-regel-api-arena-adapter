package no.nav.dagpenger.regel.api.internalV2

enum class BehovStatus {
    PENDING
}

data class BehovStatusResponse(val status: BehovStatus)