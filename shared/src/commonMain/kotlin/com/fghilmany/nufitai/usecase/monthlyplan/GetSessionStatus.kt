package com.fghilmany.nufitai.usecase.monthlyplan

import com.fghilmany.nufitai.domain.monthlyplan.entity.DayType
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDay
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDaySessionLog
import kotlin.time.Instant

enum class SessionStatus { DONE, SKIPPED, TODAY, UPCOMING, REST, LIGHT_ACTIVITY }

/**
 * Pure derivation (AC-1: "dihitung dari log, bukan disimpan manual") -- the one source of
 * truth used by both P-03's session cards and P-04's actual-vs-target rendering.
 * Timezone/DST-aware day boundaries are explicitly out of scope (issue #76 §9 item 8/G5,
 * deferred) -- this uses device-local whole-day differences only.
 */
class GetSessionStatus {
    operator fun invoke(planDay: PlanDay, log: PlanDaySessionLog?, planStartedAt: Instant, now: Instant): SessionStatus {
        if (log?.completedAt != null) return SessionStatus.DONE
        if (log?.skippedAt != null) return SessionStatus.SKIPPED

        if (planDay.type == DayType.REST) return SessionStatus.REST
        if (planDay.type == DayType.LIGHT_ACTIVITY) return SessionStatus.LIGHT_ACTIVITY

        val todayDayNumber = (now - planStartedAt).inWholeDays.toInt() + 1
        return if (planDay.dayNumber == todayDayNumber) SessionStatus.TODAY else SessionStatus.UPCOMING
    }
}
