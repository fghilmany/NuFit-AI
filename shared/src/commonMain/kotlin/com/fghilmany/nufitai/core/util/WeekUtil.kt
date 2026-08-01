package com.fghilmany.nufitai.core.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/** techspec meal-plan.md §5/§6 -- Monday 00:00 local time, keys `MealPlan.weekStartDate` (AC-6/AC-8). */
fun startOfWeek(instant: Instant, timeZone: TimeZone = TimeZone.currentSystemDefault()): Instant {
    val localDateTime = instant.toLocalDateTime(timeZone)
    // kotlinx.datetime.DayOfWeek.MONDAY has ordinal 0 -- already "days since Monday", no -1 needed.
    val daysSinceMonday = localDateTime.dayOfWeek.ordinal
    val startOfDay = LocalDateTime(localDateTime.date, LocalTime(0, 0))
    return startOfDay.toInstant(timeZone).minus(daysSinceMonday.days)
}
