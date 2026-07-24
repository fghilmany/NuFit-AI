package com.fghilmany.nufitai.domain.fullassessment.entity

enum class PushupVersion { STANDAR, LUTUT }
enum class TestProtocol { MAXIMAL, SUBMAXIMAL_RPE7, TEMPO_TERKONTROL }

data class PushupResult(val reps: Int, val versi: PushupVersion, val protokol: TestProtocol)
data class PlankResult(val detik: Int, val protokol: TestProtocol)
data class SitToStandResult(val reps: Int, val protokol: TestProtocol)

/** Nullable per-test -- CAL-05: any skipped test defaults its pola_gerak to Regresi (safest). */
data class CapacityTestResult(
    val pushup: PushupResult?,
    val plank: PlankResult?,
    val sitToStand: SitToStandResult?,
)
