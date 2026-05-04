package no.uio.ifi.in2000.team45.ui.savedplaces

// Risk level for a single hazard category in the saved-places view.
// UNKNOWN is used when the upstream service didn't respond, so the UI
// renders a neutral state instead of pretending the place is safe.
enum class RiskLevel {
    HIGH,
    MODERATE,
    LOW,
    UNKNOWN
}
