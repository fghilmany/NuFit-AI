package com.fghilmany.nufitai.presentation.fullassessment.component

import androidx.compose.runtime.Composable
import com.fghilmany.nufitai.domain.exerciselibrary.entity.BodyArea
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.fullassessment_body_area_bahu
import nufitai.shared.generated.resources.fullassessment_body_area_leher
import nufitai.shared.generated.resources.fullassessment_body_area_lutut
import nufitai.shared.generated.resources.fullassessment_body_area_punggung_bawah
import org.jetbrains.compose.resources.stringResource

@Composable
fun BodyArea.shortLabel(): String = when (this) {
    BodyArea.PUNGGUNG_BAWAH -> stringResource(Res.string.fullassessment_body_area_punggung_bawah)
    BodyArea.LUTUT -> stringResource(Res.string.fullassessment_body_area_lutut)
    BodyArea.BAHU -> stringResource(Res.string.fullassessment_body_area_bahu)
    BodyArea.LEHER -> stringResource(Res.string.fullassessment_body_area_leher)
}
