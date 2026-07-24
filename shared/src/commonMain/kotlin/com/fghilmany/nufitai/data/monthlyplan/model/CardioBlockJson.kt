package com.fghilmany.nufitai.data.monthlyplan.model

import com.fghilmany.nufitai.domain.monthlyplan.entity.CardioBlock
import kotlinx.serialization.Serializable

/** JSON shape for `plan_day`'s inline cardio-block columns (ADR-003) -- see PlanDay.sq comment. */
@Serializable
data class CardioBlockJson(val jenis: String, val durasiMenit: Int, val intensitas: String? = null) {
    fun toEntity() = CardioBlock(jenis, durasiMenit, intensitas)

    companion object {
        fun from(block: CardioBlock) = CardioBlockJson(block.jenis, block.durasiMenit, block.intensitas)
    }
}
