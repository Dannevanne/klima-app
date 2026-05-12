package no.uio.ifi.in2000.team45.ui.statistic.climate

/**
 * Represents a calculated trend compared to a baseline value.
 *
 * @param direction the overall direction of the trend (up, down, or stable).
 * @param percentChange the percentage change relative to the baseline, or null if unavailable.
 */
data class TrendInfo(
    val direction: TrendDirection,
    val percentChange: Double?
)

/**
 * Direction of a trend when comparing current values to a baseline.
 */
enum class TrendDirection { UP, DOWN, STABLE }