package com.fghilmany.nufitai.data.exerciselibrary.model

import com.fghilmany.nufitai.domain.exerciselibrary.entity.CommonMistake
import kotlinx.serialization.Serializable

/** JSON-column DTO for `Exercise.commonMistakes` (ADR-003) -- mirrors CardioBlockJson's from()/toEntity() shape. */
@Serializable
data class CommonMistakeJson(val title: String, val description: String) {
    fun toEntity(): CommonMistake = CommonMistake(title = title, description = description)

    companion object {
        fun from(entity: CommonMistake) = CommonMistakeJson(title = entity.title, description = entity.description)
    }
}
