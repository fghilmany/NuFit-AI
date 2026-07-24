package com.fghilmany.nufitai.usecase.monthlyplan.rules

import com.fghilmany.nufitai.domain.exerciselibrary.entity.BodyArea
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseFlag
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MovementPattern

data class FilterKeamananResult(
    val filteredPool: Map<MovementPattern, List<Exercise>>,
    /** SAFE-09: capped at [SAFE_09_BUDGET_MAX], selected by "most restrictive flag" heuristic. */
    val korektifWajib: List<Exercise>,
    /** SAFE-09 overflow -- becomes `PlanDay.homework` on the nearest rest day, never dropped. */
    val korektifOverflow: List<Exercise>,
)

const val SAFE_09_BUDGET_MAX = 3

/**
 * issue #29 layer 3 -- SAFE-01..18, implemented generically via each [Exercise]'s own
 * `flagExclusion`/`flagPrioritas` (sourced from the `Gym Techniques` catalog) rather than
 * hardcoding each rule's specific "ganti_dengan" exercise IDs -- the catalog already encodes
 * that substitution data per-exercise, so exclusion + pool re-sort achieves the same outcome
 * as the individual SAFE-01/02/03/04/05/06/07/13/14/15/16/17 rules without duplicating it.
 *
 * SAFE-09 (corrective budget) and SAFE-10 (area-based soft-block) are NOT expressible as
 * per-exercise exclusion and are implemented explicitly below.
 * SAFE-08/12 (informational reminders, no exercise-pool effect) are presentation-layer
 * concerns, out of scope for this pure rule object.
 */
object FilterKeamanan {
    operator fun invoke(
        pool: Map<MovementPattern, List<Exercise>>,
        activeFlags: Set<ExerciseFlag>,
        areaNyeri: Set<BodyArea>, // SAFE-10
    ): FilterKeamananResult {
        val filtered = pool.mapValues { (_, exercises) ->
            exercises.filter { exercise ->
                exercise.flagExclusion.none { it in activeFlags } && // SAFE-01..07/13..18
                    (exercise.areaTerbebani?.none { it in areaNyeri } != false) // SAFE-10
            }.sortedByDescending { it.flagPrioritas.count { flag -> flag in activeFlags } }
        }

        val korektifCandidates = pool[MovementPattern.CORRECTIVE].orEmpty()
            .filter { it.flagPrioritas.any { flag -> flag in activeFlags } }
            .sortedByDescending { it.flagPrioritas.count { flag -> flag in activeFlags } } // "paling membatasi" proxy

        return FilterKeamananResult(
            filteredPool = filtered,
            korektifWajib = korektifCandidates.take(SAFE_09_BUDGET_MAX),
            korektifOverflow = korektifCandidates.drop(SAFE_09_BUDGET_MAX),
        )
    }
}
