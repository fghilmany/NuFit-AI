package com.fghilmany.nufitai.data.fullassessment.model

import com.fghilmany.nufitai.domain.fullassessment.entity.CapacityTestResult
import com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentParQAnswer
import com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentParQQuestionId
import com.fghilmany.nufitai.domain.fullassessment.entity.PlankResult
import com.fghilmany.nufitai.domain.fullassessment.entity.PushupResult
import com.fghilmany.nufitai.domain.fullassessment.entity.PushupVersion
import com.fghilmany.nufitai.domain.fullassessment.entity.SitToStandResult
import com.fghilmany.nufitai.domain.fullassessment.entity.TestProtocol
import kotlinx.serialization.Serializable

/**
 * JSON-column shapes for `full_assessment_result`'s `par_q_answers`/`capacity_test` TEXT
 * columns (ADR-003) -- kept out of `domain/` per architecture.md (DTOs live only in `data`).
 */
@Serializable
data class ParQAnswerJson(val questionId: String, val answer: Boolean) {
    fun toEntity() = FullAssessmentParQAnswer(FullAssessmentParQQuestionId.valueOf(questionId), answer)

    companion object {
        fun from(answer: FullAssessmentParQAnswer) = ParQAnswerJson(answer.questionId.name, answer.answer)
    }
}

@Serializable
data class CapacityTestJson(
    val pushupReps: Int? = null,
    val pushupVersion: String? = null,
    val pushupProtocol: String? = null,
    val plankSeconds: Int? = null,
    val plankProtocol: String? = null,
    val sitToStandReps: Int? = null,
    val sitToStandProtocol: String? = null,
) {
    fun toEntity(): CapacityTestResult = CapacityTestResult(
        pushup = if (pushupReps != null && pushupVersion != null && pushupProtocol != null) {
            PushupResult(pushupReps, PushupVersion.valueOf(pushupVersion), TestProtocol.valueOf(pushupProtocol))
        } else {
            null
        },
        plank = if (plankSeconds != null && plankProtocol != null) {
            PlankResult(plankSeconds, TestProtocol.valueOf(plankProtocol))
        } else {
            null
        },
        sitToStand = if (sitToStandReps != null && sitToStandProtocol != null) {
            SitToStandResult(sitToStandReps, TestProtocol.valueOf(sitToStandProtocol))
        } else {
            null
        },
    )

    companion object {
        fun from(result: CapacityTestResult) = CapacityTestJson(
            pushupReps = result.pushup?.reps,
            pushupVersion = result.pushup?.version?.name,
            pushupProtocol = result.pushup?.protocol?.name,
            plankSeconds = result.plank?.seconds,
            plankProtocol = result.plank?.protocol?.name,
            sitToStandReps = result.sitToStand?.reps,
            sitToStandProtocol = result.sitToStand?.protocol?.name,
        )
    }
}
