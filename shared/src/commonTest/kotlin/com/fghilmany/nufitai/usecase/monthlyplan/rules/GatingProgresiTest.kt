package com.fghilmany.nufitai.usecase.monthlyplan.rules

import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDaySessionLog
import com.fghilmany.nufitai.domain.monthlyplan.entity.ProgressionMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class GatingProgresiTest {

    @Test
    fun `given konservatif mode when checkpointDays invoked then returns 21 and 28`() {
        assertEquals(listOf(21, 28), GatingProgresi.checkpointDays(ProgressionMode.KONSERVATIF))
    }

    @Test
    fun `given normal mode when checkpointDays invoked then returns 14 and 21`() {
        assertEquals(listOf(14, 21), GatingProgresi.checkpointDays(ProgressionMode.NORMAL))
    }

    @Test
    fun `given konservatif mode on day 14 when canLevelUp invoked then always false (PROG-06 log only)`() {
        val logs = List(2) { completedLog(rpe = 5) }
        val result = GatingProgresi.canLevelUp(ProgressionMode.KONSERVATIF, checkpointDay = 14, logs, rpeTargetMax = 7, painAreasForPattern = emptySet())
        assertFalse(result)
    }

    @Test
    fun `given 2 consecutive sessions within RPE target and no pain when canLevelUp invoked then true`() {
        val logs = List(2) { completedLog(rpe = 6) }
        val result = GatingProgresi.canLevelUp(ProgressionMode.NORMAL, checkpointDay = 14, logs, rpeTargetMax = 7, painAreasForPattern = emptySet())
        assertTrue(result)
    }

    @Test
    fun `given fewer than 2 sessions when canLevelUp invoked then false`() {
        val result = GatingProgresi.canLevelUp(ProgressionMode.NORMAL, checkpointDay = 14, listOf(completedLog(rpe = 5)), rpeTargetMax = 7, painAreasForPattern = emptySet())
        assertFalse(result)
    }

    @Test
    fun `given no completed session in over 14 days when isAbsent invoked then true`() {
        val now = Instant.fromEpochMilliseconds(0) + kotlin.time.Duration.parse("20d")
        val lastSession = Instant.fromEpochMilliseconds(0)
        assertTrue(GatingProgresi.isAbsent(lastSession, now))
    }

    private fun completedLog(rpe: Int) =
        PlanDaySessionLog("l", "d", completedAt = Instant.fromEpochMilliseconds(0), skippedAt = null, rpeReported = rpe, painReported = emptySet())
}
