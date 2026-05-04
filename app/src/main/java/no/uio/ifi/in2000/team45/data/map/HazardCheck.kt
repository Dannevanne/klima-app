package no.uio.ifi.in2000.team45.data.map

// Result of a point-in-hazard check against an NVE service.
// UNKNOWN means the upstream call did not respond -- callers should render
// that as "no data" instead of pretending the point is safe.
enum class HazardCheck {
    IN_ZONE,
    OUT_OF_ZONE,
    UNKNOWN;

    val isIn: Boolean get() = this == IN_ZONE
    val isKnown: Boolean get() = this != UNKNOWN
}
