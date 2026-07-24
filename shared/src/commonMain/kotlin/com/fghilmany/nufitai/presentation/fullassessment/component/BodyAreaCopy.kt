package com.fghilmany.nufitai.presentation.fullassessment.component

import com.fghilmany.nufitai.domain.exerciselibrary.entity.BodyArea

fun BodyArea.shortLabel(): String = when (this) {
    BodyArea.PUNGGUNG_BAWAH -> "Punggung bawah"
    BodyArea.LUTUT -> "Lutut"
    BodyArea.BAHU -> "Bahu"
    BodyArea.LEHER -> "Leher"
}
