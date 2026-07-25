package com.fghilmany.nufitai.usecase.onboarding

import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.onboarding.entity.Experience
import com.fghilmany.nufitai.domain.onboarding.entity.FrequencyBucket
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import com.fghilmany.nufitai.domain.onboarding.entity.Level
import com.fghilmany.nufitai.domain.onboarding.entity.QuickAssessmentAnswer
import com.fghilmany.nufitai.domain.onboarding.entity.ResolvedSplit
import com.fghilmany.nufitai.domain.onboarding.entity.SplitPreference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ResolveQuickAssessmentTest {

    private val resolve = ResolveQuickAssessment()

    private fun answer(
        experience: Experience = Experience.NEVER,
        goal: GoalCategory = GoalCategory.FAT_LOSS,
        equipment: Set<EquipmentCategory> = setOf(EquipmentCategory.BODYWEIGHT),
        frequency: FrequencyBucket = FrequencyBucket.THREE,
        splitPreference: SplitPreference = SplitPreference.FULL_BODY,
    ) = QuickAssessmentAnswer(experience, goal, equipment, frequency, splitPreference)

    @Test
    fun `given belum pernah when resolving level then beginner`() {
        val result = resolve(answer(experience = Experience.NEVER))
        assertEquals(Level.BEGINNER, result.level)
    }

    @Test
    fun `given kurang 1 tahun when resolving level then beginner`() {
        val result = resolve(answer(experience = Experience.UNDER_1_YEAR))
        assertEquals(Level.BEGINNER, result.level)
    }

    @Test
    fun `given rutin bertahun when resolving level then intermediate not advanced`() {
        val result = resolve(answer(experience = Experience.YEARS_ROUTINE))
        assertEquals(Level.INTERMEDIATE, result.level)
    }

    @Test
    fun `given full body preference when resolving split then respected without note`() {
        val result = resolve(answer(splitPreference = SplitPreference.FULL_BODY))
        assertEquals(ResolvedSplit.FULL_BODY, result.resolvedSplit)
        assertNull(result.splitEducationalNote)
    }

    @Test
    fun `given upper lower with 2-3x frequency then respected but with educational note`() {
        val result = resolve(
            answer(splitPreference = SplitPreference.UPPER_LOWER, frequency = FrequencyBucket.THREE),
        )
        assertEquals(ResolvedSplit.UPPER_LOWER, result.resolvedSplit)
        assertNotNull(result.splitEducationalNote)
    }

    @Test
    fun `given upper lower with 4-5x frequency then respected without note`() {
        val result = resolve(
            answer(splitPreference = SplitPreference.UPPER_LOWER, frequency = FrequencyBucket.FOUR),
        )
        assertEquals(ResolvedSplit.UPPER_LOWER, result.resolvedSplit)
        assertNull(result.splitEducationalNote)
    }

    @Test
    fun `given tidak tahu and beginner then system resolves to full body`() {
        val result = resolve(
            answer(
                experience = Experience.NEVER,
                splitPreference = SplitPreference.UNKNOWN,
                frequency = FrequencyBucket.FOUR,
            ),
        )
        assertEquals(ResolvedSplit.FULL_BODY, result.resolvedSplit)
        assertNotNull(result.splitEducationalNote)
    }

    @Test
    fun `given tidak tahu and intermediate with 2-3x then system resolves to full body`() {
        val result = resolve(
            answer(
                experience = Experience.YEARS_ROUTINE,
                splitPreference = SplitPreference.UNKNOWN,
                frequency = FrequencyBucket.THREE,
            ),
        )
        assertEquals(ResolvedSplit.FULL_BODY, result.resolvedSplit)
    }

    @Test
    fun `given tidak tahu and intermediate with 4-5x then system resolves to upper lower`() {
        val result = resolve(
            answer(
                experience = Experience.YEARS_ROUTINE,
                splitPreference = SplitPreference.UNKNOWN,
                frequency = FrequencyBucket.FOUR,
            ),
        )
        assertEquals(ResolvedSplit.UPPER_LOWER, result.resolvedSplit)
    }

    @Test
    fun `templateId encodes level goal split and frequency`() {
        val result = resolve(
            answer(
                experience = Experience.NEVER,
                goal = GoalCategory.MUSCLE_GAIN,
                splitPreference = SplitPreference.FULL_BODY,
                frequency = FrequencyBucket.THREE,
            ),
        )
        assertEquals("BEGINNER_MUSCLE_GAIN_FULL_BODY_THREE", result.templateId)
    }
}
