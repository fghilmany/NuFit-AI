package com.fghilmany.nufitai.usecase.fullassessment

import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseFlag
import com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentParQAnswer
import com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentParQQuestionId
import com.fghilmany.nufitai.domain.fullassessment.entity.isHardStop
import com.fghilmany.nufitai.domain.fullassessment.entity.toExerciseFlagOrNull

data class FullAssessmentParQGateResult(
    val hardStopFlagged: Boolean,
    val flaggedHardStopQuestions: List<FullAssessmentParQQuestionId>,
    val exerciseFlags: Set<ExerciseFlag>,
)

/**
 * Pure decision-table usecase (no I/O), mirrors `ResolveQuickAssessment`'s shape -- GATE-01/
 * GATE-02 evaluation (issue #29 layer 1) doesn't touch the repository. Persistence happens once,
 * at the end of the wizard, via `CompleteFullAssessment`.
 */
class SubmitFullAssessmentParQ {
    operator fun invoke(answers: List<FullAssessmentParQAnswer>): FullAssessmentParQGateResult {
        val answeredYes = answers.filter { it.answer }
        return FullAssessmentParQGateResult(
            hardStopFlagged = answeredYes.any { it.questionId.isHardStop() },
            flaggedHardStopQuestions = answeredYes.filter { it.questionId.isHardStop() }.map { it.questionId },
            exerciseFlags = answeredYes.mapNotNull { it.questionId.toExerciseFlagOrNull() }.toSet(),
        )
    }
}
