package com.fghilmany.nufitai.usecase.monthlyplan.rules

import kotlin.test.Test
import kotlin.test.assertEquals

class MonthlySchedulerTest {

    @Test
    fun `given a rest-gap-infeasible selectedWeekdays when scheduled then the requested weekly frequency is still honored`() {
        // Regression: 4x/week previously collapsed to ~2x/week because {Mon,Tue,Thu,Fri} has
        // adjacent pairs the rest-gap constraint silently dropped, with no correction.
        val days = MonthlyScheduler.buildCalendarFramework(sessionsPerWeek = 4, selectedWeekdays = setOf(1, 2, 4, 5), minRestBetweenSessions = true)

        assertEquals(17, days.size) // floor(4*30/7)
    }

    @Test
    fun `given a low, achievable frequency with a rest gap when scheduled then no two session days are adjacent`() {
        val days = MonthlyScheduler.buildCalendarFramework(sessionsPerWeek = 3, selectedWeekdays = setOf(1, 3, 5), minRestBetweenSessions = true)

        assertEquals(12, days.size) // floor(3*30/7)
        val gaps = days.zipWithNext { a, b -> b - a }
        assertEquals(true, gaps.all { it > 1 })
    }

    @Test
    fun `given 7x per week when scheduled then every day of the month is a session day`() {
        val days = MonthlyScheduler.buildCalendarFramework(sessionsPerWeek = 7, selectedWeekdays = (1..7).toSet(), minRestBetweenSessions = false)

        assertEquals(30, days.size)
    }

    @Test
    fun `given fewer distinct weekdays picked than the frequency needs when scheduled then the count still matches`() {
        // selectedWeekdays only offers 2 distinct weekdays but 5x/week is requested -- pass 3 fallback fills the rest.
        val days = MonthlyScheduler.buildCalendarFramework(sessionsPerWeek = 5, selectedWeekdays = setOf(1, 2), minRestBetweenSessions = false)

        assertEquals(21, days.size) // floor(5*30/7)
    }
}
