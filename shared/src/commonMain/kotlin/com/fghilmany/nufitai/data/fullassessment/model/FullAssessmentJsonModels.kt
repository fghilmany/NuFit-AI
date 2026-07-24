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
    val pushupVersi: String? = null,
    val pushupProtokol: String? = null,
    val plankDetik: Int? = null,
    val plankProtokol: String? = null,
    val sitToStandReps: Int? = null,
    val sitToStandProtokol: String? = null,
) {
    fun toEntity(): CapacityTestResult = CapacityTestResult(
        pushup = if (pushupReps != null && pushupVersi != null && pushupProtokol != null) {
            PushupResult(pushupReps, PushupVersion.valueOf(pushupVersi), TestProtocol.valueOf(pushupProtokol))
        } else {
            null
        },
        plank = if (plankDetik != null && plankProtokol != null) {
            PlankResult(plankDetik, TestProtocol.valueOf(plankProtokol))
        } else {
            null
        },
        sitToStand = if (sitToStandReps != null && sitToStandProtokol != null) {
            SitToStandResult(sitToStandReps, TestProtocol.valueOf(sitToStandProtokol))
        } else {
            null
        },
    )

    companion object {
        fun from(result: CapacityTestResult) = CapacityTestJson(
            pushupReps = result.pushup?.reps,
            pushupVersi = result.pushup?.versi?.name,
            pushupProtokol = result.pushup?.protokol?.name,
            plankDetik = result.plank?.detik,
            plankProtokol = result.plank?.protokol?.name,
            sitToStandReps = result.sitToStand?.reps,
            sitToStandProtokol = result.sitToStand?.protokol?.name,
        )
    }
}
