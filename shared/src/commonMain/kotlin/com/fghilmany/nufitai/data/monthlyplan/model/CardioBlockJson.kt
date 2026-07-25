package com.fghilmany.nufitai.data.monthlyplan.model

import com.fghilmany.nufitai.domain.monthlyplan.entity.CardioBlock
import kotlinx.serialization.Serializable

/** JSON shape for `plan_day`'s inline cardio-block columns (ADR-003) -- see PlanDay.sq comment. */
@Serializable
data class CardioBlockJson(val type: String, val durationMinutes: Int, val intensity: String? = null) {
    fun toEntity() = CardioBlock(type, durationMinutes, intensity)

    companion object {
        fun from(block: CardioBlock) = CardioBlockJson(block.type, block.durationMinutes, block.intensity)
    }
}
