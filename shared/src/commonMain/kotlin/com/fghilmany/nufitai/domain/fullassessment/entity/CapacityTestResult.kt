package com.fghilmany.nufitai.domain.fullassessment.entity

enum class PushupVersion { STANDARD, KNEE }
enum class TestProtocol { MAXIMAL, SUBMAXIMAL_RPE7, CONTROLLED_TEMPO }

data class PushupResult(val reps: Int, val version: PushupVersion, val protocol: TestProtocol)
data class PlankResult(val seconds: Int, val protocol: TestProtocol)
data class SitToStandResult(val reps: Int, val protocol: TestProtocol)

/** Nullable per-test -- CAL-05: any skipped test defaults its movement-pattern level to REGRESSION (safest). */
data class CapacityTestResult(
    val pushup: PushupResult?,
    val plank: PlankResult?,
    val sitToStand: SitToStandResult?,
)
