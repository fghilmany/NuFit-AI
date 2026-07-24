package com.fghilmany.nufitai.usecase.monthlyplan

import com.fghilmany.nufitai.domain.monthlyplan.entity.DayType
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDay
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDaySessionLog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class GetSessionStatusTest {
    private val getSessionStatus = GetSessionStatus()
    private val start = Instant.fromEpochMilliseconds(0)

    private fun day(dayNumber: Int, type: DayType = DayType.SESSION) =
        PlanDay(id = "d$dayNumber", planId = "p1", dayNumber = dayNumber, type = type, templateLetter = null, warmup = null, mainExercises = null, cardio = null, cooldown = null, homework = null)

    @Test
    fun `given a completed log when invoked then status is DONE regardless of day number`() {
        val log = PlanDaySessionLog("l1", "d5", completedAt = start, skippedAt = null, rpeReported = null, painReported = null)
        assertEquals(SessionStatus.DONE, getSessionStatus(day(5), log, start, now = start + 4.days))
    }

    @Test
    fun `given a skipped log when invoked then status is SKIPPED`() {
        val log = PlanDaySessionLog("l1", "d5", completedAt = null, skippedAt = start, rpeReported = null, painReported = null)
        assertEquals(SessionStatus.SKIPPED, getSessionStatus(day(5), log, start, now = start + 4.days))
    }

    @Test
    fun `given no log and day number matches today when invoked then status is TODAY`() {
        assertEquals(SessionStatus.TODAY, getSessionStatus(day(5), log = null, planStartedAt = start, now = start + 4.days))
    }

    @Test
    fun `given no log and day number is in the future when invoked then status is UPCOMING`() {
        assertEquals(SessionStatus.UPCOMING, getSessionStatus(day(10), log = null, planStartedAt = start, now = start + 4.days))
    }

    @Test
    fun `given a rest day with no log when invoked then status is REST`() {
        assertEquals(SessionStatus.REST, getSessionStatus(day(5, DayType.REST), log = null, planStartedAt = start, now = start + 4.days))
    }
}
