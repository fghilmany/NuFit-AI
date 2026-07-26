package com.fghilmany.nufitai.usecase.planmanagement

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanStatus
import com.fghilmany.nufitai.domain.monthlyplan.entity.TemplatePlanInput
import com.fghilmany.nufitai.domain.monthlyplan.repository.MonthlyPlanRepository
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import com.fghilmany.nufitai.domain.onboarding.entity.Level
import com.fghilmany.nufitai.domain.onboarding.entity.ResolvedSplit
import com.fghilmany.nufitai.usecase.monthlyplan.GenerateLocalTemplatePlan

private const val FULL_BODY_SESSIONS_PER_WEEK = 3
private const val UPPER_LOWER_SESSIONS_PER_WEEK = 4
private val DEFAULT_TEMPLATE_EQUIPMENT = setOf(EquipmentCategory.BODYWEIGHT)

/**
 * P-14 UC-4 -- manual 3-step picker (Level -> Goal -> Split, no assessment, no equipment/frequency
 * step). Equipment defaults to Bodyweight-only; frequency is inferred from the chosen split
 * (same direction Quick Assessment already infers in reverse). If [activateNow], archives the
 * current active plan first (mirrors `ActivatePlan`'s guard) before generating as ACTIVE;
 * otherwise generates as DRAFT with no archiving.
 */
class CreateTemplatePlan(
    private val monthlyPlanRepository: MonthlyPlanRepository,
    private val generateLocalTemplatePlan: GenerateLocalTemplatePlan,
) {
    suspend operator fun invoke(level: Level, goal: GoalCategory, resolvedSplit: ResolvedSplit, activateNow: Boolean): AppResult<MonthlyPlan> {
        if (activateNow) {
            val activePlan = when (val result = monthlyPlanRepository.getActivePlan()) {
                is AppResult.Success -> result.data
                is AppResult.Error -> return result
            }
            if (activePlan != null) {
                when (val archived = monthlyPlanRepository.archivePlan(activePlan.id)) {
                    is AppResult.Error -> return archived
                    is AppResult.Success -> Unit
                }
            }
        }

        val sessionsPerWeek = if (resolvedSplit == ResolvedSplit.FULL_BODY) FULL_BODY_SESSIONS_PER_WEEK else UPPER_LOWER_SESSIONS_PER_WEEK
        val input = TemplatePlanInput(
            level = level,
            goal = goal,
            equipment = DEFAULT_TEMPLATE_EQUIPMENT,
            sessionsPerWeek = sessionsPerWeek,
            resolvedSplit = resolvedSplit,
        )
        return generateLocalTemplatePlan(input, status = if (activateNow) PlanStatus.ACTIVE else PlanStatus.DRAFT)
    }
}
